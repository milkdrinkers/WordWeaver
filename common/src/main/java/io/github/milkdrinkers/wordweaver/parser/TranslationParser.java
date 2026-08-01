package io.github.milkdrinkers.wordweaver.parser;

import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.TranslationLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Parses a single file format into bundle entries.
 * <p>
 * Implementations are registered through the {@link java.util.ServiceLoader} by declaring themselves in
 * {@code META-INF/services/io.github.milkdrinkers.wordweaver.parser.TranslationParser}, or explicitly added through
 * {@link io.github.milkdrinkers.wordweaver.config.TranslationConfig.Builder#parser(TranslationParser)}.
 *
 * @see io.github.milkdrinkers.wordweaver.storage.TranslationBundle
 */
public interface TranslationParser {
    /**
     * The file extensions this parser handles, lowercase and without a leading dot (e.g. {@code "json"}, {@code "jsonc"}).
     *
     * @return The handled extensions
     */
    Set<String> extensions();

    /**
     * Parse a bundle file into entries.
     *
     * @param file The file to parse
     * @return A map of entries keyed by their path
     * @throws TranslationLoadException If the file cannot be read or is malformed
     */
    Map<String, TranslationBundleEntry> parse(Path file) throws TranslationLoadException;

    /**
     * Whether this parser can add missing keys from a shipped origin file into an already extracted file.
     *
     * @return True if {@link #merge(InputStream, Path)} is supported
     * @implNote Defaults to false
     */
    default boolean supportsMerge() {
        return false;
    }

    /**
     * Add keys present in the shipped origin but missing from the target, preserving the targets existing values.
     *
     * @param origin The shipped origin file as a resource stream
     * @param target The extracted file on disk to update
     * @throws IOException If an I/O error occurs
     * @implNote Defaults to a no op
     */
    default void merge(InputStream origin, Path target) throws IOException {
    }
}
