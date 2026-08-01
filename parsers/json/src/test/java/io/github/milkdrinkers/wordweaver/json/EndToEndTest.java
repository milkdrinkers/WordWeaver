package io.github.milkdrinkers.wordweaver.json;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.TranslationLoader;
import io.github.milkdrinkers.wordweaver.loader.impl.DefaultTranslationLoader;
import io.github.milkdrinkers.wordweaver.service.impl.TranslationServiceImpl;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleRegistryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exercises the full stack (loader -> registry -> service) with a mix of .json, .jsonc and .properties bundles
 * loaded through auto-discovered parsers.
 */
class EndToEndTest {
    private static final Locale EN_US = Locale.forLanguageTag("en-US");
    private static final Locale EN_GB = Locale.forLanguageTag("en-GB");
    private static final Locale SV_SE = Locale.forLanguageTag("sv-SE");

    @TempDir
    Path dir;
    private TranslationServiceImpl service;

    @BeforeEach
    void setUp() throws IOException {
        Files.write(dir.resolve("en_US.json"),
            "{\"brand\":\"WordWeaver\",\"welcome\":\"Hi from <key:brand>\",\"rules\":[\"one\",\"two\"]}".getBytes(StandardCharsets.UTF_8));
        Files.write(dir.resolve("en_GB.jsonc"),
            "{\n // a comment\n \"greeting\":\"Hiya\"\n}".getBytes(StandardCharsets.UTF_8));
        Files.write(dir.resolve("sv_SE.properties"),
            "greeting=Hej\n".getBytes(StandardCharsets.UTF_8));

        final TranslationConfig config = TranslationConfig.builder()
            .namespace("wordweaver:e2e")
            .translationDirectory(dir)
            .locale("en_US")
            .defaultLocale("sv_SE")
            .extractBundles(false)
            .updateBundles(false)
            .build();

        final TranslationBundleRegistry registry = new TranslationBundleRegistryImpl(config);
        final TranslationLoader loader = new DefaultTranslationLoader(config, registry);
        service = new TranslationServiceImpl(config, registry, loader);
    }

    @Test
    void loadsEveryFormat() {
        assertEquals("WordWeaver", service.getString(EN_US, "brand", null));
        assertEquals("Hiya", service.getString(EN_GB, "greeting", null));
        assertEquals("Hej", service.getString(SV_SE, "greeting", null));
    }

    @Test
    void resolvesReferencesInJson() {
        assertEquals("Hi from WordWeaver", service.getString(EN_US, "welcome", null));
    }

    @Test
    void jsonArraysBecomeLists() {
        final List<String> rules = service.getStringList(EN_US, "rules", null);
        assertEquals(Arrays.asList("one", "two"), rules);
        assertEquals("one", service.getString(EN_US, "rules.1", null));
    }

    @Test
    void fallsBackFromJsonActiveToPropertiesDefault() {
        // Active locale en_US (JSON) lacks "greeting"; falls back to default sv_SE (.properties)
        assertEquals("Hej", service.getString("greeting", null));
    }
}
