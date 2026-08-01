package io.github.milkdrinkers.wordweaver.service.impl;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.TranslationLoader;
import io.github.milkdrinkers.wordweaver.loader.impl.DefaultTranslationLoader;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleRegistryImpl;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationServiceImplTest {
    @TempDir
    Path dir;
    private TranslationServiceImpl service;

    @BeforeEach
    void setUp() throws IOException {
        Files.write(dir.resolve("en_US.properties"), "greeting=Hello\nonly_en=EN only\nrules=single\n".getBytes(StandardCharsets.UTF_8));
        Files.write(dir.resolve("fr_FR.properties"), "greeting=Bonjour\n".getBytes(StandardCharsets.UTF_8));

        final TranslationConfig config = TranslationConfig.builder()
            .namespace("wordweaver:test")
            .translationDirectory(dir)
            .locale("fr_FR")
            .defaultLocale("en_US")
            .extractBundles(false)
            .updateBundles(false)
            .build();

        final TranslationBundleRegistry registry = new TranslationBundleRegistryImpl(config);
        final TranslationLoader loader = new DefaultTranslationLoader(config, registry);
        service = new TranslationServiceImpl(config, registry, loader);
    }

    @Test
    void returnsActiveLocaleValue() {
        assertEquals("Bonjour", service.getString("greeting", null));
    }

    @Test
    void fallsBackToDefaultLocaleForMissingKey() {
        assertEquals("EN only", service.getString("only_en", null));
    }

    @Test
    void missingKeyReturnsFallbackThenEmpty() {
        assertEquals("fb", service.getString("does.not.exist", "fb"));
        assertEquals("", service.getString("does.not.exist", null));
    }

    @Test
    void propertiesListsAreSingleElement() {
        final List<String> rules = service.getStringList("rules", null);
        assertEquals(Collections.singletonList("single"), rules);
    }

    @Test
    void convertsToComponent() {
        final Component component = service.getComponent("greeting", null);
        assertNotNull(component);
    }

    @Test
    void switchingLocaleChangesLookup() {
        service.setLocale(Locale.forLanguageTag("en-US"));
        assertEquals("Hello", service.getString("greeting", null));
    }

    @Test
    void keysCombineCurrentAndDefault() {
        assertTrue(service.getKeys().contains("greeting"));
        assertTrue(service.getKeys().contains("only_en"));
    }

    @Test
    void reloadPicksUpDiskChanges() throws IOException {
        Files.write(dir.resolve("fr_FR.properties"), "greeting=Salut\n".getBytes(StandardCharsets.UTF_8));
        service.reload();
        assertEquals("Salut", service.getString("greeting", null));
    }
}
