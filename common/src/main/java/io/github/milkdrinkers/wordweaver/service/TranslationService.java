package io.github.milkdrinkers.wordweaver.service;

import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Set;

/**
 * Service interface for translation functionality
 */
public interface TranslationService {
    String getString(String key);

    String getString(String key, String fallback);

    List<String> getStringList(String key);

    List<String> getStringList(String key, List<String> fallback);

    Component getComponent(String key);

    Component getComponent(String key, Component fallback);

    List<Component> getComponentList(String key);

    List<Component> getComponentList(String key, List<Component> fallback);

    Set<String> getKeys();

    /**
     * Set the default/fallback language
     */
    void setDefaultLanguage(String language);

    /**
     * Get the default/fallback language
     */
    String getDefaultLanguage();

    /**
     * Set the active language
     */
    void setLanguage(String language);

    /**
     * Get the active language
     */
    String getLanguage();

    /**
     * Reload all translations
     */
    void reload();
}