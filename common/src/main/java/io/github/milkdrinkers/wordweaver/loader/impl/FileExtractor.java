package io.github.milkdrinkers.wordweaver.loader.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.milkdrinkers.wordweaver.Translation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class FileExtractor {
    @SuppressWarnings("FieldMayBeFinal")
    private static ClassLoader CLASS_LOADER = FileExtractor.class.getClassLoader();

    private FileExtractor() {
    }

    /**
     * Extracts Json files from JAR if they don't exist in the target directory
     *
     * @param outputDir   The directory where files should be extracted
     * @param resourceDir Relative path to the subdirectory where language files are located in the resources directory.
     * @return List of paths to extracted files
     * @throws IOException If an I/O error occurs
     */
    public static List<Path> extractJsonResources(Path outputDir, Path resourceDir) throws IOException {
        if (!Files.exists(outputDir))
            Files.createDirectories(outputDir);

        if (resourceDir.isAbsolute())
            throw new IOException("The resource directory must be relative");

        final List<Path> extractedFiles = new ArrayList<>();
        final List<Path> resourceFiles = findJsonResourceFiles(resourceDir);

        for (Path resourcePath : resourceFiles) {
            final String fileName = resourcePath.getFileName().toString(); // Get just the filename
            final Path targetFile = outputDir.resolve(fileName);

            // Only extract if file doesn't exist
            if (Files.notExists(targetFile)) {
                extractResourceFile(resourcePath, targetFile);
                extractedFiles.add(targetFile);
            }
        }

        return extractedFiles;
    }

    /**
     * Finds all Json resource files in the JAR
     *
     * @param resourceDir Relative path to the subdirectory where language files are located in the resources directory.
     * @return List of resource paths to Json files
     * @throws IOException If an I/O error occurs
     */
    private static List<Path> findJsonResourceFiles(Path resourceDir) throws IOException {
        final List<Path> resources = new ArrayList<>();

        // Are we running from a JAR or from the filesystem?
        final URL resourceUrl = CLASS_LOADER.getResource(resourceDir.toString());
        if (resourceUrl == null)
            return resources; // Resource directory not found

        if (resourceUrl.getProtocol().equals("jar")) {
            resources.addAll(findResourcesInJar(resourceDir)); // We're running from a JAR file
        } else {
            resources.addAll(findResourcesInFileSystem(resourceDir)); // We're running from the filesystem (development mode)
        }

        return resources;
    }

    /**
     * Finds Json/Jsonc resources when running from a JAR
     *
     * @param resourceDir Relative path to the subdirectory where language files are located in the resources directory.
     */
    private static List<Path> findResourcesInJar(Path resourceDir) throws IOException {
        final List<Path> resources = new ArrayList<>();

        // Get path to the JAR file
        final String jarPath;
        try {
            jarPath = Translation.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
        } catch (URISyntaxException e) {
            throw new IOException("Failed to get JAR path", e);
        }

        // Search through the JAR for matching files
        try (JarFile jar = new JarFile(jarPath)) {
            final Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String entryName = entry.getName();

                if (entry.isDirectory())
                    continue;

                if (!entryName.startsWith(resourceDir.toString()))
                    continue;

                if (entryName.endsWith(".json") || entryName.endsWith(".jsonc")) {
                    resources.add(Paths.get(entryName));
                }
            }
        }

        return resources;
    }

    /**
     * Finds Json/Jsonc resources when running from the filesystem (development mode)
     *
     * @param resourceDir Relative path to the subdirectory where language files are located in the resources directory.
     */
    private static List<Path> findResourcesInFileSystem(Path resourceDir) throws IOException {
        final List<Path> resources = new ArrayList<>();

        try {
            final URL url = CLASS_LOADER.getResource(resourceDir.toString());
            assert url != null;

            final URI uri = url.toURI();
            final Path resourcesPath = Paths.get(uri);

            // Get all Json/Jsonc files in the directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(resourcesPath, path -> path.toString().endsWith(".json") || path.toString().endsWith(".jsonc"))) {
                for (Path path : stream) {
                    resources.add(resourceDir.resolve(path.getFileName())); // Convert to resource path format
                }
            }
        } catch (URISyntaxException e) {
            throw new IOException("Failed to get resource directory", e);
        }

        return resources;
    }

    /**
     * Extracts a resource file to the specified target path
     */
    private static void extractResourceFile(Path resourcePath, Path targetFile) throws IOException {
        try (final InputStream inputStream = CLASS_LOADER.getResourceAsStream(resourcePath.toString())) {
            if (inputStream == null)
                throw new IOException("Resource not found: " + resourcePath);

            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Updates existing Json files in the output directory by merging them with the corresponding resource files
     *
     * @param outputDir   The directory where files should be extracted
     * @param resourceDir Relative path to the subdirectory where language files are located in the resources directory.
     * @throws IOException If an I/O error occurs
     */
    public static void updateFiles(Path outputDir, Path resourceDir) throws IOException {
        if (!Files.exists(outputDir))
            Files.createDirectories(outputDir);

        if (resourceDir.isAbsolute())
            throw new IOException("The resource directory must be relative");

        final List<Path> resourceFiles = findJsonResourceFiles(resourceDir);

        for (Path resourcePath : resourceFiles) {
            final String fileName = resourcePath.getFileName().toString(); // Get just the filename
            final Path targetFile = outputDir.resolve(fileName);

            if (!Files.exists(targetFile))
                continue;

            mergeJsonFiles(resourceDir.resolve(fileName), targetFile);
        }
    }

    /**
     * Merges two Json files by adding missing keys from origin to target
     * while preserving target's existing content and maintaining origin's order.
     *
     * @param originPath Path to the origin Json file (with more keys)
     * @param targetPath Path to the target Json file (with fewer keys)
     * @throws IOException If an I/O error occurs
     */
    private static void mergeJsonFiles(Path originPath, Path targetPath) throws IOException {
        // Read origin file from JAR
        final InputStream originStream = CLASS_LOADER.getResourceAsStream(originPath.toString());
        if (originStream == null)
            throw new IOException("Resource not found: " + originPath);
        final Reader originReader = new InputStreamReader(originStream);

        // Read target file from filesystem
        final String targetContent = new String(Files.readAllBytes(targetPath));

        // Parse Json
        final JsonObject originJson = JsonParser.parseReader(originReader).getAsJsonObject();
        final JsonObject targetJson = JsonParser.parseString(targetContent).getAsJsonObject();

        // Merge preserving order
        final JsonObject mergedJson = mergeJsonObjects(originJson, targetJson);

        Files.write(targetPath, FileReader.GSON.toJson(mergedJson).getBytes());
    }

    /**
     * Recursively merges Json objects, adding missing keys from origin to target while preserving target's existing values and maintaining origin's order.
     *
     * @param origin The original Json object
     * @param target The user modified Json object
     * @return The merged Json object
     */
    private static JsonObject mergeJsonObjects(JsonObject origin, JsonObject target) {
        final Set<String> processedKeys = new HashSet<>();
        final JsonObject result = new JsonObject();

        // Add all keys from origin in original order
        for (Map.Entry<String, JsonElement> entry : origin.entrySet()) {
            final String key = entry.getKey();
            processedKeys.add(key);

            if (target.has(key)) { // Key exists in both, check if deep merge required
                final JsonElement originValue = entry.getValue();
                final JsonElement targetValue = target.get(key);

                if (originValue.isJsonObject() && targetValue.isJsonObject()) {
                    result.add(key, mergeJsonObjects(originValue.getAsJsonObject(), targetValue.getAsJsonObject())); // Recursively merge nested objects
                } else {
                    result.add(key, targetValue); // Keep target's value
                }
            } else {
                result.add(key, entry.getValue()); // Key exists only in origin, add to target
            }
        }

        // Add remaining keys from target that are not present in origin
        for (Map.Entry<String, JsonElement> entry : target.entrySet()) {
            final String key = entry.getKey();
            if (!processedKeys.contains(key)) {
                result.add(key, entry.getValue());
            }
        }

        return result;
    }
}
