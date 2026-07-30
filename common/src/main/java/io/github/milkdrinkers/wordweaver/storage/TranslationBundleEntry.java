package io.github.milkdrinkers.wordweaver.storage;

import java.util.List;

/**
 * Represents an entry inside a bundle.
 * <p>
 * This interface is used to represent a single translation entry within a {@link TranslationBundle}. It is used to
 * provide a common interface for all entries, regardless of their native json type.
 *
 * @see TranslationBundle
 */
public interface TranslationBundleEntry {
    enum Type {
        STRING,
        LIST
    }

    /**
     * Get the type of this entry.
     *
     * @return The type of this entry.
     */
    Type getType();

    /**
     * Get the value of this entry.
     *
     * @return The value of this entry.
     */
    String getValue();

    /**
     * Get the values of this entry.
     *
     * @return The values of this entry.
     */
    List<String> getValues();

    /**
     * Returns if this entry represents a collection.
     *
     * @return True if this entry is a collection, false otherwise.
     */
    boolean isCollection();
}
