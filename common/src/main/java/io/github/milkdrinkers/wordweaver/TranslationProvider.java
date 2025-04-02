package io.github.milkdrinkers.wordweaver;

import io.github.milkdrinkers.wordweaver.service.TranslationService;

/**
 * Singleton provider for the translation service
 */
public final class TranslationProvider {
    private static TranslationProvider INSTANCE;
    private final TranslationService translationService;

    private TranslationProvider(TranslationService service) {
        this.translationService = service;
    }

    /**
     * Get the singleton instance of the translation provider
     */
    static TranslationProvider getInstance() {
        if (INSTANCE == null)
            throw new IllegalStateException("Translation provider has not been initialized");

        return INSTANCE;
    }

    /**
     * Initialize the translation provider with the given service
     */
    static synchronized void initialize(TranslationService service) {
        if (INSTANCE != null)
            throw new IllegalStateException("Translation provider already initialized");

        INSTANCE = new TranslationProvider(service);
    }

    /**
     * Get the translation service
     */
    TranslationService getTranslationService() {
        return translationService;
    }
}