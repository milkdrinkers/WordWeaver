package io.github.milkdrinkers.wordweaver.storage;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The translations for a single locale.
 * <p>
 * A bundle is a collection of {@link TranslationBundleEntry entries}.
 * It is used to manage and access the translations for one locale in WordWeaver.
 * <p>
 * This interface is used by the {@link TranslationBundleRegistry} to load and manage bundles.
 *
 * @see TranslationBundleRegistry
 */
public interface TranslationBundle {
    /**
     * Get the locale of this bundle
     *
     * @return The locale of this bundle
     */
    Locale getLocale();

    /**
     * Get the entries in this bundle
     *
     * @return A map of entries
     */
    Map<String, TranslationBundleEntry> getEntries();

    /**
     * Get the entry for the given key
     *
     * @param key The key to get the entry for
     * @return The entry for the given key
     */
    @Nullable TranslationBundleEntry getEntry(String key);

    /**
     * Get the entry for the given key
     *
     * @param key The key to get the entry for
     * @return The entry for the given key
     */
    Optional<TranslationBundleEntry> getEntryOptional(String key);

    /**
     * Check if the bundle contains an entry for the given key
     *
     * @param key The key to check
     * @return True if the bundle contains an entry for the given key, false otherwise
     */
    boolean hasEntry(String key);

    /**
     * Get all keys in this bundle
     *
     * @return A set of all keys in this bundle
     */
    Set<String> getKeys();
}
