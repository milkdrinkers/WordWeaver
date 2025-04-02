package io.github.milkdrinkers.wordweaver.storage;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

/**
 * Interface for a registry containing languages
 * <p>
 * Used in {@link io.github.milkdrinkers.wordweaver.service.TranslationService} to hold loaded languages
 * @see io.github.milkdrinkers.wordweaver.service.TranslationService
 */
public interface LanguageRegistry {
    /**
     * Get the language with the given name
     *
     * @param name The name of the language
     * @return The language with the given name
     */
    @Nullable Language get(String name);

    /**
     * Get the language with the given name
     *
     * @param name The name of the language
     * @return The language with the given name
     */
    Optional<Language> getOptional(String name);

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
     * Get the names of all registered languages
     *
     * @return The names of all registered languages
     */
    Set<String> getRegistered();

    /**
     * Check if a language with the given name is registered
     *
     * @param name The name of the language
     * @return Whether the language is registered
     */
    boolean isRegistered(String name);

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
