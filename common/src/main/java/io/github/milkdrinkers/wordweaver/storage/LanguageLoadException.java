package io.github.milkdrinkers.wordweaver.storage;

public class LanguageLoadException extends RuntimeException {
    private static final long serialVersionUID = 3L;

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
