package io.github.milkdrinkers.wordweaver.loader.impl;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.parser.TranslationParser;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundle;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleEntryImpl;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleRegistryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultTranslationLoaderTest {
    private static final Locale EN = Locale.forLanguageTag("en-US");

    @TempDir
    Path dir;

    private TranslationConfig baseConfig() {
        return TranslationConfig.builder()
            .namespace("wordweaver:test")
            .translationDirectory(dir)
            .locale("en_US")
            .defaultLocale("en_US")
            .extractBundles(false)
            .updateBundles(false)
            .build();
    }

    private TranslationBundleRegistry load(TranslationConfig config) throws IOException {
        final TranslationBundleRegistry registry = new TranslationBundleRegistryImpl(config);
        new DefaultTranslationLoader(config, registry).loadBundles();
        return registry;
    }

    @Test
    void loadsPropertiesViaServiceLoader() throws IOException {
        Files.write(dir.resolve("en_US.properties"), "greeting=Hello\n".getBytes(StandardCharsets.UTF_8));

        final TranslationBundle bundle = load(baseConfig()).get(EN);

        assertNotNull(bundle);
        assertEquals("Hello", bundle.getEntry("greeting").getValue());
    }

    @Test
    void resolvesReferencesDuringLoad() throws IOException {
        Files.write(dir.resolve("en_US.properties"), "brand=WordWeaver\nwelcome=Welcome to <key:brand>\n".getBytes(StandardCharsets.UTF_8));

        final TranslationBundle bundle = load(baseConfig()).get(EN);

        assertEquals("Welcome to WordWeaver", bundle.getEntry("welcome").getValue());
    }

    @Test
    void ignoresFilesWithUnknownExtensions() throws IOException {
        Files.write(dir.resolve("en_US.properties"), "a=1\n".getBytes(StandardCharsets.UTF_8));
        Files.write(dir.resolve("en_US.txt"), "b=2\n".getBytes(StandardCharsets.UTF_8));

        final TranslationBundle bundle = load(baseConfig()).get(EN);

        assertNotNull(bundle.getEntry("a"));
        assertNull(bundle.getEntry("b"));
    }

    @Test
    void usesExplicitlyConfiguredParser() throws IOException {
        Files.write(dir.resolve("en_US.custom"), "content".getBytes(StandardCharsets.UTF_8));

        final TranslationConfig config = TranslationConfig.builder()
            .namespace("wordweaver:test")
            .translationDirectory(dir)
            .locale("en_US")
            .defaultLocale("en_US")
            .extractBundles(false)
            .updateBundles(false)
            .parser(new CustomParser())
            .build();

        final TranslationBundle bundle = load(config).get(EN);

        assertEquals("customValue", bundle.getEntry("customKey").getValue());
    }

    /**
     * A trivial parser for a fictional ".custom" format, used to verify explicit registration.
     */
    private static final class CustomParser implements TranslationParser {
        @Override
        public Set<String> extensions() {
            return Collections.singleton("custom");
        }

        @Override
        public Map<String, TranslationBundleEntry> parse(Path file) {
            final Map<String, TranslationBundleEntry> entries = new HashMap<>();
            entries.put("customKey", new TranslationBundleEntryImpl(TranslationBundleEntry.Type.STRING, "customValue"));
            return entries;
        }
    }
}
