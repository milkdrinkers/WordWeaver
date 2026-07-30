package io.github.milkdrinkers.wordweaver.service.impl;

import io.github.milkdrinkers.wordweaver.TranslatorImpl;
import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.TranslationLoader;
import io.github.milkdrinkers.wordweaver.service.TranslationService;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundle;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
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
    private final TranslationBundleRegistry registry;
    private final TranslationLoader loader;
    private Translator translator;

    public TranslationServiceImpl(TranslationConfig config, TranslationBundleRegistry registry, TranslationLoader loader) {
        this.config = config;
        this.registry = registry;
        this.loader = loader;

        // Initialize bundles
        initialize();

        translator = new TranslatorImpl(config);
        GlobalTranslator.translator().addSource(translator);
    }

    private void initialize() {
        try {
            // Extract, update and load bundles
            if (config.shouldExtractBundles())
                loader.extractMissingBundles();
            if (config.shouldUpdateBundles())
                loader.updateExistingBundles();
            loader.loadBundles();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize translation service", e);
        }
    }

    @Override
    public String getString(Locale locale, String key, @Nullable String fallback) {
        final TranslationBundle bundle = registry.get(locale);
        if (bundle == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, fallback);

        final TranslationBundleEntry value = bundle.getEntry(key);
        if (value == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, fallback);

        return value.getValue();
    }

    @Override
    public List<String> getStringList(Locale locale, String key, List<String> fallback) {
        final TranslationBundle bundle = registry.get(locale);
        if (bundle == null)
            return config.getMissingTranslationHandler().handleListString(config, registry, key, fallback);

        final TranslationBundleEntry value = bundle.getEntry(key);
        if (value == null)
            return config.getMissingTranslationHandler().handleListString(config, registry, key, fallback);

        return value.getValues();
    }

    @Override
    public Component getComponent(Locale locale, String key, Component fallback) {
        final TranslationBundle bundle = registry.get(locale);
        if (bundle == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, (Component) null);

        final TranslationBundleEntry value = bundle.getEntry(key);
        if (value == null)
            return config.getMissingTranslationHandler().handle(config, registry, key, (Component) null);

        return config.getComponentConverter().apply(value.getValue());
    }

    @Override
    public List<Component> getComponentList(Locale locale, String key, List<Component> fallback) {
        final TranslationBundle bundle = registry.get(locale);
        if (bundle == null)
            return config.getMissingTranslationHandler().handleListComponent(config, registry, key, null);

        final TranslationBundleEntry value = bundle.getEntry(key);
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
    public void setDefaultLocale(Locale locale) {
        config.setDefaultLocale(locale);
    }

    @Override
    public Locale getDefaultLocale() {
        return config.getDefaultLocale();
    }

    @Override
    public void setLocale(Locale locale) {
        config.setCurrentLocale(locale);
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
            loader.loadBundles();
            translator = new TranslatorImpl(config);
            GlobalTranslator.translator().addSource(translator);
        } catch (Exception e) {
            LOGGER.error("Failed to reload translation service", e);
        }
    }
}
