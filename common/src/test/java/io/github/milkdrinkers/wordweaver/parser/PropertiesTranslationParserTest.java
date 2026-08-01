package io.github.milkdrinkers.wordweaver.parser;

import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertiesTranslationParserTest {
    private final PropertiesTranslationParser parser = new PropertiesTranslationParser();

    @TempDir
    Path tempDir;

    @Test
    void handlesOnlyPropertiesExtension() {
        assertTrue(parser.extensions().contains("properties"));
        assertTrue(parser.supportsMerge());
    }

    @Test
    void shouldParseProperties() throws IOException {
        Path file = tempDir.resolve("en_US.properties");
        Files.write(file, "messages.welcome=Welcome\nmessages.bye=Goodbye\n".getBytes(StandardCharsets.UTF_8));

        Map<String, TranslationBundleEntry> entries = parser.parse(file);

        assertEquals("Welcome", entries.get("messages.welcome").getValue());
        assertEquals("Goodbye", entries.get("messages.bye").getValue());
        assertFalse(entries.get("messages.welcome").isCollection());
    }

    @Test
    void shouldParseUtf8Values() throws IOException {
        Path file = tempDir.resolve("de_DE.properties");
        Files.write(file, "greeting=Grüß Gott\n".getBytes(StandardCharsets.UTF_8));

        Map<String, TranslationBundleEntry> entries = parser.parse(file);

        assertEquals("Grüß Gott", entries.get("greeting").getValue());
    }

    @Test
    void shouldAppendMissingKeysOnMerge() throws IOException {
        Path target = tempDir.resolve("en_US.properties");
        Files.write(target, "messages.welcome=Hello\n".getBytes(StandardCharsets.UTF_8));

        String origin = "messages.welcome=Welcome\nmessages.newKey=Default\n";
        parser.merge(new ByteArrayInputStream(origin.getBytes(StandardCharsets.UTF_8)), target);

        Map<String, TranslationBundleEntry> entries = parser.parse(target);

        // Existing value preserved, missing key appended
        assertEquals("Hello", entries.get("messages.welcome").getValue());
        assertEquals("Default", entries.get("messages.newKey").getValue());
    }

    @Test
    void mergeIsNoOpWhenNothingMissing() throws IOException {
        Path target = tempDir.resolve("en_US.properties");
        Files.write(target, "a=1\nb=2\n".getBytes(StandardCharsets.UTF_8));

        String origin = "a=9\nb=8\n"; // same keys, different values
        parser.merge(new ByteArrayInputStream(origin.getBytes(StandardCharsets.UTF_8)), target);

        Map<String, TranslationBundleEntry> entries = parser.parse(target);

        // No keys missing, so nothing appended and target values untouched
        assertEquals("1", entries.get("a").getValue());
        assertEquals("2", entries.get("b").getValue());
        assertEquals(2, entries.size());
    }
}
