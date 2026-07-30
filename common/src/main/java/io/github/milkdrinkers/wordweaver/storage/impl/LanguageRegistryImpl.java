package io.github.milkdrinkers.wordweaver.storage.impl;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.storage.Language;
import io.github.milkdrinkers.wordweaver.storage.LanguageRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class LanguageRegistryImpl implements LanguageRegistry {
    private final AtomicReference<Map<Locale, Language>> languages;
    private final AtomicReference<Set<String>> keys;
    private final TranslationConfig config;

    private final AtomicReference<Language> currentLanguage;
    private final AtomicReference<Language> defaultLanguage;

    public LanguageRegistryImpl(TranslationConfig config) {
        this.config = config;
        this.languages = new AtomicReference<>(Collections.unmodifiableMap(new HashMap<>()));
        this.keys = new AtomicReference<>(Collections.unmodifiableSet(new HashSet<>()));
        this.currentLanguage = new AtomicReference<>(null);
        this.defaultLanguage = new AtomicReference<>(null);
    }

    @Override
    public @Nullable Language get(Locale locale) {
        return languages.get().get(locale);
    }

    @Override
    public Optional<Language> getOptional(Locale locale) {
        return Optional.ofNullable(get(locale));
    }

    @Override
    public @Nullable Language getCurrent() {
        return currentLanguage.get();
    }

    @Override
    public Optional<Language> getCurrentOptional() {
        return Optional.ofNullable(getCurrent());
    }

    @Override
    public @Nullable Language getDefault() {
        return defaultLanguage.get();
    }

    @Override
    public Optional<Language> getDefaultOptional() {
        return Optional.ofNullable(getDefault());
    }

    @Override
    public Set<Locale> getRegistered() {
        return Collections.unmodifiableSet(languages.get().keySet());
    }

    @Override
    public boolean isRegistered(Locale locale) {
        return languages.get().containsKey(locale);
    }

    @Override
    public Set<String> getKeys() {
        return keys.get();
    }

    @Override
    public void register(Language language) {
        // Update languages map
        final Map<Locale, Language> updatedLanguages = new HashMap<>(languages.get());
        updatedLanguages.putIfAbsent(language.getLocale(), language);
        languages.set(Collections.unmodifiableMap(updatedLanguages));

        // Update keys map
        final Set<String> updatedKeys = new HashSet<>(getKeys());

        // Cache the new language ref
        final Language newLanguage = updatedLanguages.get(language.getLocale());

        // Update current language if necessary
        if (currentLanguage.get() == null && language.getLocale().equals(config.getCurrentLocale())) {
            currentLanguage.set(newLanguage);

            if (newLanguage != null)
                updatedKeys.addAll(newLanguage.keys());
        }

        // Update default language if necessary
        if (defaultLanguage.get() == null && language.getLocale().equals(config.getDefaultLocale())) {
            defaultLanguage.set(newLanguage);

            if (newLanguage != null)
                updatedKeys.addAll(newLanguage.keys());
        }

        keys.set(Collections.unmodifiableSet(updatedKeys));
    }

    @Override
    public void clear() {
        currentLanguage.set(null);
        defaultLanguage.set(null);
        keys.set(Collections.unmodifiableSet(new HashSet<>()));
        languages.set(Collections.unmodifiableMap(new HashMap<>()));
    }
}
