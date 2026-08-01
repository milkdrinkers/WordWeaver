package io.github.milkdrinkers.wordweaver.loader.impl;

import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleEntryImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves {@code <key:other.key>} references within a bundle's entries.
 * <p>
 * Applied by the loader to every parsers output.
 */
final class ReferenceResolver {
    private static final Pattern KEY_PATTERN = Pattern.compile("<key:([^>]+)>");
    private static final int MAX_RECURSION_DEPTH = 3;

    private ReferenceResolver() {
    }

    /**
     * Processes all entries to resolve key references recursively.
     *
     * @param entries The original map of entries
     * @return A new map with resolved references
     */
    static Map<String, TranslationBundleEntry> resolve(final Map<String, TranslationBundleEntry> entries) {
        final Map<String, TranslationBundleEntry> processed = new HashMap<>(entries);

        for (String key : entries.keySet()) {
            processEntry(key, entries, processed, 0);
        }

        return processed;
    }

    /**
     * Processes a single entry to resolve key references.
     *
     * @param key      The key of the entry to process
     * @param original The original map of entries
     * @param results  The map of processed entries
     * @param depth    Current recursion depth
     */
    private static void processEntry(final String key, final Map<String, TranslationBundleEntry> original, final Map<String, TranslationBundleEntry> results, int depth) {
        if (depth >= MAX_RECURSION_DEPTH)
            return;

        final TranslationBundleEntry entry = results.get(key);
        if (entry == null)
            return;

        boolean foundAnyKey = false;

        if (entry.isCollection()) { // LIST type entries
            final List<String> processedValues = new ArrayList<>();

            for (String value : entry.getValues()) {
                final StringBuffer processedValue = new StringBuffer();
                foundAnyKey = replaceKeysInString(value, processedValue, original, results, depth + 1);
                processedValues.add(processedValue.toString());
            }

            if (foundAnyKey) {
                results.put(key, new TranslationBundleEntryImpl(TranslationBundleEntry.Type.LIST, processedValues));
            }
        } else { // STRING type entries
            final StringBuffer processedValue = new StringBuffer();
            foundAnyKey = replaceKeysInString(entry.getValue(), processedValue, original, results, depth + 1);

            if (foundAnyKey) {
                results.put(key, new TranslationBundleEntryImpl(TranslationBundleEntry.Type.STRING, processedValue.toString()));
            }
        }

        // If we found and replaced any keys, process again to handle nested replacements
        if (foundAnyKey) {
            processEntry(key, original, results, depth + 1);
        }
    }

    /**
     * Processes a single string to replace all key references.
     *
     * @param input    The string to process
     * @param result   The buffer to append results to
     * @param original The original map of entries
     * @param results  The map of processed entries
     * @param depth    Current recursion depth
     * @return True if any keys were found and replaced
     */
    private static boolean replaceKeysInString(final String input, final StringBuffer result, final Map<String, TranslationBundleEntry> original, final Map<String, TranslationBundleEntry> results, int depth) {
        boolean foundAnyKey = false;
        final Matcher matcher = KEY_PATTERN.matcher(input);

        while (matcher.find()) {
            foundAnyKey = true;
            final String fullMatch = matcher.group(0);
            final String keyName = matcher.group(1);

            final String replacement = getReplacementValue(keyName, fullMatch, original, results, depth + 1);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return foundAnyKey;
    }

    /**
     * Gets the replacement value for a key reference.
     *
     * @param keyName   The key name to replace
     * @param fullMatch The full match string
     * @param original  The original map of entries
     * @param results   The map of processed entries
     * @param depth     Current recursion depth
     * @return The replacement value or the original match if not found
     */
    private static String getReplacementValue(final String keyName, final String fullMatch, final Map<String, TranslationBundleEntry> original, final Map<String, TranslationBundleEntry> results, int depth) {
        if (original.containsKey(keyName)) { // If the referenced key exists, ensure it's processed
            processEntry(keyName, original, results, depth + 1);

            return results.get(keyName).getValue(); // Return processed value
        } else {
            return fullMatch; // No replacement found, return the original
        }
    }
}
