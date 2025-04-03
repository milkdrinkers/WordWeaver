package io.github.milkdrinkers.wordweaver.loader.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileExtractorTest {
    @TempDir
    private Path tempDir;
    private Path outputDir;
    private ClassLoader originalClassLoader;

    @BeforeEach
    void setUp() throws Exception {
        outputDir = tempDir.resolve("lang");
        Files.createDirectories(outputDir);

        // Store the original class loader
        Field classLoaderField = FileExtractor.class.getDeclaredField("CLASS_LOADER");
        classLoaderField.setAccessible(true);
        originalClassLoader = (ClassLoader) classLoaderField.get(null);
    }

    /**
     * Test that files are properly extracted when they don't exist
     */
    @Test
    void shouldExtractJsonResources() throws IOException {
        // Extract resources
        List<Path> extractedFiles = FileExtractor.extractJsonResources(outputDir);

        // Verify extracted files
        assertEquals(2, extractedFiles.size());
        assertTrue(Files.exists(outputDir.resolve("en_US.jsonc")));
        assertTrue(Files.exists(outputDir.resolve("en_GB.json")));
    }

    /**
     * Test that files are not extracted when they already exist
     */
    @Test
    void shouldNotExtractExistingFiles() throws IOException {
        // Create existing file
        Path existingFile1 = outputDir.resolve("en_US.jsonc");
        Path existingFile2 = outputDir.resolve("en_GB.json");
        Files.writeString(existingFile1, "{\"existing\":\"content\"}");
        Files.writeString(existingFile2, "{\"existing\":\"content\"}");

        // Extract resources
        List<Path> extractedFiles = FileExtractor.extractJsonResources(outputDir);

        // Verify no files were extracted
        assertEquals(0, extractedFiles.size());

        // Verify existing file content wasn't changed
        String content1 = Files.readString(existingFile1);
        assertEquals("{\"existing\":\"content\"}", content1);
        String content2 = Files.readString(existingFile1);
        assertEquals("{\"existing\":\"content\"}", content2);
    }

    /**
     * Test merging JSON files with different structures
     */
    @Test
    void shouldMergeJsonFiles() throws Exception {
        // Create original JSON file with structure
        String originJson = "{\"key1\":\"value1\",\"section\":{\"subkey1\":\"subvalue1\",\"subkey2\":\"subvalue2\"},\"key2\":\"value2\"}";
        Path originPath = tempDir.resolve("origin.json");
        Files.writeString(originPath, originJson);

        // Create target JSON file with modified values and missing keys
        String targetJson = "{\"key1\":\"modified1\",\"section\":{\"subkey1\":\"modified_subvalue1\"}}";
        Path targetPath = tempDir.resolve("target.json");
        Files.writeString(targetPath, targetJson);

        // Use reflection to call mergeJsonObjects method
        Method mergeMethod = FileExtractor.class.getDeclaredMethod("mergeJsonObjects", JsonObject.class, JsonObject.class);
        mergeMethod.setAccessible(true);

        JsonObject origin = JsonParser.parseString(originJson).getAsJsonObject();
        JsonObject target = JsonParser.parseString(targetJson).getAsJsonObject();

        JsonObject result = (JsonObject) mergeMethod.invoke(null, origin, target);

        // Verify result has all keys
        assertTrue(result.has("key1"));
        assertTrue(result.has("key2"));
        assertTrue(result.has("section"));

        // Verify values from target were preserved
        assertEquals("modified1", result.get("key1").getAsString());

        // Verify missing keys from origin were added
        assertEquals("value2", result.get("key2").getAsString());

        // Verify nested objects were merged properly
        JsonObject section = result.getAsJsonObject("section");
        assertEquals("modified_subvalue1", section.get("subkey1").getAsString());
        assertEquals("subvalue2", section.get("subkey2").getAsString());
    }

    /**
     * Test updating files with missing keys
     */
    @Test
    void shouldUpdateFilesWithMissingKeys() throws Exception {
        // Create existing file with partial content
        Path existingFile = outputDir.resolve("en_US.jsonc");
        Files.writeString(existingFile, "{\"greeting\":\"Hello\"}");

        // Call updateFiles method
        FileExtractor.updateFiles(outputDir);
    }

    /**
     * Test finding resources in the file system
     */
    @Test
    void shouldFindResourcesInFileSystem() throws Exception {
        // Create mock directory structure
        Path resourceDir = tempDir.resolve("lang");
        Files.createDirectories(resourceDir);

        // Create mock JSON files
        Path enUs = resourceDir.resolve("en_US.jsonc");
        Path frFr = resourceDir.resolve("fr_FR.jsonc");
        Files.writeString(enUs, "{}");
        Files.writeString(frFr, "{}");

        // Set up test ClassLoader that will find our mock resources
        TestClassLoader testLoader = new TestClassLoader(resourceDir);

        // Use reflection to replace CLASS_LOADER
        Field classLoaderField = FileExtractor.class.getDeclaredField("CLASS_LOADER");
        classLoaderField.setAccessible(true);
        classLoaderField.set(null, testLoader);

        // Call method via reflection
        Method findResourcesMethod = FileExtractor.class.getDeclaredMethod("findResourcesInFileSystem");
        findResourcesMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> resources = (List<String>) findResourcesMethod.invoke(null);

        // Verify both files were found
        assertEquals(2, resources.size());
        List<String> fileNames = resources.stream()
            .map(s -> s.substring(s.lastIndexOf('/') + 1))
            .toList();
        assertTrue(fileNames.contains("en_US.jsonc"));
        assertTrue(fileNames.contains("fr_FR.jsonc"));
    }

    /**
     * Helper method to restore original ClassLoader after tests
     */
    @AfterEach
    void tearDown() throws Exception {
        // Restore the original class loader
        Field classLoaderField = FileExtractor.class.getDeclaredField("CLASS_LOADER");
        classLoaderField.setAccessible(true);
        classLoaderField.set(null, originalClassLoader);
    }

    private static class TestClassLoader extends ClassLoader {
        private final Path resourceDir;

        public TestClassLoader(Path resourceDir) {
            this.resourceDir = resourceDir;
        }

        @Override
        public java.net.URL getResource(String name) {
            try {
                if (name.equals("lang")) {
                    return resourceDir.toUri().toURL();
                }
                return null;
            } catch (java.net.MalformedURLException e) {
                return null;
            }
        }
    }
}