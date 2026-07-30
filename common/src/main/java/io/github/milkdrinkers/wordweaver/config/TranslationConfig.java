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

import static io.github.milkdrinkers.wordweaver.LocaleUtil.*;

/**
 * Configuration for WordWeaver
 */
public class TranslationConfig {
    public static final Locale DEFAULT_LANG = Locale.US;

    // Configuration
    private @KeyPattern.Namespace String namespace;
    private Path languagesDirectory;
    private Locale defaultLanguage;
    private Locale currentLanguage;

    private Path resourcesDirectory;
    private boolean extractLanguages;
    private boolean updateLanguages;

    // Behavior
    private MissingTranslationHandler missingTranslationHandler;
    private Function<String, Component> componentConverter;

    private TranslationConfig() {
        this.namespace = "";
        this.languagesDirectory = null;
        this.defaultLanguage = DEFAULT_LANG;
        this.currentLanguage = defaultLanguage;

        this.resourcesDirectory = Paths.get("lang");
        this.extractLanguages = true;
        this.updateLanguages = true;

        this.missingTranslationHandler = MissingTranslationHandler.DEFAULT;
        this.componentConverter = Component::text;
    }

    public @KeyPattern.Namespace String getNamespace() {
        return namespace;
    }

    public void setNamespace(@KeyPattern.Namespace String namespace) {
        this.namespace = namespace;
    }

    public Path getLanguagesDirectory() {
        return languagesDirectory;
    }

    public String getDefaultLanguage() {
        return deserialize(defaultLanguage);
    }

    public Locale getDefaultLocale() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(Locale defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getCurrentLanguage() {
        return deserialize(currentLanguage);
    }

    public Locale getCurrentLocale() {
        return currentLanguage;
    }

    public void setCurrentLanguage(Locale currentLanguage) {
        this.currentLanguage = currentLanguage;
    }

    public Path getResourcesDirectory() {
        return resourcesDirectory;
    }

    public boolean shouldExtractLanguages() {
        return extractLanguages;
    }

    public boolean shouldUpdateLanguages() {
        return updateLanguages;
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
         * Set the directory where translation files are located.
         *
         * @param directory The directory to use
         */
        public Builder translationDirectory(Path directory) {
            config.languagesDirectory = directory;
            return this;
        }

        /**
         * Sets the default language to use, this is used as a fallback if a key cannot be found in the requested language.
         *
         * @param language The <a href="https://gist.github.com/typpo/b2b828a35e683b9bf8db91b5404f1bd1">BCP 47 Language Code</a> (e.g., "en-US", "xx-XX", "en_US", "xx_XX"), representing a language.
         * @implNote Defaults to {@code en_US}
         * @see #defaultLanguage(Locale)
         */
        public Builder defaultLanguage(String language) {
            return defaultLanguage(serialize(language));
        }

        /**
         * Sets the default language to use by specifying a Locale, this is used as a fallback if a key cannot be found in the requested language.
         *
         * @param locale A Locale representing a language (e.g., {@code Locale.forLanguageTag("en-US")}, {@code new Locale("en", "US")}, {@code new Locale("xx", "XX")}).
         * @implNote Defaults to {@code Locale.US}
         */
        public Builder defaultLanguage(Locale locale) {
            config.defaultLanguage = locale;
            return this;
        }

        /**
         * Sets the language to use
         *
         * @param language The <a href="https://gist.github.com/typpo/b2b828a35e683b9bf8db91b5404f1bd1">BCP 47 Language Code</a> (e.g., "en-US", "xx-XX", "en_US", "xx_XX"), representing a language.
         * @implNote Defaults to the value of {@link #defaultLanguage(String)}
         * @see #language(Locale)
         */
        public Builder language(String language) {
            return language(serialize(language));
        }

        /**
         * Sets the language to use by specifying a Locale
         *
         * @param locale A Locale representing the language to use (e.g., {@code Locale.forLanguageTag("en-US")}, {@code new Locale("en", "US")}, {@code new Locale("xx", "XX")}).
         * @implNote Defaults to the value of {@link #defaultLanguage(Locale)}
         */
        public Builder language(Locale locale) {
            config.currentLanguage = locale;
            return this;
        }

        /**
         * Set the subdirectory where language files are located in the resources directory.
         *
         * @param path Relative path to the subdirectory where language files are located.
         * @implNote Defaults to {@code lang}. This defines where the language files shipped with your program are located.
         */
        public Builder resourcesDirectory(Path path) {
            config.resourcesDirectory = path;
            return this;
        }

        /**
         * Set whether to extract missing language files to the languages directory
         *
         * @param extract Whether to extract missing language files
         * @implNote Defaults to true
         */
        public Builder extractLanguages(boolean extract) {
            config.extractLanguages = extract;
            return this;
        }

        /**
         * Set whether to add missing keys to existing language files
         *
         * @param update Whether to add missing keys to existing language files
         * @implNote Defaults to true
         */
        public Builder updateLanguages(boolean update) {
            config.updateLanguages = update;
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

            if (config.languagesDirectory == null)
                throw new IllegalStateException("Translation directory must be set");

            if (config.defaultLanguage == null)
                config.defaultLanguage = DEFAULT_LANG;

            if (config.currentLanguage == null)
                config.currentLanguage = config.defaultLanguage;

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