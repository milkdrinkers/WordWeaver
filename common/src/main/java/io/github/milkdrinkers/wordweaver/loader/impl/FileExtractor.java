package io.github.milkdrinkers.wordweaver.loader.impl;

import io.github.milkdrinkers.wordweaver.Translation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Discovers and extracts bundle files shipped in the program resources.
 */
final class FileExtractor {
    @SuppressWarnings("FieldMayBeFinal")
    private static ClassLoader CLASS_LOADER = FileExtractor.class.getClassLoader();

    private FileExtractor() {
    }

    /**
     * Extracts resource files with a known extension from the resources if they don't exist in the target directory
     *
     * @param outputDir The directory where files should be extracted
     * @param resourceDir Relative path to the subdirectory where bundle files are located in the resources directory.
     * @param extensions The file extensions to extract (lowercase, without leading dot)
     * @return List of paths to extracted files
     * @throws IOException If an I/O error occurs
     */
    static List<Path> extractMissingResources(Path outputDir, Path resourceDir, Set<String> extensions) throws IOException {
        if (!Files.exists(outputDir))
            Files.createDirectories(outputDir);

        if (resourceDir.isAbsolute())
            throw new IOException("The resource directory must be relative");

        final List<Path> extractedFiles = new ArrayList<>();
        final List<Path> resourceFiles = findResourceFiles(resourceDir, extensions);

        for (Path resourcePath : resourceFiles) {
            final String fileName = resourcePath.getFileName().toString();
            final Path targetFile = outputDir.resolve(fileName);

            if (Files.notExists(targetFile)) {
                extractResourceFile(resourcePath, targetFile);
                extractedFiles.add(targetFile);
            }
        }

        return extractedFiles;
    }

    /**
     * Finds all resource files with a known extension in the resources
     *
     * @param resourceDir Relative path to the subdirectory where bundle files are located in the resources directory.
     * @param extensions  The file extensions to look for (lowercase, without leading dot)
     * @return List of resource paths to matching files
     * @throws IOException If an I/O error occurs
     */
    static List<Path> findResourceFiles(Path resourceDir, Set<String> extensions) throws IOException {
        final List<Path> resources = new ArrayList<>();

        // Are we running from a JAR or from the filesystem?
        final URL resourceUrl = CLASS_LOADER.getResource(resourceDir.toString());
        if (resourceUrl == null)
            return resources; // Resource directory not found

        if (resourceUrl.getProtocol().equals("jar")) {
            resources.addAll(findResourcesInJar(resourceDir, extensions)); // We're running from a JAR file
        } else {
            resources.addAll(findResourcesInFileSystem(resourceDir, extensions)); // We're running from the filesystem (development mode)
        }

        return resources;
    }

    /**
     * Opens a resource file as a stream, or null if it cannot be found.
     *
     * @param resourcePath The resource path
     * @return An input stream for the resource, or null
     */
    static InputStream openResource(Path resourcePath) {
        return CLASS_LOADER.getResourceAsStream(resourcePath.toString().replace("\\", "/"));
    }

    /**
     * Finds matching resources when running from a JAR
     *
     * @param resourceDir Relative path to the subdirectory where bundle files are located in the resources directory.
     * @param extensions  The file extensions to look for (lowercase, without leading dot)
     */
    private static List<Path> findResourcesInJar(Path resourceDir, Set<String> extensions) throws IOException {
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

                if (hasKnownExtension(entryName, extensions)) {
                    resources.add(Paths.get(entryName));
                }
            }
        }

        return resources;
    }

    /**
     * Finds matching resources when running from the filesystem (development mode)
     *
     * @param resourceDir Relative path to the subdirectory where bundle files are located in the resources directory.
     * @param extensions  The file extensions to look for (lowercase, without leading dot)
     */
    private static List<Path> findResourcesInFileSystem(Path resourceDir, Set<String> extensions) throws IOException {
        final List<Path> resources = new ArrayList<>();

        try {
            final URL url = CLASS_LOADER.getResource(resourceDir.toString());
            assert url != null;

            final URI uri = url.toURI();
            final Path resourcesPath = Paths.get(uri);

            // Get all matching files in the directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(resourcesPath, path -> hasKnownExtension(path.getFileName().toString(), extensions))) {
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
        try (final InputStream inputStream = openResource(resourcePath)) {
            if (inputStream == null)
                throw new IOException("Resource not found while extracting: " + resourcePath);

            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Returns whether the given file name ends with one of the known extensions.
     */
    private static boolean hasKnownExtension(String name, Set<String> extensions) {
        final int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1)
            return false;

        return extensions.contains(name.substring(dot + 1).toLowerCase());
    }
}
