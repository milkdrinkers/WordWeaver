package io.github.milkdrinkers.wordweaver.storage;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for a language
 * <p>
 * A language is a collection of language entries.
 * It is used to manage and access language entries in WordWeaver.
 * <p>
 * This interface is used by the {@link LanguageRegistry} to load and manage translations/{@link LanguageEntry}'s.
 * @see LanguageRegistry
 */
public interface Language {
    /**
     * Get the name of this language
     *
     * @return The name of this language
     * @implNote Follows naming convention from <a href="https://minecraft.gamepedia.com/Language">Minecraft Wiki</a>. Ie, (xx_XX).
     */
    String getName();

    /**
     * Get the entries in this language
     *
     * @return A map of entries
     */
    Map<String, LanguageEntry> get();

    /**
     * Get the entry for the given key
     *
     * @param key The key to get the entries for
     * @return The entry for the given key
     */
    @Nullable LanguageEntry get(String key);

    /**
     * Get the entry for the given key
     *
     * @param key The key to get the entry for
     * @return The entry for the given key
     */
    Optional<LanguageEntry> getOptional(String key);

    /**
     * Check if the language contains a entry for the given key
     *
     * @param key The key to check
     * @return True if the language contains a entry for the given key, false otherwise
     */
    boolean has(String key);

    /**
     * Add a entry to the language
     * @param key The key to add
     * @param entry The entry to add
     */
    void add(String key, LanguageEntry entry);

    /**
     * Remove a entry from the language
     * @param key The key to remove
     */
    void remove(String key);

    /**
     * Get all keys in this language
     * @return A set of all keys in this language
     */
    Set<String> keys();
}
