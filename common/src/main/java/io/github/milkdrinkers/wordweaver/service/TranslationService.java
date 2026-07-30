package io.github.milkdrinkers.wordweaver.service;

import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Service interface for translation functionality
 */
public interface TranslationService {
    @SuppressWarnings("unused")
    default String getString(String key) {
        return getString(key, null);
    }

    default String getString(String key, String fallback) {
        return getString(getLocale(), key, fallback);
    }

    @SuppressWarnings("unused")
    default List<String> getStringList(String key) {
        return getStringList(key, null);
    }

    default List<String> getStringList(String key, List<String> fallback) {
        return getStringList(getLocale(), key, fallback);
    }

    @SuppressWarnings("unused")
    default Component getComponent(String key) {
        return getComponent(key, null);
    }

    default Component getComponent(String key, Component fallback) {
        return getComponent(getLocale(), key, fallback);
    }

    @SuppressWarnings("unused")
    default List<Component> getComponentList(String key) {
        return getComponentList(key, null);
    }

    default List<Component> getComponentList(String key, List<Component> fallback) {
        return getComponentList(getLocale(), key, fallback);
    }

    @SuppressWarnings("unused")
    default String getString(Locale locale, String key) {
        return getString(locale, key, null);
    }

    String getString(Locale locale, String key, String fallback);

    @SuppressWarnings("unused")
    default List<String> getStringList(Locale locale, String key) {
        return getStringList(locale, key, null);
    }

    List<String> getStringList(Locale locale, String key, List<String> fallback);

    @SuppressWarnings("unused")
    default Component getComponent(Locale locale, String key) {
        return getComponent(locale, key, null);
    }

    Component getComponent(Locale locale, String key, Component fallback);

    @SuppressWarnings("unused")
    default List<Component> getComponentList(Locale locale, String key) {
        return getComponentList(locale, key, null);
    }

    List<Component> getComponentList(Locale locale, String key, List<Component> fallback);

    Set<String> getKeys();

    /**
     * Set the default/fallback locale
     */
    void setDefaultLocale(Locale locale);

    /**
     * Get the default/fallback locale
     */
    Locale getDefaultLocale();

    /**
     * Set the active locale
     */
    void setLocale(Locale locale);

    /**
     * Get the active locale
     */
    Locale getLocale();

    /**
     * Reload all translations
     */
    void reload();
}