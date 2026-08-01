package io.github.milkdrinkers.wordweaver.json;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.impl.DefaultTranslationLoader;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleRegistryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises extraction + update against the bundled test resource {@code lang/en_US.json} ({"a":"1","b":"2"}).
 */
class ExtractUpdateIntegrationTest {
    private static final Locale EN = Locale.forLanguageTag("en-US");

    @TempDir
    Path dir;

    private TranslationConfig config(boolean extract, boolean update) {
        return TranslationConfig.builder()
            .namespace("wordweaver:extract")
            .translationDirectory(dir)
            .resourcesDirectory(Paths.get("lang"))
            .locale("en_US")
            .defaultLocale("en_US")
            .extractBundles(extract)
            .updateBundles(update)
            .build();
    }

    @Test
    void extractsMissingResourceThenLoadsIt() throws IOException {
        final TranslationConfig config = config(true, false);
        final TranslationBundleRegistry registry = new TranslationBundleRegistryImpl(config);
        final DefaultTranslationLoader loader = new DefaultTranslationLoader(config, registry);

        loader.extractMissingBundles();
        loader.loadBundles();

        assertTrue(Files.exists(dir.resolve("en_US.json")), "resource should be extracted");
        assertEquals("1", registry.get(EN).getEntry("a").getValue());
        assertEquals("2", registry.get(EN).getEntry("b").getValue());
    }

    @Test
    void updateAddsMissingKeysWhilePreservingUserValues() throws IOException {
        // User already has a partial file with a customised value
        Files.write(dir.resolve("en_US.json"), "{\"a\":\"custom\"}".getBytes(StandardCharsets.UTF_8));

        final TranslationConfig config = config(false, true);
        final TranslationBundleRegistry registry = new TranslationBundleRegistryImpl(config);
        final DefaultTranslationLoader loader = new DefaultTranslationLoader(config, registry);

        loader.updateExistingBundles();
        loader.loadBundles();

        assertEquals("custom", registry.get(EN).getEntry("a").getValue(), "user value preserved");
        assertEquals("2", registry.get(EN).getEntry("b").getValue(), "missing key merged in from resource");
    }
}
