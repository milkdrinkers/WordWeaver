package io.github.milkdrinkers.wordweaver;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundle;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class DefaultMissingTranslationHandler implements MissingTranslationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMissingTranslationHandler.class);
    private final Function<@Nullable String, String> stringResultHandler = (fallback) -> fallback == null ? "" : fallback;
    private final Function<@Nullable List<String>, List<String>> stringResultHandler2 = (fallback) -> fallback == null ? Collections.emptyList() : fallback;
    private final Function<@Nullable Component, Component> componentResultHandler = (fallback) -> fallback == null ? Component.empty() : fallback;
    private final Function<@Nullable List<Component>, List<Component>> componentResultHandler2 = (fallback) -> fallback == null ? Collections.emptyList() : fallback;

    DefaultMissingTranslationHandler() {
    }

    @Override
    public @Nullable String handle(TranslationConfig config, TranslationBundleRegistry registry, String key, @Nullable String fallback) {
        LOGGER.debug("Missing translation for key: '{}' in locale: '{}'", key, config.getCurrentLocaleTag());

        final Optional<TranslationBundle> bundle = registry.getDefaultOptional();
        if (!bundle.isPresent())
            return stringResultHandler.apply(fallback);

        final Optional<TranslationBundleEntry> value = bundle.get().getEntryOptional(key);
        if (!value.isPresent())
            return stringResultHandler.apply(fallback);

        return value.get().getValue();
    }

    @Override
    public @Nullable Component handle(TranslationConfig config, TranslationBundleRegistry registry, String key, @Nullable Component fallback) {
        LOGGER.debug("Missing translation for key: '{}' in locale: '{}'", key, config.getCurrentLocaleTag());

        final Optional<TranslationBundle> bundle = registry.getDefaultOptional();
        if (!bundle.isPresent())
            return componentResultHandler.apply(fallback);

        final Optional<TranslationBundleEntry> value = bundle.get().getEntryOptional(key);
        if (!value.isPresent())
            return componentResultHandler.apply(fallback);

        return config.getComponentConverter().apply(value.get().getValue());
    }

    @Override
    public @Nullable List<String> handleListString(TranslationConfig config, TranslationBundleRegistry registry, String key, @Nullable List<String> fallback) {
        LOGGER.debug("Missing translation for key: '{}' in locale: '{}'", key, config.getCurrentLocaleTag());

        final Optional<TranslationBundle> bundle = registry.getDefaultOptional();
        if (!bundle.isPresent())
            return stringResultHandler2.apply(fallback);

        final Optional<TranslationBundleEntry> value = bundle.get().getEntryOptional(key);
        if (!value.isPresent())
            return stringResultHandler2.apply(fallback);

        return value.get().getValues();
    }

    @SuppressWarnings("OptionalIsPresent")
    @Override
    public @Nullable List<Component> handleListComponent(TranslationConfig config, TranslationBundleRegistry registry, String key, @Nullable List<Component> fallback) {
        LOGGER.debug("Missing translation for key: '{}' in locale: '{}'", key, config.getCurrentLocaleTag());

        final Optional<TranslationBundle> bundle = registry.getDefaultOptional();
        if (!bundle.isPresent())
            return componentResultHandler2.apply(fallback);

        final Optional<TranslationBundleEntry> value = bundle.get().getEntryOptional(key);
        if (!value.isPresent())
            return componentResultHandler2.apply(fallback);

        return value.get().getValues().stream()
            .map(s -> config.getComponentConverter().apply(s))
            .collect(Collectors.toList());
    }
}
