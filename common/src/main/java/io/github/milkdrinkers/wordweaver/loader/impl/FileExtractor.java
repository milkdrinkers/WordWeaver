package io.github.milkdrinkers.wordweaver.loader.impl;

import com.google.gson.*;
import io.github.milkdrinkers.wordweaver.Translation;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class FileExtractor {
    private static final String RESOURCE_PATH = "lang";
    @SuppressWarnings("FieldMayBeFinal")
    private static ClassLoader CLASS_LOADER = FileExtractor.class.getClassLoader();

    private FileExtractor() {}

    /**
     * Extracts Json files from JAR if they don't exist in the target directory
     *
     * @param outputDir The directory where files should be extracted
     * @return List of paths to extracted files
     * @throws IOException If an I/O error occurs
     */
    public static List<Path> extractJsonResources(Path outputDir) throws IOException {
        if (!Files.exists(outputDir))
            Files.createDirectories(outputDir);

        final List<Path> extractedFiles = new ArrayList<>();
        final List<String> resourceFiles = findJsonResourceFiles();

        for (String resourcePath : resourceFiles) {
            final String fileName = Paths.get(resourcePath).getFileName().toString(); // Get just the filename
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
     * @return List of resource paths to Json files
     * @throws IOException If an I/O error occurs
     */
    private static List<String> findJsonResourceFiles() throws IOException {
        final List<String> resources = new ArrayList<>();

        // Are we running from a JAR or from the filesystem?
        final URL resourceUrl = CLASS_LOADER.getResource(RESOURCE_PATH);
        if (resourceUrl == null)
            return resources; // Resource directory not found

        if (resourceUrl.getProtocol().equals("jar")) {
            resources.addAll(findResourcesInJar()); // We're running from a JAR file
        } else {
            resources.addAll(findResourcesInFileSystem()); // We're running from the filesystem (development mode)
        }

        return resources;
    }

    /**
     * Finds Json/Jsonc resources when running from a JAR
     */
    private static List<String> findResourcesInJar() throws IOException {
        List<String> resources = new ArrayList<>();

        // Get path to the JAR file
        String jarPath;
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
            final String resourcePrefix = RESOURCE_PATH + "/";

            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String entryName = entry.getName();

                if (entry.isDirectory())
                    continue;

                if (!entryName.startsWith(resourcePrefix))
                    continue;

                if (entryName.endsWith(".json") || entryName.endsWith(".jsonc")) {
                    resources.add(entryName);
                }
            }
        }

        return resources;
    }

    /**
     * Finds Json/Jsonc resources when running from the filesystem (development mode)
     */
    private static List<String> findResourcesInFileSystem() throws IOException {
        final List<String> resources = new ArrayList<>();

        try {
            final URL url = CLASS_LOADER.getResource(RESOURCE_PATH);
            assert url != null;

            final URI uri = url.toURI();
            final Path resourcesPath = Paths.get(uri);

            // Get all Json/Jsonc files in the directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(resourcesPath, path -> path.toString().endsWith(".json") || path.toString().endsWith(".jsonc"))) {
                for (Path path : stream) {
                    resources.add(RESOURCE_PATH + "/" + path.getFileName().toString()); // Convert to resource path format
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
    private static void extractResourceFile(String resourcePath, Path targetFile) throws IOException {
        try (InputStream in = CLASS_LOADER.getResourceAsStream(resourcePath)) {
            if (in == null)
                throw new IOException("Resource not found: " + resourcePath);

            Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void updateFiles(Path outputDir) throws IOException {
        // Create output directory if it doesn't exist
        if (!Files.exists(outputDir))
            Files.createDirectories(outputDir);

        final List<String> resourceFiles = findJsonResourceFiles();

        for (String resourcePath : resourceFiles) {
            final String fileName = Paths.get(resourcePath).getFileName().toString(); // Get just the filename
            final Path targetFile = outputDir.resolve(fileName);

            if (!Files.exists(targetFile))
                continue;

            mergeJsonFiles("lang/" + fileName, targetFile);
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
    public static void mergeJsonFiles(String originPath, Path targetPath) throws IOException {
        // Read origin file from JAR
        final InputStream originStream = CLASS_LOADER.getResourceAsStream(originPath);
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
