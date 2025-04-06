package io.github.milkdrinkers.wordweaver.loader.impl;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import io.github.milkdrinkers.wordweaver.storage.LanguageEntry;
import io.github.milkdrinkers.wordweaver.storage.LanguageLoadException;
import io.github.milkdrinkers.wordweaver.storage.impl.LanguageEntryImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class FileReader {
    static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .setStrictness(Strictness.LENIENT)
        .create();

    /**
     * Reads a map of translation from a json file
     *
     * @param path The path to the file
     * @return A map of translations
     * @throws LanguageLoadException If the file does not exist, can not be read, is not a file, or the json is malformed
     */
    public static Map<String, LanguageEntry> readFile(final String path) throws LanguageLoadException {
        return readFile(new File(path));
    }

    /**
     * Reads a map of translation from a json file
     *
     * @param path The path to the file
     * @return A map of translations
     * @throws LanguageLoadException If the file does not exist, can not be read, is not a file, or the json is malformed
     */
    public static Map<String, LanguageEntry> readFile(final Path path) throws LanguageLoadException {
        return readFile(path.toFile());
    }

    /**
     * Reads a map of translation from a json file
     *
     * @param file The file to read
     * @return A map of translations
     * @throws LanguageLoadException If the file does not exist, can not be read, is not a file, or the json is malformed
     */
    public static Map<String, LanguageEntry> readFile(final File file) throws LanguageLoadException {
        try {
            if (!file.exists())
                throw new LanguageLoadException("The language file does not exist!");

            if (!file.canRead())
                throw new LanguageLoadException("The language file can not be read! Ensure the application has sufficient permissions to read the file.");

            if (!file.isFile())
                throw new LanguageLoadException("The language file is not a file!");
        } catch (SecurityException e) {
            throw new LanguageLoadException("Security violation!", e);
        }

        try (
            final java.io.FileReader fileReader = new java.io.FileReader(file);
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            final JsonReader jsonReader = GSON.newJsonReader(bufferedReader)
        ) {
            final JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();

            if (jsonObject == null)
                throw new LanguageLoadException("Failed to read json as it is malformed!");

            return Parser.processAllEntries(flatten(jsonObject));
        } catch (JsonIOException e) {
            throw new LanguageLoadException("Failed to read json from reader!", e);
        } catch (JsonSyntaxException e) {
            throw new LanguageLoadException("Failed to read json as it is malformed!", e);
        } catch (IOException e) {
            throw new LanguageLoadException("Failed to read json as the file does not exist!", e);
        }
    }

    /**
     * Flattens a JsonObject into a map of translations
     *
     * @param jsonObject The JsonObject to flatten
     * @return A map of translations
     */
    public static Map<String, LanguageEntry> flatten(final JsonObject jsonObject) {
        final Map<String, LanguageEntry> translationMap = new HashMap<>();

        if (jsonObject.isJsonNull()) {
            return translationMap;
        } else {
            flattenJsonElement("", jsonObject, translationMap); // Begin recursively flattening
        }

        return translationMap;
    }

    /**
     * Flattens a JsonElement into a map of translations
     *
     * @param currentPath    The current path in the translationMap (Used in recursion)
     * @param element        The current JsonElement
     * @param translationMap The map that stores the translations
     */
    private static void flattenJsonElement(final String currentPath, final JsonElement element, final Map<String, LanguageEntry> translationMap) {
        if (element.isJsonPrimitive()) {
            final JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();

            translationMap.put(currentPath, new LanguageEntryImpl(LanguageEntry.Type.STRING, jsonPrimitive.getAsString()));
        } else if (element.isJsonObject()) {
            flattenObject(currentPath, element, translationMap);
        } else if (element.isJsonArray()) {
            flattenArray(currentPath, element, translationMap);
        } else if (element.isJsonNull()) {
            // Add empty translation for null
            translationMap.put(currentPath, new LanguageEntryImpl(LanguageEntry.Type.STRING, ""));
        }
    }

    /**
     * Recursively iterates through objects and stores primitive values
     *
     * @param currentPath    The current path in the translationMap
     * @param element        The current JsonElement
     * @param translationMap The map that stores the translations
     */
    private static void flattenObject(final String currentPath, final JsonElement element, final Map<String, LanguageEntry> translationMap) {
        /*
            Allows accessing individual elements via {@code object.key}.
         */
        final JsonObject jsonObject = element.getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            final String internalPath = currentPath.isEmpty() ? entry.getKey() : String.format("%s.%s", currentPath, entry.getKey());

            // Recursively flatten
            flattenJsonElement(internalPath, entry.getValue(), translationMap);
        }
    }

    /**
     * Recursively iterates through arrays and stores primitive values
     *
     * @param currentPath    The current path in the translationMap
     * @param element        The current JsonElement
     * @param translationMap The map that stores the translations
     * @implNote This method stores individual elements with array indices and the complete array as a Translation with all values
     */
    private static void flattenArray(final String currentPath, final JsonElement element, final Map<String, LanguageEntry> translationMap) {
        /*
          Allows accessing individual elements via {@code array[index]}.
          Allows retrieving the entire array of elements via {@code array}.
         */
        final JsonArray jsonArray = element.getAsJsonArray();
        final List<String> arrayValues = new ArrayList<>();

        // Store individual elements with array indices
        for (int i = 0; i < jsonArray.size(); i++) {
            final JsonElement arrayElement = jsonArray.get(i);
            final String internalPath = String.format("%s.%d", currentPath, i + 1);

            if (arrayElement.isJsonPrimitive()) {
                final String arrayValue = arrayElement.getAsString();

                // Add element to resulting list
                arrayValues.add(arrayValue);

                // Add unique entry for element
                translationMap.put(internalPath, new LanguageEntryImpl(LanguageEntry.Type.LIST, arrayValue));
            } else {
                // Recursively flatten
                flattenJsonElement(internalPath, arrayElement, translationMap);
            }
        }

        // Store the complete array as a Translation with all values
        if (!arrayValues.isEmpty()) {
            translationMap.put(currentPath, new LanguageEntryImpl(LanguageEntry.Type.LIST, arrayValues));
        }
    }

    static class Parser {
        private static final Pattern KEY_PATTERN = Pattern.compile("<key:([^>]+)>");
        private static final int MAX_RECURSION_DEPTH = 3;

        /**
         * Processes all LanguageEntries to resolve key references recursively.
         *
         * @param entries The original map of language entries
         * @return A new map with resolved references
         */
        public static Map<String, LanguageEntry> processAllEntries(final Map<String, LanguageEntry> entries) {
            final Map<String, LanguageEntry> processed = new HashMap<>(entries);

            // Process each entry to resolve references
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
        private static void processEntry(final String key, final Map<String, LanguageEntry> original, final Map<String, LanguageEntry> results, int depth) {
            if (depth >= MAX_RECURSION_DEPTH)
                return;

            final LanguageEntry entry = results.get(key);
            if (entry == null)
                return;

            boolean foundAnyKey = false;

            if (entry.isCollection()) { // Handle LIST type entries
                final List<String> processedValues = new ArrayList<>();

                for (String value : entry.getValues()) {
                    final StringBuffer processedValue = new StringBuffer();
                    foundAnyKey = replaceKeysInString(value, processedValue, original, results, depth + 1);
                    processedValues.add(processedValue.toString());
                }

                if (foundAnyKey) {
                    results.put(key, new LanguageEntryImpl(LanguageEntry.Type.LIST, processedValues));
                }
            } else { // Handle STRING type entries
                final StringBuffer processedValue = new StringBuffer();
                foundAnyKey = replaceKeysInString(entry.getValue(), processedValue, original, results, depth + 1);

                if (foundAnyKey) {
                    results.put(key, new LanguageEntryImpl(LanguageEntry.Type.STRING, processedValue.toString()));
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
        private static boolean replaceKeysInString(final String input, final StringBuffer result, final Map<String, LanguageEntry> original, final Map<String, LanguageEntry> results, int depth) {
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
        private static String getReplacementValue(final String keyName, final String fullMatch, final Map<String, LanguageEntry> original, final Map<String, LanguageEntry> results, int depth) {
            if (original.containsKey(keyName)) { // If the referenced key exists, ensure it's processed
                processEntry(keyName, original, results, depth + 1);

                return results.get(keyName).getValue(); // Return the processed value
            } else {
                return fullMatch; // No replacement found, return the original match
            }
        }
    }
}
