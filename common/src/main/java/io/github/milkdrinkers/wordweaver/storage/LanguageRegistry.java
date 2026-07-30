package io.github.milkdrinkers.wordweaver.storage;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for a registry containing languages
 * <p>
 * Used in {@link io.github.milkdrinkers.wordweaver.service.TranslationService} to hold loaded languages
 *
 * @see io.github.milkdrinkers.wordweaver.service.TranslationService
 */
public interface LanguageRegistry {
    /**
     * Get the language with the given locale
     *
     * @param locale The locale of the language
     * @return The language with the given locale
     */
    @Nullable Language get(Locale locale);

    /**
     * Get the language with the given locale
     *
     * @param locale The locale of the language
     * @return The language with the given locale
     */
    Optional<Language> getOptional(Locale locale);

    /**
     * Get the current language
     *
     * @return The current language
     */
    @Nullable Language getCurrent();

    /**
     * Get the current language
     *
     * @return The current language
     */
    Optional<Language> getCurrentOptional();

    /**
     * Get the default language
     *
     * @return The default language
     */
    @Nullable Language getDefault();

    /**
     * Get the default language
     *
     * @return The default language
     */
    Optional<Language> getDefaultOptional();

    /**
     * Get the locales of all registered languages
     *
     * @return The locales of all registered languages
     */
    Set<Locale> getRegistered();

    /**
     * Check if a language with the given locale is registered
     *
     * @param locale The locale of the language
     * @return Whether the language is registered
     */
    boolean isRegistered(Locale locale);

    /**
     * Get a cached, combined {@link Set} of all the {@link LanguageEntry} keys in the registered current {@literal &} default languages
     *
     * @return The keys in current {@literal &} default languages
     */
    Set<String> getKeys();

    /**
     * Register a new language
     *
     * @param language The language to register
     */
    void register(Language language);

    /**
     * Clear all languages in the registry
     */
    void clear();
}
