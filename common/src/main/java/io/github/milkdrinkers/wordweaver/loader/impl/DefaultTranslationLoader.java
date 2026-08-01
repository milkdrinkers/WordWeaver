package io.github.milkdrinkers.wordweaver.loader.impl;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.loader.TranslationLoader;
import io.github.milkdrinkers.wordweaver.parser.TranslationParser;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundle;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
import io.github.milkdrinkers.wordweaver.storage.TranslationLoadException;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static io.github.milkdrinkers.wordweaver.LocaleUtil.fromTag;

/**
 * Loads bundles using the registered {@link TranslationParser}s, one per file extension.
 * <p>
 * Parsers are auto discovered through {@link ServiceLoader} and may be added or overridden through the
 * {@link TranslationConfig}. When two discovered parsers claim the same extension the first one wins. An explicitly
 * configured parser always overrides a discovered one.
 */
public class DefaultTranslationLoader implements TranslationLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTranslationLoader.class);

    private final TranslationConfig config;
    private final TranslationBundleRegistry registry;
    private final Map<String, TranslationParser> parsers;

    public DefaultTranslationLoader(TranslationConfig config, TranslationBundleRegistry registry) {
        this.config = config;
        this.registry = registry;
        this.parsers = resolveParsers(config);
    }

    private static Map<String, TranslationParser> resolveParsers(TranslationConfig config) {
        final Map<String, TranslationParser> resolved = new HashMap<>();

        // Auto-discovered parsers, first registration for an extension wins
        for (TranslationParser parser : ServiceLoader.load(TranslationParser.class, TranslationParser.class.getClassLoader())) {
            for (String extension : parser.extensions()) {
                final String ext = extension.toLowerCase();
                final TranslationParser existing = resolved.putIfAbsent(ext, parser);
                if (existing != null)
                    LOGGER.warn("Multiple parsers registered for extension '{}', keeping {}", ext, existing.getClass().getName());
            }
        }

        // Explicitly configured parsers override discovered ones
        for (TranslationParser parser : config.getParsers()) {
            for (String extension : parser.extensions()) {
                resolved.put(extension.toLowerCase(), parser);
            }
        }

        if (resolved.isEmpty())
            LOGGER.warn("No translation parsers registered. Add a parser module (e.g. wordweaver-json) or configure one.");

        return resolved;
    }

    @Override
    public void extractMissingBundles() throws IOException {
        if (parsers.isEmpty())
            return;

        try {
            FileExtractor.extractMissingResources(config.getTranslationDirectory(), config.getResourcesDirectory(), parsers.keySet());
        } catch (RuntimeException e) {
            LOGGER.error("Failed to extract missing bundle files: ", e);
            throw e;
        }
    }

    @Override
    public void updateExistingBundles() throws IOException {
        if (parsers.isEmpty())
            return;

        try {
            final Path outputDir = config.getTranslationDirectory();
            final List<Path> resourceFiles = FileExtractor.findResourceFiles(config.getResourcesDirectory(), parsers.keySet());

            for (Path resourcePath : resourceFiles) {
                final String fileName = resourcePath.getFileName().toString();
                final Path targetFile = outputDir.resolve(fileName);

                if (!Files.exists(targetFile))
                    continue;

                final TranslationParser parser = parsers.get(extensionOf(fileName));
                if (parser == null || !parser.supportsMerge())
                    continue;

                try (InputStream origin = FileExtractor.openResource(resourcePath)) {
                    if (origin == null)
                        continue;

                    parser.merge(origin, targetFile);
                }
            }
        } catch (RuntimeException e) {
            LOGGER.error("Failed to update existing bundle files: ", e);
            throw e;
        }
    }

    @Override
    public void loadBundles() throws IOException {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(config.getTranslationDirectory());

            // Load each bundle file with a known extension
            try (Stream<Path> files = Files.list(config.getTranslationDirectory())) {
                files.filter(path -> parsers.containsKey(extensionOf(path.getFileName().toString())))
                    .forEach(this::load);
            }
        } catch (RuntimeException e) {
            LOGGER.error("Failed to load bundle files: ", e);
            throw e;
        }
    }

    private void load(Path file) throws TranslationLoadException {
        try {
            final String fileName = file.getFileName().toString();
            final String localeTag = fileName.substring(0, fileName.lastIndexOf('.'));
            final TranslationParser parser = parsers.get(extensionOf(fileName));

            final Map<String, TranslationBundleEntry> entries = ReferenceResolver.resolve(parser.parse(file));
            final TranslationBundle bundle = new TranslationBundleImpl(fromTag(localeTag), entries);

            registry.register(bundle);
        } catch (TranslationLoadException e) {
            LOGGER.error("Failed to load bundle file: {}", file.getFileName(), e);
            throw e;
        }
    }

    private static String extensionOf(String fileName) {
        final int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }
}
