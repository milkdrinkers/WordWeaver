package io.github.milkdrinkers.wordweaver.config;

import io.github.milkdrinkers.wordweaver.MissingTranslationHandler;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Function;

import static io.github.milkdrinkers.wordweaver.LocaleUtil.fromTag;
import static io.github.milkdrinkers.wordweaver.LocaleUtil.toTag;

/**
 * Configuration for WordWeaver
 */
public class TranslationConfig {
    public static final Locale DEFAULT_LOCALE = Locale.US;

    // Configuration
    private @KeyPattern.Namespace String namespace;
    private Path translationDirectory;
    private Locale defaultLocale;
    private Locale currentLocale;

    private Path resourcesDirectory;
    private boolean extractBundles;
    private boolean updateBundles;

    // Behavior
    private MissingTranslationHandler missingTranslationHandler;
    private Function<String, Component> componentConverter;

    private TranslationConfig() {
        this.namespace = "";
        this.translationDirectory = null;
        this.defaultLocale = DEFAULT_LOCALE;
        this.currentLocale = defaultLocale;

        this.resourcesDirectory = Paths.get("lang");
        this.extractBundles = true;
        this.updateBundles = true;

        this.missingTranslationHandler = MissingTranslationHandler.DEFAULT;
        this.componentConverter = Component::text;
    }

    public @KeyPattern.Namespace String getNamespace() {
        return namespace;
    }

    public void setNamespace(@KeyPattern.Namespace String namespace) {
        this.namespace = namespace;
    }

    public Path getTranslationDirectory() {
        return translationDirectory;
    }

    public String getDefaultLocaleTag() {
        return toTag(defaultLocale);
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public String getCurrentLocaleTag() {
        return toTag(currentLocale);
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(Locale currentLocale) {
        this.currentLocale = currentLocale;
    }

    public Path getResourcesDirectory() {
        return resourcesDirectory;
    }

    public boolean shouldExtractBundles() {
        return extractBundles;
    }

    public boolean shouldUpdateBundles() {
        return updateBundles;
    }

    public MissingTranslationHandler getMissingTranslationHandler() {
        return missingTranslationHandler;
    }

    public Function<String, Component> getComponentConverter() {
        return componentConverter;
    }

    /**
     * Creates a new builder for the TranslationConfig
     *
     * @return A new builder instance
     */
    @SuppressWarnings("unused")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for TranslationConfig
     */
    public static class Builder {
        private final TranslationConfig config = new TranslationConfig();

        private Builder() {
        }

        /**
         * Set the namespace of the implementing plugin/mod.
         * This is used to access translations through the {@link net.kyori.adventure.translation.GlobalTranslator}.
         *
         * @param namespace The namespace to use
         */
        public Builder namespace(@KeyPattern.Namespace String namespace) {
            config.namespace = namespace;
            return this;
        }

        /**
         * Set the directory where bundle files are located at runtime.
         *
         * @param directory The directory to use
         */
        public Builder translationDirectory(Path directory) {
            config.translationDirectory = directory;
            return this;
        }

        /**
         * Sets the default locale to use, this is used as a fallback if a key cannot be found in the requested locale.
         *
         * @param localeTag The <a href="https://gist.github.com/typpo/b2b828a35e683b9bf8db91b5404f1bd1">BCP 47 locale tag</a> (e.g., "en-US", "xx-XX", "en_US", "xx_XX"), representing a locale.
         * @implNote Defaults to {@code en_US}
         * @see #defaultLocale(Locale)
         */
        public Builder defaultLocale(String localeTag) {
            return defaultLocale(fromTag(localeTag));
        }

        /**
         * Sets the default locale to use, this is used as a fallback if a key cannot be found in the requested locale.
         *
         * @param locale A Locale representing a locale (e.g., {@code Locale.forLanguageTag("en-US")}, {@code new Locale("en", "US")}, {@code new Locale("xx", "XX")}).
         * @implNote Defaults to {@code Locale.US}
         */
        public Builder defaultLocale(Locale locale) {
            config.defaultLocale = locale;
            return this;
        }

        /**
         * Sets the locale to use
         *
         * @param localeTag The <a href="https://gist.github.com/typpo/b2b828a35e683b9bf8db91b5404f1bd1">BCP 47 locale tag</a> (e.g., "en-US", "xx-XX", "en_US", "xx_XX"), representing a locale.
         * @implNote Defaults to the value of {@link #defaultLocale(String)}
         * @see #locale(Locale)
         */
        public Builder locale(String localeTag) {
            return locale(fromTag(localeTag));
        }

        /**
         * Sets the locale to use by specifying a Locale
         *
         * @param locale A Locale representing the locale to use (e.g., {@code Locale.forLanguageTag("en-US")}, {@code new Locale("en", "US")}, {@code new Locale("xx", "XX")}).
         * @implNote Defaults to the value of {@link #defaultLocale(Locale)}
         */
        public Builder locale(Locale locale) {
            config.currentLocale = locale;
            return this;
        }

        /**
         * Set the subdirectory where bundle files are located in the resources directory.
         *
         * @param path Relative path to the subdirectory where bundle files are located.
         * @implNote Defaults to {@code lang}. This defines where the bundle files shipped with your program are located.
         */
        public Builder resourcesDirectory(Path path) {
            config.resourcesDirectory = path;
            return this;
        }

        /**
         * Set whether to extract missing bundle files to the translation directory
         *
         * @param extract Whether to extract missing bundle files
         * @implNote Defaults to true
         */
        public Builder extractBundles(boolean extract) {
            config.extractBundles = extract;
            return this;
        }

        /**
         * Set whether to add missing entries to existing bundle files
         *
         * @param update Whether to add missing entries to existing bundle files
         * @implNote Defaults to true
         */
        public Builder updateBundles(boolean update) {
            config.updateBundles = update;
            return this;
        }

        /**
         * Set the handler for missing translations.
         *
         * @param handler The handler to use for missing translations
         * @implNote Defaults to {@link MissingTranslationHandler#DEFAULT}
         * @see MissingTranslationHandler
         */
        public Builder missingTranslationHandler(MissingTranslationHandler handler) {
            config.missingTranslationHandler = handler;
            return this;
        }

        /**
         * Set the function used to convert strings to components
         *
         * @param converter The function to convert a string to a Component.
         * @implNote Defaults to {@link Component#text(String)}
         */
        public Builder componentConverter(Function<String, Component> converter) {
            config.componentConverter = converter;
            return this;
        }

        /**
         * Builds the TranslationConfig object
         *
         * @return The configured TranslationConfig
         */
        public TranslationConfig build() {
            if (config.namespace == null || config.namespace.isEmpty())
                throw new IllegalStateException("Namespace must be set");

            try {
                // noinspection PatternValidation
                Key.key(config.getNamespace());
            } catch (InvalidKeyException e) {
                throw new IllegalStateException("Namespace is invalid, we recommend \"wordweaver:pluginname\"", e);
            }

            if (config.translationDirectory == null)
                throw new IllegalStateException("Translation directory must be set");

            if (config.defaultLocale == null)
                config.defaultLocale = DEFAULT_LOCALE;

            if (config.currentLocale == null)
                config.currentLocale = config.defaultLocale;

            if (config.resourcesDirectory == null)
                config.resourcesDirectory = Paths.get("lang");

            if (config.resourcesDirectory.isAbsolute())
                throw new IllegalStateException("Resources directory must be relative");

            if (config.missingTranslationHandler == null)
                config.missingTranslationHandler = MissingTranslationHandler.DEFAULT;

            if (config.componentConverter == null)
                config.componentConverter = Component::text;

            return config;
        }
    }
}
