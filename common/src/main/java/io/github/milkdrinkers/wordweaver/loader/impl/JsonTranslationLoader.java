package io.github.milkdrinkers.wordweaver.loader.impl;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.TranslationLoader;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundle;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
import io.github.milkdrinkers.wordweaver.storage.TranslationLoadException;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.milkdrinkers.wordweaver.LocaleUtil.fromTag;

/**
 * Loads bundles from JSON/JSONC files
 */
public class JsonTranslationLoader implements TranslationLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonTranslationLoader.class);

    private final TranslationConfig config;
    private final TranslationBundleRegistry registry;

    public JsonTranslationLoader(TranslationConfig config, TranslationBundleRegistry registry) {
        this.config = config;
        this.registry = registry;
    }

    @Override
    public void extractMissingBundles() throws IOException {
        // Extract missing bundle files from .jar resources
        try {
            FileExtractor.extractJsonResources(config.getTranslationDirectory(), config.getResourcesDirectory());
        } catch (RuntimeException e) {
            LOGGER.error("Failed to extract missing bundle files: ", e);
            throw e;
        }
    }

    @Override
    public void updateExistingBundles() throws IOException {
        // Add missing entries from .jar resources to extracted bundle files
        try {
            FileExtractor.updateFiles(config.getTranslationDirectory(), config.getResourcesDirectory());
        } catch (RuntimeException e) {
            LOGGER.error("Failed to update existing bundle files: ", e);
            throw e;
        }
    }

    @Override
    public void loadBundles() throws IOException {
        // Load bundles from extracted files
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(config.getTranslationDirectory());

            // Load each bundle file
            try (Stream<Path> files = Files.list(config.getTranslationDirectory())) {
                files.filter(path -> path.toString().endsWith(".jsonc") || path.toString().endsWith(".json"))
                    .forEach(this::load);
            }
        } catch (RuntimeException e) {
            LOGGER.error("Failed to load bundle files: ", e);
            throw e;
        }
    }

    private void load(Path file) throws TranslationLoadException {
        try {
            final String filename = file.getFileName().toString();
            final String localeTag = filename.substring(0, filename.lastIndexOf('.'));
            final Map<String, TranslationBundleEntry> entries = FileReader.readFile(file);

            final TranslationBundle bundle = new TranslationBundleImpl(fromTag(localeTag), entries);

            registry.register(bundle);
        } catch (TranslationLoadException e) {
            LOGGER.error("Failed to load bundle file: {}", file.getFileName(), e);
            throw e;
        }
    }
}
