package io.github.milkdrinkers.wordweaver.storage.impl;

import io.github.milkdrinkers.wordweaver.LocaleUtil;
import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundle;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationBundleRegistryImplTest {
    private static final Locale EN = Locale.forLanguageTag("en-US");
    private static final Locale FR = Locale.forLanguageTag("fr-FR");

    private TranslationConfig config(String current, String fallback) {
        return TranslationConfig.builder()
            .namespace("wordweaver:test")
            .translationDirectory(Paths.get("unused"))
            .locale(current)
            .defaultLocale(fallback)
            .build();
    }

    private TranslationBundle bundle(String tag, String... kv) {
        final Map<String, TranslationBundleEntry> entries = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2)
            entries.put(kv[i], new TranslationBundleEntryImpl(TranslationBundleEntry.Type.STRING, kv[i + 1]));
        return new TranslationBundleImpl(LocaleUtil.fromTag(tag), entries);
    }

    @Test
    void registersAndRetrievesByLocale() {
        final TranslationBundleRegistryImpl registry = new TranslationBundleRegistryImpl(config("en_US", "en_US"));
        registry.register(bundle("en_US", "a", "1"));

        assertNotNull(registry.get(EN));
        assertTrue(registry.isRegistered(EN));
        assertTrue(registry.getRegisteredLocales().contains(EN));
        assertEquals("1", registry.get(EN).getEntry("a").getValue());
    }

    @Test
    void tracksCurrentAndDefaultBundles() {
        final TranslationBundleRegistryImpl registry = new TranslationBundleRegistryImpl(config("fr_FR", "en_US"));
        registry.register(bundle("fr_FR", "a", "fr"));
        registry.register(bundle("en_US", "a", "en"));

        assertEquals("fr", registry.getCurrent().getEntry("a").getValue());
        assertEquals("en", registry.getDefault().getEntry("a").getValue());
    }

    @Test
    void combinesKeysOfCurrentAndDefault() {
        final TranslationBundleRegistryImpl registry = new TranslationBundleRegistryImpl(config("fr_FR", "en_US"));
        registry.register(bundle("fr_FR", "only_fr", "x"));
        registry.register(bundle("en_US", "only_en", "y"));

        assertTrue(registry.getKeys().containsAll(Arrays.asList("only_fr", "only_en")));
    }

    @Test
    void clearRemovesEverything() {
        final TranslationBundleRegistryImpl registry = new TranslationBundleRegistryImpl(config("en_US", "en_US"));
        registry.register(bundle("en_US", "a", "1"));
        registry.clear();

        assertNull(registry.getCurrent());
        assertNull(registry.getDefault());
        assertTrue(registry.getRegisteredLocales().isEmpty());
        assertTrue(registry.getKeys().isEmpty());
        assertFalse(registry.isRegistered(EN));
    }
}
