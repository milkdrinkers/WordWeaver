package io.github.milkdrinkers.wordweaver;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.storage.LanguageRegistry;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Implementations of this interface define the behavior of when a key does not exist in a translation file.
 */
public interface MissingTranslationHandler {
    /**
     * Default implementation of the MissingTranslationHandler used in WordWeaver.
     */
    MissingTranslationHandler DEFAULT = new DefaultMissingTranslationHandler();

    /**
     * Handle a missing entry for a key.
     * @param config The translation config
     * @param registry The language registry
     * @param key The key to the translation
     * @param fallback The default value provided by the developer
     * @return The value to return if the key is missing
     */
    @Nullable String handle(TranslationConfig config, LanguageRegistry registry, String key, @Nullable String fallback);

    /**
     * Handle a missing entry for a key.
     * @param config The translation config
     * @param registry The language registry
     * @param key The key to the translation
     * @param fallback The default value provided by the developer
     * @return The value to return if the key is missing
     */
    @Nullable Component handle(TranslationConfig config, LanguageRegistry registry, String key, @Nullable Component fallback);

    /**
     * Handle a missing entry for a key.
     * @param config The translation config
     * @param registry The language registry
     * @param key The key to the translation
     * @param fallback The default value provided by the developer
     * @return The value to return if the key is missing
     */
    @Nullable List<String> handleListString(TranslationConfig config, LanguageRegistry registry, String key, @Nullable List<String> fallback);

    /**
     * Handle a missing entry for a key.
     * @param config The translation config
     * @param registry The language registry
     * @param key The key to the translation
     * @param fallback The default value provided by the developer
     * @return The value to return if the key is missing
     */
    @Nullable List<Component> handleListComponent(TranslationConfig config, LanguageRegistry registry, String key, @Nullable List<Component> fallback);
}
