package io.github.milkdrinkers.wordweaver.loader.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileExtractorTest {
    private static final Set<String> EXTENSIONS = new HashSet<>(Arrays.asList("json", "jsonc"));

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
    void shouldExtractResources() throws IOException {
        // Extract resources
        List<Path> extractedFiles = FileExtractor.extractMissingResources(outputDir, Path.of("lang"), EXTENSIONS);

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
        List<Path> extractedFiles = FileExtractor.extractMissingResources(outputDir, Path.of("lang"), EXTENSIONS);

        // Verify no files were extracted
        assertEquals(0, extractedFiles.size());

        // Verify existing file content wasn't changed
        String content1 = Files.readString(existingFile1);
        assertEquals("{\"existing\":\"content\"}", content1);
        String content2 = Files.readString(existingFile2);
        assertEquals("{\"existing\":\"content\"}", content2);
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

        List<Path> resources = FileExtractor.findResourceFiles(Path.of("lang"), EXTENSIONS);

        // Verify both files were found
        assertEquals(2, resources.size());
        List<String> fileNames = resources.stream()
            .map(s -> s.getFileName().toString())
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
