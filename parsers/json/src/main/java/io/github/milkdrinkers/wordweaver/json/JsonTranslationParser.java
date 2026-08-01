package io.github.milkdrinkers.wordweaver.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import io.github.milkdrinkers.wordweaver.parser.TranslationParser;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.TranslationLoadException;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleEntryImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parser for JSON and JSONC (JSON with comments) files using GSON.
 */
public class JsonTranslationParser implements TranslationParser {
    private static final Set<String> EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("json", "jsonc")));

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .setStrictness(Strictness.LENIENT)
        .create();

    @Override
    public Set<String> extensions() {
        return EXTENSIONS;
    }

    @Override
    public Map<String, TranslationBundleEntry> parse(Path file) throws TranslationLoadException {
        if (!Files.exists(file))
            throw new TranslationLoadException("The bundle file does not exist!");

        if (!Files.isReadable(file))
            throw new TranslationLoadException("The bundle file can not be read! Ensure the application has sufficient permissions to read the file.");

        if (!Files.isRegularFile(file))
            throw new TranslationLoadException("The bundle file is not a file!");

        try (
            final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
            final JsonReader jsonReader = GSON.newJsonReader(reader)
        ) {
            final JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();

            if (jsonObject == null)
                throw new TranslationLoadException("Failed to read json as it is malformed!");

            return flatten(jsonObject);
        } catch (JsonIOException e) {
            throw new TranslationLoadException("Failed to read json from reader!", e);
        } catch (JsonSyntaxException e) {
            throw new TranslationLoadException("Failed to read json as it is malformed!", e);
        } catch (IOException e) {
            throw new TranslationLoadException("Failed to read json as the file does not exist!", e);
        }
    }

    @Override
    public boolean supportsMerge() {
        return true;
    }

    @Override
    public void merge(InputStream origin, Path target) throws IOException {
        final JsonObject originJson;
        try (
            final Reader reader = new InputStreamReader(origin, StandardCharsets.UTF_8);
            final JsonReader jsonReader = GSON.newJsonReader(reader)
        ) {
            originJson = JsonParser.parseReader(jsonReader).getAsJsonObject();
        }

        final JsonObject targetJson;
        try (
            final Reader reader = Files.newBufferedReader(target, StandardCharsets.UTF_8);
            final JsonReader jsonReader = GSON.newJsonReader(reader)
        ) {
            targetJson = JsonParser.parseReader(jsonReader).getAsJsonObject();
        }

        // Merge preserving order
        final JsonObject mergedJson = mergeJsonObjects(originJson, targetJson);

        Files.write(target, GSON.toJson(mergedJson).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Flattens a JsonObject into a map of entries
     *
     * @param jsonObject The JsonObject to flatten
     * @return A map of entries
     */
    private static Map<String, TranslationBundleEntry> flatten(final JsonObject jsonObject) {
        final Map<String, TranslationBundleEntry> entries = new HashMap<>();

        if (jsonObject.isJsonNull()) {
            return entries;
        } else {
            flattenJsonElement("", jsonObject, entries); // Begin recursively flattening
        }

        return entries;
    }

    /**
     * Flattens a JsonElement into a map of entries
     *
     * @param currentPath The current path in the map (Used in recursion)
     * @param element The current JsonElement
     * @param entries The map that stores the entries
     */
    private static void flattenJsonElement(final String currentPath, final JsonElement element, final Map<String, TranslationBundleEntry> entries) {
        if (element.isJsonPrimitive()) {
            final JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();

            entries.put(currentPath, new TranslationBundleEntryImpl(TranslationBundleEntry.Type.STRING, jsonPrimitive.getAsString()));
        } else if (element.isJsonObject()) {
            flattenObject(currentPath, element, entries);
        } else if (element.isJsonArray()) {
            flattenArray(currentPath, element, entries);
        } else if (element.isJsonNull()) {
            // Add empty entry for null
            entries.put(currentPath, new TranslationBundleEntryImpl(TranslationBundleEntry.Type.STRING, ""));
        }
    }

    /**
     * Recursively iterates through objects and stores primitive values
     *
     * @param currentPath The current path in the map
     * @param element The current JsonElement
     * @param entries The map that stores the entries
     */
    private static void flattenObject(final String currentPath, final JsonElement element, final Map<String, TranslationBundleEntry> entries) {
        final JsonObject jsonObject = element.getAsJsonObject(); // Allows accessing individual elements via "object.key"

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            final String internalPath = currentPath.isEmpty() ? entry.getKey() : String.format("%s.%s", currentPath, entry.getKey());

            // Recursively flatten
            flattenJsonElement(internalPath, entry.getValue(), entries);
        }
    }

    /**
     * Recursively iterates through arrays and stores primitive values
     *
     * @param currentPath The current path in the map
     * @param element The current JsonElement
     * @param entries The map that stores the entries
     * @implNote This method stores individual elements with array indices and the complete array as an entry with all values
     */
    private static void flattenArray(final String currentPath, final JsonElement element, final Map<String, TranslationBundleEntry> entries) {
        final JsonArray jsonArray = element.getAsJsonArray(); // Allows accessing individual elements via "array[index]" and retrieving the entire array of elements via "array"
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
                entries.put(internalPath, new TranslationBundleEntryImpl(TranslationBundleEntry.Type.LIST, arrayValue));
            } else {
                // Recursively flatten
                flattenJsonElement(internalPath, arrayElement, entries);
            }
        }

        // Store the complete array as an entry with all values
        if (!arrayValues.isEmpty()) {
            entries.put(currentPath, new TranslationBundleEntryImpl(TranslationBundleEntry.Type.LIST, arrayValues));
        }
    }

    /**
     * Recursively merges Json objects, adding missing keys from origin to target while preserving targets existing values and maintaining origins order.
     *
     * @param origin The original Json object
     * @param target The user modified Json object
     * @return The merged Json object
     */
    private static JsonObject mergeJsonObjects(JsonObject origin, JsonObject target) {
        final Set<String> processedKeys = new HashSet<>();
        final JsonObject result = new JsonObject();

        // Add all keys from origin in original order
        for (Map.Entry<String, JsonElement> entry : origin.entrySet()) {
            final String key = entry.getKey();
            processedKeys.add(key);

            if (target.has(key)) { // Key exists in both, check if deep merge required
                final JsonElement originValue = entry.getValue();
                final JsonElement targetValue = target.get(key);

                if (originValue.isJsonObject() && targetValue.isJsonObject()) {
                    result.add(key, mergeJsonObjects(originValue.getAsJsonObject(), targetValue.getAsJsonObject())); // Recursively merge nested objects
                } else {
                    result.add(key, targetValue); // Keep target's value
                }
            } else {
                result.add(key, entry.getValue()); // Key exists only in origin, add to target
            }
        }

        // Add remaining keys from target that are not present in origin
        for (Map.Entry<String, JsonElement> entry : target.entrySet()) {
            final String key = entry.getKey();
            if (!processedKeys.contains(key)) {
                result.add(key, entry.getValue());
            }
        }

        return result;
    }
}
