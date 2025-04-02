package io.github.milkdrinkers.wordweaver.config;

import io.github.milkdrinkers.wordweaver.MissingTranslationHandler;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * Configuration for WordWeaver
 */
public class TranslationConfig {
    public static final String DEFAULT_LANG = "en_US";
    
    // Configuration
    private Path translationDirectory;
    private String defaultLanguage;
    private String currentLanguage;
    private boolean updateLanguages;

    // Behavior
    private MissingTranslationHandler missingTranslationHandler;
    private Function<String, Component> componentConverter;

    private TranslationConfig() {
        this.translationDirectory = null;
        this.defaultLanguage = DEFAULT_LANG;
        this.currentLanguage = defaultLanguage;
        this.updateLanguages = true;

        this.missingTranslationHandler = MissingTranslationHandler.DEFAULT;
        this.componentConverter = Component::text;
    }

    public Path getTranslationDirectory() {
        return translationDirectory;
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

    public boolean updateLanguages() {
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
            config.translationDirectory = directory;
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
            if (config.translationDirectory == null)
                throw new IllegalStateException("Translation directory must be set");

            if (config.defaultLanguage == null || config.defaultLanguage.isEmpty())
                config.defaultLanguage = DEFAULT_LANG;

            if (config.currentLanguage == null || config.currentLanguage.isEmpty())
                config.currentLanguage = config.defaultLanguage;

            if (config.missingTranslationHandler == null)
                config.missingTranslationHandler = MissingTranslationHandler.DEFAULT;

            if (config.componentConverter == null)
                config.componentConverter = Component::text;

            return config;
        }
    }
}