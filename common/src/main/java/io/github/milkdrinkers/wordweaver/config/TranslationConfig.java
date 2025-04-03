package io.github.milkdrinkers.wordweaver.config;

import io.github.milkdrinkers.wordweaver.MissingTranslationHandler;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * Configuration for WordWeaver
 */
public class TranslationConfig {
    public static final String DEFAULT_LANG = "en_US";
    
    // Configuration
    private Path languagesDirectory;
    private String defaultLanguage;
    private String currentLanguage;

    private Path resourcesDirectory;
    private boolean extractLanguages;
    private boolean updateLanguages;

    // Behavior
    private MissingTranslationHandler missingTranslationHandler;
    private Function<String, Component> componentConverter;

    private TranslationConfig() {
        this.languagesDirectory = null;
        this.defaultLanguage = DEFAULT_LANG;
        this.currentLanguage = defaultLanguage;

        this.resourcesDirectory = Paths.get("lang");
        this.extractLanguages = true;
        this.updateLanguages = true;

        this.missingTranslationHandler = MissingTranslationHandler.DEFAULT;
        this.componentConverter = Component::text;
    }

    public Path getLanguagesDirectory() {
        return languagesDirectory;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setCurrentLanguage(String currentLanguage) {
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
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for TranslationConfig
     */
    public static class Builder {
        private final TranslationConfig config = new TranslationConfig();

        private Builder() {}

        /**
         * Set the directory where translation files are located.
         * @param directory The directory to use
         */
        public Builder translationDirectory(Path directory) {
            config.languagesDirectory = directory;
            return this;
        }

        /**
         * Sets the default language to use, this is used as a fallback if a key cannot be found in the requested language.
         * @param language The language code like <a href="https://minecraft.gamepedia.com/Language">Minecraft Wiki</a> (e.g., "en_US", "xx_XX").
         * @implNote Defaults to {@code en_US}
         */
        public Builder defaultLanguage(String language) {
            config.defaultLanguage = language;
            return this;
        }

        /**
         * Sets the language to use
         * @param language The language code like <a href="https://minecraft.gamepedia.com/Language">Minecraft Wiki</a> (e.g., "en_US", "xx_XX").
         * @implNote Defaults to the value of {@link #defaultLanguage(String)}
         */
        public Builder language(String language) {
            config.currentLanguage = language;
            return this;
        }

        /**
         * Set the subdirectory where language files are located in the resources directory.
         * @param path Relative path to the subdirectory where language files are located.
         * @implNote Defaults to {@code lang}. This defines where the language files shipped with your program are located.
         */
        public Builder resourcesDirectory(Path path) {
            config.resourcesDirectory = path;
            return this;
        }

        /**
         * Set whether to extract missing language files to the languages directory
         * @param extract Whether to extract missing language files
         * @implNote Defaults to true
         */
        public Builder extractLanguages(boolean extract) {
            config.extractLanguages = extract;
            return this;
        }

        /**
         * Set whether to add missing keys to existing language files
         * @param update Whether to add missing keys to existing language files
         * @implNote Defaults to true
         */
        public Builder updateLanguages(boolean update) {
            config.updateLanguages = update;
            return this;
        }

        /**
         * Set the handler for missing translations.
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
            if (config.languagesDirectory == null)
                throw new IllegalStateException("Translation directory must be set");

            if (config.defaultLanguage == null || config.defaultLanguage.isEmpty())
                config.defaultLanguage = DEFAULT_LANG;

            if (config.currentLanguage == null || config.currentLanguage.isEmpty())
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