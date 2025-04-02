package io.github.milkdrinkers.wordweaver.loader.impl;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.TranslationLoader;
import io.github.milkdrinkers.wordweaver.storage.Language;
import io.github.milkdrinkers.wordweaver.storage.LanguageLoadException;
import io.github.milkdrinkers.wordweaver.storage.LanguageRegistry;
import io.github.milkdrinkers.wordweaver.storage.LanguageEntry;
import io.github.milkdrinkers.wordweaver.storage.impl.LanguageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Loads translations from JSON/JSONC files
 */
public class JsonTranslationLoader implements TranslationLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonTranslationLoader.class);
    
    private final TranslationConfig config;
    private final LanguageRegistry registry;

    public JsonTranslationLoader(TranslationConfig config, LanguageRegistry registry) {
        this.config = config;
        this.registry = registry;
    }

    @Override
    public void extractMissingLanguages() throws IOException {
        // Extract missing language files from .jar resources
        try {
            FileExtractor.extractJsonResources(config.getTranslationDirectory());
        } catch (RuntimeException e) {
            LOGGER.error("Failed to extract missing language files: ", e);
        }
    }

    @Override
    public void updateExistingLanguages() throws IOException {
        // Add missing entries from .jar resources to extracted language files
        try {
            FileExtractor.updateFiles(config.getTranslationDirectory());
        } catch (RuntimeException e) {
            LOGGER.error("Failed to update existing language files: ", e);
        }
    }

    @Override
    public void loadLanguages() throws IOException {
        // Load languages from extracted languages
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(config.getTranslationDirectory());

            // Load each translation file
            try (Stream<Path> files = Files.list(config.getTranslationDirectory())) {
                files.filter(path -> path.toString().endsWith(".jsonc") || path.toString().endsWith(".json"))
                .forEach(this::load);
            }
        } catch (RuntimeException e) {
            LOGGER.error("Failed to load language files: ", e);
        }
    }

    private void load(Path file) throws LanguageLoadException {
        try {
            final String filename = file.getFileName().toString();
            final String language = filename.substring(0, filename.lastIndexOf('.'));
            final Map<String, LanguageEntry> translations = FileReader.readFile(file);

            final Language languageFile = new LanguageImpl(language, translations);

            registry.register(languageFile);
        } catch (LanguageLoadException e) {
            LOGGER.error("Failed to load language file: {}", file.getFileName(), e);
            throw e;
        }
    }
}