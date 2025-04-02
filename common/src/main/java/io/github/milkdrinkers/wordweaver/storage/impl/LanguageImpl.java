package io.github.milkdrinkers.wordweaver.storage.impl;

import io.github.milkdrinkers.wordweaver.storage.Language;
import io.github.milkdrinkers.wordweaver.storage.LanguageEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageImpl implements Language {
    private final String languageName;
    private final ConcurrentHashMap<String, LanguageEntry> translations;

    public LanguageImpl(final String languageName, final Map<String, LanguageEntry> translations) {
        this.languageName = languageName;
        this.translations = new ConcurrentHashMap<>(translations);
    }

    @Override
    public String getName() {
        return languageName;
    }

    @Override
    public Map<String, LanguageEntry> get() {
        return translations;
    }

    @Override
    public @Nullable LanguageEntry get(String key) {
        return translations.get(key);
    }

    @Override
    public Optional<LanguageEntry> getOptional(String key) {
        return Optional.ofNullable(get(key));
    }

    @Override
    public boolean has(String key) {
        return translations.containsKey(key);
    }

    @Override
    public void add(String key, LanguageEntry entry) {
        translations.put(key, entry);
    }

    @Override
    public void remove(String key) {
        translations.remove(key);
    }

    @Override
    public Set<String> keys() {
        return Collections.unmodifiableSet(translations.keySet());
    }
}
