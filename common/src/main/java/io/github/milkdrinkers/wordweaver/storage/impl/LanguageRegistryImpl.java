package io.github.milkdrinkers.wordweaver.storage.impl;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.storage.Language;
import io.github.milkdrinkers.wordweaver.storage.LanguageRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageRegistryImpl implements LanguageRegistry {
    private final ConcurrentHashMap<String, Language> languages;
    private final Set<String> keys;
    private final TranslationConfig config;

    private Language currentLanguage;
    private Language defaultLanguage;

    public LanguageRegistryImpl(TranslationConfig config) {
        this.config = config;
        this.languages = new ConcurrentHashMap<>();
        this.keys = ConcurrentHashMap.newKeySet();
    }

    @Override
    public @Nullable Language get(String name) {
        return languages.get(name);
    }

    @Override
    public Optional<Language> getOptional(String name) {
        return Optional.ofNullable(get(name));
    }

    @Override
    public @Nullable Language getCurrent() {
        return currentLanguage;
    }

    @Override
    public Optional<Language> getCurrentOptional() {
        return Optional.ofNullable(getCurrent());
    }

    @Override
    public @Nullable Language getDefault() {
        return defaultLanguage;
    }

    @Override
    public Optional<Language> getDefaultOptional() {
        return Optional.ofNullable(getDefault());
    }

    @Override
    public Set<String> getRegistered() {
        return Collections.unmodifiableSet(languages.keySet());
    }

    @Override
    public boolean isRegistered(String name) {
        return languages.containsKey(name);
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(keys);
    }

    @Override
    public void register(Language language) {
        languages.putIfAbsent(language.getName(), language);

        if (currentLanguage == null && language.getName().equals(config.getCurrentLanguage())) {
            currentLanguage = get(config.getCurrentLanguage());

            if (currentLanguage != null)
                keys.addAll(currentLanguage.keys());
        }

        if (defaultLanguage == null && language.getName().equals(config.getDefaultLanguage())) {
            defaultLanguage = get(config.getDefaultLanguage());

            if (defaultLanguage != null)
                keys.addAll(defaultLanguage.keys());
        }
    }

    @Override
    public void clear() {
        currentLanguage = null;
        defaultLanguage = null;
        keys.clear();
        languages.clear();
    }
}
