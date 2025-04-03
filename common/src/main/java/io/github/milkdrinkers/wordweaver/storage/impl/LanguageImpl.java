package io.github.milkdrinkers.wordweaver.storage.impl;

import io.github.milkdrinkers.wordweaver.storage.Language;
import io.github.milkdrinkers.wordweaver.storage.LanguageEntry;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LanguageImpl implements Language {
    private final String languageName;
    private final Map<String, LanguageEntry> translations;

    public LanguageImpl(final String languageName, final Map<String, LanguageEntry> translations) {
        this.languageName = languageName;
        this.translations = Collections.unmodifiableMap(new HashMap<>(translations));
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
    public Set<String> keys() {
        return Collections.unmodifiableSet(translations.keySet());
    }
}
