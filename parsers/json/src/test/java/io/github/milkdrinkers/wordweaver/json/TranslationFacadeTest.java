package io.github.milkdrinkers.wordweaver.json;

import io.github.milkdrinkers.wordweaver.Translation;
import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test of the public static {@link Translation} facade over the real stack. Resets the one-shot provider
 * singleton via reflection so the test is isolated and re-runnable.
 */
class TranslationFacadeTest {
    @TempDir
    Path dir;

    @BeforeEach
    void resetProviderSingleton() throws Exception {
        final Field instance = Class.forName("io.github.milkdrinkers.wordweaver.TranslationProvider").getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void fullApiThroughStaticFacade() throws Exception {
        Files.write(dir.resolve("en_US.json"),
            "{\"msg\":{\"hello\":\"Hello\"},\"list\":[\"x\",\"y\"]}".getBytes(StandardCharsets.UTF_8));

        final TranslationConfig config = TranslationConfig.builder()
            .namespace("wordweaver:facade")
            .translationDirectory(dir)
            .locale("en_US")
            .defaultLocale("en_US")
            .extractBundles(false)
            .updateBundles(false)
            .build();

        Translation.initialize(config);

        assertEquals("Hello", Translation.of("msg.hello"));
        assertEquals("fallback", Translation.of("missing", "fallback"));
        assertEquals(Arrays.asList("x", "y"), Translation.ofList("list"));

        final Component component = Translation.as("msg.hello");
        assertNotNull(component);

        final List<Component> components = Translation.asList("list");
        assertEquals(2, components.size());

        assertTrue(Translation.getKeys().contains("msg.hello"));
        assertEquals("en_US", Translation.getLocaleTag());
        assertEquals("en_US", Translation.getDefaultLocaleTag());
    }
}
