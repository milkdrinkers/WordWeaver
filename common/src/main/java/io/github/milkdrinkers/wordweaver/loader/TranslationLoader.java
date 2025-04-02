package io.github.milkdrinkers.wordweaver.loader;

import io.github.milkdrinkers.wordweaver.storage.Language;
import io.github.milkdrinkers.wordweaver.storage.LanguageRegistry;

import java.io.IOException;

/**
 * Interface for loading language files from various sources
 * @see LanguageRegistry
 * @see Language
 */
public interface TranslationLoader {
    /**
     * Extract missing language files from .jar resources
     */
    void extractMissingLanguages() throws IOException;

    /**
     * Update existing language files with missing translations entries
     */
    void updateExistingLanguages() throws IOException;

    /**
     * Load language files from configured sources
     */
    void loadLanguages() throws IOException;
}