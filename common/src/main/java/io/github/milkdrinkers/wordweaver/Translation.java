package io.github.milkdrinkers.wordweaver;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.TranslationLoader;
import io.github.milkdrinkers.wordweaver.loader.impl.DefaultTranslationLoader;
import io.github.milkdrinkers.wordweaver.service.TranslationService;
import io.github.milkdrinkers.wordweaver.service.impl.TranslationServiceImpl;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleRegistryImpl;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static io.github.milkdrinkers.wordweaver.LocaleUtil.fromTag;
import static io.github.milkdrinkers.wordweaver.LocaleUtil.toTag;

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
     * Get the translated string for a key
     *
     * @param key The key to the translation
     */
    public static String of(String key) {
        return of(key, null);
    }

    /**
     * Get the translated string for a key
     *
     * @param key      The key to the translation
     * @param fallback The default value to return if no valid value was found
     */
    public static String of(String key, String fallback) {
        return TranslationProvider.getInstance().getTranslationService().getString(key, fallback);
    }

    /**
     * Get the translated list of strings for a key
     *
     * @param key The key to the translation
     * @see #ofList(String)
     */
    public static List<String> ofList(String key) {
        return ofList(key, null);
    }

    /**
     * Get the translated list of strings for a key
     *
     * @param key      The key to the translation
     * @param fallback The default value to return if no valid value was found, empty list if null
     */
    public static List<String> ofList(String key, @Nullable List<String> fallback) {
        return TranslationProvider.getInstance().getTranslationService().getStringList(key, fallback != null ? fallback : Collections.emptyList());
    }

    /**
     * Get the translated value for a key as an Adventure Component
     *
     * @param key The key to the translation
     * @see Component
     */
    public static Component as(String key) {
        return as(key, null);
    }

    /**
     * Get the translated value for a key as an Adventure Component
     *
     * @param key      The key to the translation
     * @param fallback The default value to return if no valid value was found
     * @see Component
     */
    public static Component as(String key, Component fallback) {
        return TranslationProvider.getInstance().getTranslationService().getComponent(key, fallback);
    }

    /**
     * Get the translated list of Adventure Components for a key
     *
     * @param key The key to the translation
     * @see #asList(String, List)
     * @see Component
     */
    public static List<Component> asList(String key) {
        return asList(key, null);
    }

    /**
     * Get the translated list of Adventure Components for a key
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
     * Get a set of all entry keys in the current and fallback bundles
     *
     * @return A set of all keys in the current and fallback bundles
     */
    public static Set<String> getKeys() {
        return TranslationProvider.getInstance().getTranslationService().getKeys();
    }

    /**
     * Set the default/fallback locale
     *
     * @param localeTag The <a href="https://gist.github.com/typpo/b2b828a35e683b9bf8db91b5404f1bd1">BCP 47 locale tag</a> (e.g., "en-US", "xx-XX", "en_US", "xx_XX"), representing a locale.
     */
    public static void setDefaultLocale(@NotNull String localeTag) {
        setDefaultLocale(fromTag(localeTag));
    }

    /**
     * Set the default/fallback locale
     *
     * @param locale A Locale representing a locale (e.g., {@code Locale.forLanguageTag("en-US")}, {@code new Locale("en", "US")}, {@code new Locale("xx", "XX")}).
     */
    public static void setDefaultLocale(@NotNull Locale locale) {
        TranslationProvider.getInstance().getTranslationService().setDefaultLocale(locale);
    }

    /**
     * Get the default/fallback locale as a locale tag
     */
    public static String getDefaultLocaleTag() {
        return toTag(getDefaultLocale());
    }

    /**
     * Get the default/fallback locale
     */
    public static Locale getDefaultLocale() {
        return TranslationProvider.getInstance().getTranslationService().getDefaultLocale();
    }

    /**
     * Set the active locale
     *
     * @param localeTag The <a href="https://gist.github.com/typpo/b2b828a35e683b9bf8db91b5404f1bd1">BCP 47 locale tag</a> (e.g., "en-US", "xx-XX", "en_US", "xx_XX"), representing a locale.
     */
    public static void setLocale(@NotNull String localeTag) {
        setLocale(fromTag(localeTag));
    }

    /**
     * Set the active locale
     *
     * @param locale A Locale representing the locale to use (e.g., {@code Locale.forLanguageTag("en-US")}, {@code new Locale("en", "US")}, {@code new Locale("xx", "XX")}).
     */
    public static void setLocale(@NotNull Locale locale) {
        TranslationProvider.getInstance().getTranslationService().setLocale(locale);
    }

    /**
     * Get the active locale as a locale tag
     */
    public static String getLocaleTag() {
        return toTag(getLocale());
    }

    /**
     * Get the active locale
     */
    public static Locale getLocale() {
        return TranslationProvider.getInstance().getTranslationService().getLocale();
    }

    /**
     * Initialize WordWeaver
     *
     * @implSpec This method will initialize the translation system with the provided configuration. WordWeaver will throw exceptions if it has not been initialized before usage.
     */
    @SuppressWarnings("unused")
    public static void initialize(@NotNull TranslationConfig config) {
        final TranslationBundleRegistry registry = new TranslationBundleRegistryImpl(config);
        final TranslationLoader loader = new DefaultTranslationLoader(config, registry);
        final TranslationService service = new TranslationServiceImpl(config, registry, loader);

        // Initialize provider
        TranslationProvider.initialize(service);

        LOGGER.debug("Initialized WordWeaver with current locale: {}, and fallback locale: {}", config.getCurrentLocaleTag(), config.getDefaultLocaleTag());
    }

    /**
     * Reload all translations
     */
    @SuppressWarnings("unused")
    public static void reload() {
        TranslationProvider.getInstance().getTranslationService().reload();
    }
}
