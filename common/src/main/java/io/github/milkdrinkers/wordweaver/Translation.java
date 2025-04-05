package io.github.milkdrinkers.wordweaver;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.TranslationLoader;
import io.github.milkdrinkers.wordweaver.loader.impl.JsonTranslationLoader;
import io.github.milkdrinkers.wordweaver.service.TranslationService;
import io.github.milkdrinkers.wordweaver.service.impl.TranslationServiceImpl;
import io.github.milkdrinkers.wordweaver.storage.LanguageRegistry;
import io.github.milkdrinkers.wordweaver.storage.impl.LanguageRegistryImpl;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Main API interface for accessing WordWeaver translations.
 */
public final class Translation {
    private static final Logger LOGGER = LoggerFactory.getLogger(Translation.class);

    static {
        if (LOGGER.getClass().getName().contains("NOPLogger"))
            System.err.println("No SLF4J implementation found for WordWeaver. \nConsider adding an SLF4J compatible logging implementation to your project.");
    }

    private Translation() {
    }

    /**
     * Get a translation string by key
     *
     * @param key The key to the translation
     */
    public static String of(String key) {
        return of(key, null);
    }

    /**
     * Get a translation string by key
     *
     * @param key      The key to the translation
     * @param fallback The default value to return if no valid value was found
     */
    public static String of(String key, String fallback) {
        return TranslationProvider.getInstance().getTranslationService().getString(key, fallback);
    }

    /**
     * Get a list of translation strings by key
     *
     * @param key The key to the translation
     * @see #ofList(String)
     */
    public static List<String> ofList(String key) {
        return ofList(key, null);
    }

    /**
     * Get a list of translation strings by key
     *
     * @param key      The key to the translation
     * @param fallback The default value to return if no valid value was found, empty list if null
     */
    public static List<String> ofList(String key, @Nullable List<String> fallback) {
        return TranslationProvider.getInstance().getTranslationService().getStringList(key, fallback != null ? fallback : Collections.emptyList());
    }

    /**
     * Get a translation as an Adventure Component
     *
     * @param key The key to the translation
     * @see Component
     */
    public static Component as(String key) {
        return as(key, null);
    }

    /**
     * Get a translation as an Adventure Component
     *
     * @param key      The key to the translation
     * @param fallback The default value to return if no valid value was found
     * @see Component
     */
    public static Component as(String key, Component fallback) {
        return TranslationProvider.getInstance().getTranslationService().getComponent(key, fallback);
    }

    /**
     * Get a list of translation Adventure Components by key
     *
     * @param key The key to the translation
     * @see #asList(String, List)
     * @see Component
     */
    public static List<Component> asList(String key) {
        return asList(key, null);
    }

    /**
     * Get a list as translation Adventure Components by key
     *
     * @param key      The key to the translation
     * @param fallback The default value to return if no valid value was found, empty list if null
     * @see #asList(String)
     * @see Component
     */
    public static List<Component> asList(String key, @Nullable List<Component> fallback) {
        return TranslationProvider.getInstance().getTranslationService().getComponentList(key, fallback != null ? fallback : Collections.emptyList());
    }

    /**
     * Get a list of all translation entries in the current and fallback language
     *
     * @return A set of all keys in the current and fallback language
     */
    public static Set<String> getKeys() {
        return TranslationProvider.getInstance().getTranslationService().getKeys();
    }

    /**
     * Set the default/fallback language
     *
     * @param language The language code like <a href="https://minecraft.gamepedia.com/Language">Minecraft Wiki</a> (e.g., "en_US", "xx_XX")
     */
    public static void setDefaultLanguage(@NotNull String language) {
        TranslationProvider.getInstance().getTranslationService().setDefaultLanguage(language);
    }

    /**
     * Get the default/fallback language
     */
    public static String getDefaultLanguage() {
        return TranslationProvider.getInstance().getTranslationService().getDefaultLanguage();
    }

    /**
     * Set the active language
     *
     * @param language The language code like <a href="https://minecraft.gamepedia.com/Language">Minecraft Wiki</a> (e.g., "en_US", "xx_XX")
     */
    public static void setLanguage(@NotNull String language) {
        TranslationProvider.getInstance().getTranslationService().setLanguage(language);
    }

    /**
     * Get the active language
     */
    public static String getLanguage() {
        return TranslationProvider.getInstance().getTranslationService().getLanguage();
    }

    /**
     * Initialize WordWeaver
     *
     * @implSpec This method will initialize the translation system with the provided configuration. WordWeaver will throw exceptions if it has not been initialized before usage.
     */
    public static void initialize(@NotNull TranslationConfig config) {
        final LanguageRegistry registry = new LanguageRegistryImpl(config);
        final TranslationLoader loader = new JsonTranslationLoader(config, registry);
        final TranslationService service = new TranslationServiceImpl(config, registry, loader);

        // Initialize provider
        TranslationProvider.initialize(service);

        LOGGER.debug("Initialized WordWeaver with current language: {}, and fallback language: {}", config.getCurrentLanguage(), config.getDefaultLanguage());
    }

    /**
     * Reload all translations
     */
    public static void reload() {
        TranslationProvider.getInstance().getTranslationService().reload();
    }
}