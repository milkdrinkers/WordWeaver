package io.github.milkdrinkers.wordweaver.service.impl;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.TranslationLoader;
import io.github.milkdrinkers.wordweaver.service.TranslationService;
import io.github.milkdrinkers.wordweaver.storage.Language;
import io.github.milkdrinkers.wordweaver.storage.LanguageRegistry;
import io.github.milkdrinkers.wordweaver.storage.LanguageEntry;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslationServiceImpl implements TranslationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationServiceImpl.class);
    private final TranslationConfig config;
    private final LanguageRegistry registry;
    private final TranslationLoader loader;

    public TranslationServiceImpl(TranslationConfig config, LanguageRegistry registry, TranslationLoader loader) {
        this.config = config;
        this.registry = registry;
        this.loader = loader;

        // Initialize translations
        initialize();
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
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, @Nullable String fallback) {
        final Language language = registry.getCurrent();
        if (language == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, fallback);

        final LanguageEntry value = language.get(key);
        if (value == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, fallback);

        return value.getValue();
    }

    @Override
    public List<String> getStringList(String key) {
        return getStringList(key, null);
    }

    @Override
    public List<String> getStringList(String key, List<String> fallback) {
        final Language language = registry.getCurrent();
        if (language == null)
            return config.getMissingTranslationHandler().handleListString(config, registry, key, fallback);

        final LanguageEntry value = language.get(key);
        if (value == null)
            return config.getMissingTranslationHandler().handleListString(config, registry, key, fallback);

        return value.getValues();
    }

    @Override
    public Component getComponent(String key) {
        return getComponent(key, null);
    }

    @Override
    public Component getComponent(String key, Component fallback) {
        final Language language = registry.getCurrent();
        if (language == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, (Component) null);

        final LanguageEntry value = language.get(key);
        if (value == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, (Component) null);

        return config.getComponentConverter().apply(value.getValue());
    }

    @Override
    public List<Component> getComponentList(String key) {
        return getComponentList(key, null);
    }

    @Override
    public List<Component> getComponentList(String key, List<Component> fallback) {
        final Language language = registry.getCurrent();
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
        final Language language1 = registry.getCurrent();
        final Language language2 = registry.getDefault();

        final Set<String> entries = new HashSet<>();
        if (language1 != null)
            entries.addAll(language1.keys());
        if (language2 != null)
            entries.addAll(language2.keys());

        return entries; // TODO Cache this in registry
    }

    @Override
    public void setDefaultLanguage(String language) {
        config.setDefaultLanguage(language);
    }

    @Override
    public String getDefaultLanguage() {
        return config.getDefaultLanguage();
    }

    @Override
    public void setLanguage(String language) {
        config.setCurrentLanguage(language);
    }

    @Override
    public String getLanguage() {
        return config.getCurrentLanguage();
    }

    @Override
    public void reload() {
        registry.clear();
        initialize();
    }
}