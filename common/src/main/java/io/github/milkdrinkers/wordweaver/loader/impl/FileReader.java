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

final class FileReader {
    protected static final Gson GSON = new GsonBuilder()
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
            final JsonReader jsonReader = GSON.newJsonReader(bufferedReader);
        ) {
            final JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();

            if (jsonObject == null)
                throw new LanguageLoadException("Failed to read json as it is malformed!");

            return flatten(jsonObject);
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
            final String internalPath = String.format("%s.%s", currentPath, entry.getKey());

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
            final String internalPath = String.format("%s[%d]", currentPath, i);

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
}
