package io.github.milkdrinkers.wordweaver.storage.impl;

import io.github.milkdrinkers.wordweaver.storage.TranslationBundle;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TranslationBundleImpl implements TranslationBundle {
    private final Locale locale;
    private final Map<String, TranslationBundleEntry> entries;

    public TranslationBundleImpl(final Locale locale, final Map<String, TranslationBundleEntry> entries) {
        this.locale = locale;
        this.entries = Collections.unmodifiableMap(new HashMap<>(entries));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Map<String, TranslationBundleEntry> getEntries() {
        return entries;
    }

    @Override
    public @Nullable TranslationBundleEntry getEntry(String key) {
        return entries.get(key);
    }

    @Override
    public Optional<TranslationBundleEntry> getEntryOptional(String key) {
        return Optional.ofNullable(getEntry(key));
    }

    @Override
    public boolean hasEntry(String key) {
        return entries.containsKey(key);
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(entries.keySet());
    }
}
