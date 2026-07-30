package io.github.milkdrinkers.wordweaver.loader;

import io.github.milkdrinkers.wordweaver.storage.TranslationBundle;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;

import java.io.IOException;

/**
 * Interface for loading bundles from various sources
 *
 * @see TranslationBundleRegistry
 * @see TranslationBundle
 */
public interface TranslationLoader {
    /**
     * Extract missing bundle files from .jar resources
     */
    void extractMissingBundles() throws IOException;

    /**
     * Update existing bundle files with missing entries
     */
    void updateExistingBundles() throws IOException;

    /**
     * Load bundle files from configured sources
     */
    void loadBundles() throws IOException;
}
