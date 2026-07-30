package io.github.milkdrinkers.wordweaver.storage;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for a registry containing bundles
 * <p>
 * Used in {@link io.github.milkdrinkers.wordweaver.service.TranslationService} to hold loaded bundles
 *
 * @see io.github.milkdrinkers.wordweaver.service.TranslationService
 */
public interface TranslationBundleRegistry {
    /**
     * Get the bundle with the given locale
     *
     * @param locale The locale of the bundle
     * @return The bundle with the given locale
     */
    @Nullable TranslationBundle get(Locale locale);

    /**
     * Get the bundle with the given locale
     *
     * @param locale The locale of the bundle
     * @return The bundle with the given locale
     */
    Optional<TranslationBundle> getOptional(Locale locale);

    /**
     * Get the current bundle
     *
     * @return The current bundle
     */
    @Nullable TranslationBundle getCurrent();

    /**
     * Get the current bundle
     *
     * @return The current bundle
     */
    Optional<TranslationBundle> getCurrentOptional();

    /**
     * Get the default bundle
     *
     * @return The default bundle
     */
    @Nullable TranslationBundle getDefault();

    /**
     * Get the default bundle
     *
     * @return The default bundle
     */
    Optional<TranslationBundle> getDefaultOptional();

    /**
     * Get the locales of all registered bundles
     *
     * @return The locales of all registered bundles
     */
    Set<Locale> getRegisteredLocales();

    /**
     * Check if a bundle with the given locale is registered
     *
     * @param locale The locale of the bundle
     * @return Whether the bundle is registered
     */
    boolean isRegistered(Locale locale);

    /**
     * Get a cached, combined {@link Set} of all the {@link TranslationBundleEntry} keys in the registered current {@literal &} default bundles
     *
     * @return The keys in the current {@literal &} default bundles
     */
    Set<String> getKeys();

    /**
     * Register a new bundle
     *
     * @param bundle The bundle to register
     */
    void register(TranslationBundle bundle);

    /**
     * Clear all bundles in the registry
     */
    void clear();
}
