package io.github.milkdrinkers.wordweaver.loader.impl;

import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleEntryImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceResolverTest {
    private static TranslationBundleEntry str(String value) {
        return new TranslationBundleEntryImpl(TranslationBundleEntry.Type.STRING, value);
    }

    @Test
    void shouldResolveKeyReference() {
        final Map<String, TranslationBundleEntry> entries = new HashMap<>();
        entries.put("brand", str("WordWeaver"));
        entries.put("welcome", str("Welcome to <key:brand>!"));

        final Map<String, TranslationBundleEntry> resolved = ReferenceResolver.resolve(entries);

        assertEquals("Welcome to WordWeaver!", resolved.get("welcome").getValue());
    }

    @Test
    void shouldLeaveUnknownReferenceUntouched() {
        final Map<String, TranslationBundleEntry> entries = new HashMap<>();
        entries.put("welcome", str("Hi <key:missing>"));

        final Map<String, TranslationBundleEntry> resolved = ReferenceResolver.resolve(entries);

        assertEquals("Hi <key:missing>", resolved.get("welcome").getValue());
    }

    @Test
    void shouldResolveNestedReferences() {
        final Map<String, TranslationBundleEntry> entries = new HashMap<>();
        entries.put("a", str("A"));
        entries.put("b", str("<key:a>B"));
        entries.put("c", str("<key:b>C"));

        final Map<String, TranslationBundleEntry> resolved = ReferenceResolver.resolve(entries);

        assertEquals("ABC", resolved.get("c").getValue());
    }

    @Test
    void shouldResolveReferencesInsideListEntries() {
        final Map<String, TranslationBundleEntry> entries = new HashMap<>();
        entries.put("brand", str("WordWeaver"));
        entries.put("list", new TranslationBundleEntryImpl(TranslationBundleEntry.Type.LIST, Arrays.asList("<key:brand> one", "two")));

        final Map<String, TranslationBundleEntry> resolved = ReferenceResolver.resolve(entries);

        assertEquals(Arrays.asList("WordWeaver one", "two"), resolved.get("list").getValues());
    }

    @Test
    void shouldTerminateOnCyclicReferences() {
        final Map<String, TranslationBundleEntry> entries = new HashMap<>();
        entries.put("a", str("<key:b>"));
        entries.put("b", str("<key:a>"));

        assertDoesNotThrow(() -> ReferenceResolver.resolve(entries));
    }
}
