package io.github.milkdrinkers.wordweaver.storage;

public class TranslationLoadException extends RuntimeException {
    private static final long serialVersionUID = 3L;

    public TranslationLoadException(String message) {
        super(message);
    }

    public TranslationLoadException(String message, Exception exception) {
        super(message, exception);
    }

    public TranslationLoadException(Exception exception) {
        super(exception);
    }
}
