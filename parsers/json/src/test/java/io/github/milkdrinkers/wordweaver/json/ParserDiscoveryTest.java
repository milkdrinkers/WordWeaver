package io.github.milkdrinkers.wordweaver.json;

import io.github.milkdrinkers.wordweaver.parser.TranslationParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that with both the core (properties) and JSON modules on the classpath, the {@link ServiceLoader}
 * discovers every parser - the descriptors do not override each other.
 */
class ParserDiscoveryTest {
    @Test
    void discoversBothJsonAndPropertiesParsers() {
        final Set<String> parserNames = new HashSet<>();
        final Set<String> extensions = new HashSet<>();

        for (TranslationParser parser : ServiceLoader.load(TranslationParser.class, TranslationParser.class.getClassLoader())) {
            parserNames.add(parser.getClass().getSimpleName());
            extensions.addAll(parser.extensions());
        }

        assertTrue(parserNames.contains("JsonTranslationParser"), "JSON parser should be discovered");
        assertTrue(parserNames.contains("PropertiesTranslationParser"), "Properties parser should be discovered");
        assertTrue(extensions.containsAll(Arrays.asList("json", "jsonc", "properties")), "All extensions should be covered");
    }
}
