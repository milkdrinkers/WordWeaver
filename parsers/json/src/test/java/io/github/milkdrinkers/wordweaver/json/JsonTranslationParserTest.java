package io.github.milkdrinkers.wordweaver.json;

import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.TranslationLoadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonTranslationParserTest {
    private final JsonTranslationParser parser = new JsonTranslationParser();

    @TempDir
    Path tempDir;

    private Path write(String name, String content) throws IOException {
        final Path file = tempDir.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    @Test
    void shouldParseNestedAndArrays() throws IOException {
        Map<String, TranslationBundleEntry> entries = parser.parse(write("en_US.json", "{\"messages\":{\"welcome\":\"Hi\"},\"rules\":[\"a\",\"b\"]}"));

        assertEquals("Hi", entries.get("messages.welcome").getValue());

        List<String> rules = entries.get("rules").getValues();
        assertEquals(2, rules.size());
        assertEquals("a", rules.get(0));
        assertEquals("b", rules.get(1));

        // Individual indexed elements (1-based)
        assertEquals("a", entries.get("rules.1").getValue());
        assertEquals("b", entries.get("rules.2").getValue());
    }

    @Test
    void shouldParseJsoncWithComments() throws IOException {
        Map<String, TranslationBundleEntry> entries = parser.parse(write("en_US.jsonc", "{\n  // a comment\n  \"greeting\": \"Hello\"\n}"));

        assertEquals("Hello", entries.get("greeting").getValue());
    }

    @Test
    void numbersAndBooleansBecomeStrings() throws IOException {
        Map<String, TranslationBundleEntry> entries = parser.parse(write("en_US.json", "{\"count\":42,\"enabled\":true}"));

        assertEquals("42", entries.get("count").getValue());
        assertEquals("true", entries.get("enabled").getValue());
    }

    @Test
    void nullBecomesEmptyString() throws IOException {
        Map<String, TranslationBundleEntry> entries = parser.parse(write("en_US.json", "{\"empty\":null}"));

        assertEquals("", entries.get("empty").getValue());
    }

    @Test
    void malformedJsonThrows() throws IOException {
        Path file = write("en_US.json", "{ this is not valid json ");

        assertThrows(TranslationLoadException.class, () -> parser.parse(file));
    }

    @Test
    void missingFileThrows() {
        assertThrows(TranslationLoadException.class, () -> parser.parse(tempDir.resolve("does_not_exist.json")));
    }

    @Test
    void shouldMergeMissingKeysPreservingTargetValues() throws IOException {
        Path target = write("en_US.json", "{\"key1\":\"modified\"}");

        String origin = "{\"key1\":\"original\",\"key2\":\"added\"}";
        parser.merge(new ByteArrayInputStream(origin.getBytes(StandardCharsets.UTF_8)), target);

        Map<String, TranslationBundleEntry> entries = parser.parse(target);

        assertEquals("modified", entries.get("key1").getValue());
        assertEquals("added", entries.get("key2").getValue());
    }

    @Test
    void mergePreservesNestedTargetValuesAndAddsMissing() throws IOException {
        Path target = write("en_US.json", "{\"section\":{\"a\":\"keep\"}}");

        String origin = "{\"section\":{\"a\":\"orig\",\"b\":\"added\"}}";
        parser.merge(new ByteArrayInputStream(origin.getBytes(StandardCharsets.UTF_8)), target);

        Map<String, TranslationBundleEntry> entries = parser.parse(target);

        assertEquals("keep", entries.get("section.a").getValue());
        assertEquals("added", entries.get("section.b").getValue());
    }
}
