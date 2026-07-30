package io.github.milkdrinkers.wordweaver.service.impl;

import io.github.milkdrinkers.wordweaver.TranslatorImpl;
import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.TranslationLoader;
import io.github.milkdrinkers.wordweaver.service.TranslationService;
import io.github.milkdrinkers.wordweaver.storage.Language;
import io.github.milkdrinkers.wordweaver.storage.LanguageEntry;
import io.github.milkdrinkers.wordweaver.storage.LanguageRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslationServiceImpl implements TranslationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationServiceImpl.class);
    private final TranslationConfig config;
    private final LanguageRegistry registry;
    private final TranslationLoader loader;
    private Translator translator;

    public TranslationServiceImpl(TranslationConfig config, LanguageRegistry registry, TranslationLoader loader) {
        this.config = config;
        this.registry = registry;
        this.loader = loader;

        // Initialize translations
        initialize();

        translator = new TranslatorImpl(config);
        GlobalTranslator.translator().addSource(translator);
    }

    private void initialize() {
        try {
            // Extract, update and load translations
            if (config.shouldExtractLanguages())
                loader.extractMissingLanguages();
            if (config.shouldUpdateLanguages())
                loader.updateExistingLanguages();
            loader.loadLanguages();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize translation service", e);
        }
    }

    @Override
    public String getString(Locale locale, String key, @Nullable String fallback) {
        final Language language = registry.get(locale);
        if (language == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, fallback);

        final LanguageEntry value = language.get(key);
        if (value == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, fallback);

        return value.getValue();
    }

    @Override
    public List<String> getStringList(Locale locale, String key, List<String> fallback) {
        final Language language = registry.get(locale);
        if (language == null)
            return config.getMissingTranslationHandler().handleListString(config, registry, key, fallback);

        final LanguageEntry value = language.get(key);
        if (value == null)
            return config.getMissingTranslationHandler().handleListString(config, registry, key, fallback);

        return value.getValues();
    }

    @Override
    public Component getComponent(Locale locale, String key, Component fallback) {
        final Language language = registry.get(locale);
        if (language == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, (Component) null);

        final LanguageEntry value = language.get(key);
        if (value == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, (Component) null);

        return config.getComponentConverter().apply(value.getValue());
    }

    @Override
    public List<Component> getComponentList(Locale locale, String key, List<Component> fallback) {
        final Language language = registry.get(locale);
        if (language == null)
            return config.getMissingTranslationHandler().handleListComponent(config, registry, key, null);

        final LanguageEntry value = language.get(key);
        if (value == null)
            return config.getMissingTranslationHandler().handleListComponent(config, registry, key, null);

        return value.getValues().stream()
            .map(s -> config.getComponentConverter().apply(s))
            .collect(Collectors.toList());
    }

    @Override
    public Set<String> getKeys() {
        return registry.getKeys();
    }

    @Override
    public void setDefaultLocale(Locale language) {
        config.setDefaultLanguage(language);
    }

    @Override
    public Locale getDefaultLocale() {
        return config.getDefaultLocale();
    }

    @Override
    public void setLocale(Locale language) {
        config.setCurrentLanguage(language);
    }

    @Override
    public Locale getLocale() {
        return config.getCurrentLocale();
    }

    @Override
    public void reload() {
        try {
            GlobalTranslator.translator().removeSource(translator);
            registry.clear();
            loader.loadLanguages();
            translator = new TranslatorImpl(config);
            GlobalTranslator.translator().addSource(translator);
        } catch (Exception e) {
            LOGGER.error("Failed to reload translation service", e);
        }
    }
}