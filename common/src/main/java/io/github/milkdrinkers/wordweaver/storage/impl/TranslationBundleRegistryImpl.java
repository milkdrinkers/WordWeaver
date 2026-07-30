package io.github.milkdrinkers.wordweaver.storage.impl;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundle;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TranslationBundleRegistryImpl implements TranslationBundleRegistry {
    private final AtomicReference<Map<Locale, TranslationBundle>> bundles;
    private final AtomicReference<Set<String>> keys;
    private final TranslationConfig config;

    private final AtomicReference<TranslationBundle> currentBundle;
    private final AtomicReference<TranslationBundle> defaultBundle;

    public TranslationBundleRegistryImpl(TranslationConfig config) {
        this.config = config;
        this.bundles = new AtomicReference<>(Collections.unmodifiableMap(new HashMap<>()));
        this.keys = new AtomicReference<>(Collections.unmodifiableSet(new HashSet<>()));
        this.currentBundle = new AtomicReference<>(null);
        this.defaultBundle = new AtomicReference<>(null);
    }

    @Override
    public @Nullable TranslationBundle get(Locale locale) {
        return bundles.get().get(locale);
    }

    @Override
    public Optional<TranslationBundle> getOptional(Locale locale) {
        return Optional.ofNullable(get(locale));
    }

    @Override
    public @Nullable TranslationBundle getCurrent() {
        return currentBundle.get();
    }

    @Override
    public Optional<TranslationBundle> getCurrentOptional() {
        return Optional.ofNullable(getCurrent());
    }

    @Override
    public @Nullable TranslationBundle getDefault() {
        return defaultBundle.get();
    }

    @Override
    public Optional<TranslationBundle> getDefaultOptional() {
        return Optional.ofNullable(getDefault());
    }

    @Override
    public Set<Locale> getRegisteredLocales() {
        return Collections.unmodifiableSet(bundles.get().keySet());
    }

    @Override
    public boolean isRegistered(Locale locale) {
        return bundles.get().containsKey(locale);
    }

    @Override
    public Set<String> getKeys() {
        return keys.get();
    }

    @Override
    public void register(TranslationBundle bundle) {
        // Update bundles map
        final Map<Locale, TranslationBundle> updatedBundles = new HashMap<>(bundles.get());
        updatedBundles.putIfAbsent(bundle.getLocale(), bundle);
        bundles.set(Collections.unmodifiableMap(updatedBundles));

        // Update keys map
        final Set<String> updatedKeys = new HashSet<>(getKeys());

        // Cache the new bundle ref
        final TranslationBundle newBundle = updatedBundles.get(bundle.getLocale());

        // Update current bundle if necessary
        if (currentBundle.get() == null && bundle.getLocale().equals(config.getCurrentLocale())) {
            currentBundle.set(newBundle);

            if (newBundle != null)
                updatedKeys.addAll(newBundle.getKeys());
        }

        // Update default bundle if necessary
        if (defaultBundle.get() == null && bundle.getLocale().equals(config.getDefaultLocale())) {
            defaultBundle.set(newBundle);

            if (newBundle != null)
                updatedKeys.addAll(newBundle.getKeys());
        }

        keys.set(Collections.unmodifiableSet(updatedKeys));
    }

    @Override
    public void clear() {
        currentBundle.set(null);
        defaultBundle.set(null);
        keys.set(Collections.unmodifiableSet(new HashSet<>()));
        bundles.set(Collections.unmodifiableMap(new HashMap<>()));
    }
}
