package io.github.milkdrinkers.wordweaver.parser;

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
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Parser for {@code .properties} files.
 */
public class PropertiesTranslationParser implements TranslationParser {
    private static final Set<String> EXTENSIONS = Collections.singleton("properties");

    @Override
    public Set<String> extensions() {
        return EXTENSIONS;
    }

    @Override
    public Map<String, TranslationBundleEntry> parse(Path file) throws TranslationLoadException {
        final Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new TranslationLoadException("Failed to read properties file!", e);
        }

        final Map<String, TranslationBundleEntry> entries = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            entries.put(key, new TranslationBundleEntryImpl(TranslationBundleEntry.Type.STRING, properties.getProperty(key)));
        }

        return entries;
    }

    @Override
    public boolean supportsMerge() {
        return true;
    }

    @Override
    public void merge(InputStream origin, Path target) throws IOException {
        final Properties originProperties = new Properties();
        try (Reader reader = new InputStreamReader(origin, StandardCharsets.UTF_8)) {
            originProperties.load(reader);
        }

        final Properties targetProperties = new Properties();
        try (Reader reader = Files.newBufferedReader(target, StandardCharsets.UTF_8)) {
            targetProperties.load(reader);
        }

        // Collect keys present in the origin but missing from the target
        final List<String> missingKeys = new ArrayList<>();
        for (String key : originProperties.stringPropertyNames()) {
            if (!targetProperties.containsKey(key))
                missingKeys.add(key);
        }

        if (missingKeys.isEmpty())
            return;

        // Append the missing keys to the end of the target file, preserving its existing content
        final StringBuilder appended = new StringBuilder();

        final byte[] existing = Files.readAllBytes(target);
        if (existing.length > 0 && existing[existing.length - 1] != '\n')
            appended.append(System.lineSeparator());

        for (String key : missingKeys) {
            appended.append(escape(key, true))
                .append('=')
                .append(escape(originProperties.getProperty(key), false))
                .append(System.lineSeparator());
        }

        Files.write(target, appended.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    }

    /**
     * Escapes a properties key or value the way {@link Properties} would keeping UTF-8 characters intact.
     */
    private static String escape(String value, boolean isKey) {
        final StringBuilder builder = new StringBuilder(value.length());

        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);

            switch (c) {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case ' ':
                    if (isKey || i == 0) // Escape spaces in keys, and a leading space in values
                        builder.append("\\ ");
                    else
                        builder.append(' ');
                    break;
                case '=':
                case ':':
                case '#':
                case '!':
                    if (isKey)
                        builder.append('\\').append(c);
                    else
                        builder.append(c);
                    break;
                default:
                    builder.append(c);
            }
        }

        return builder.toString();
    }
}
