package io.github.milkdrinkers.wordweaver.storage;

public class LanguageLoadException extends RuntimeException {
    public LanguageLoadException(String message) {
        super(message);
    }

    public LanguageLoadException(String message, Exception exception) {
        super(message, exception);
    }

    public LanguageLoadException(Exception exception) {
        super(exception);
    }
}
