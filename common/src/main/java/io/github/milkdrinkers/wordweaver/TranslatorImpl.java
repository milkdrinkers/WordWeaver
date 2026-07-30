package io.github.milkdrinkers.wordweaver;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;

public class TranslatorImpl implements Translator {
    private final Key key;

    public TranslatorImpl(TranslationConfig config) {
        // noinspection PatternValidation
        this.key = Key.key(config.getNamespace());
    }

    @Override
    public @NotNull Key name() {
        return this.key;
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return null;
    }

    @Override
    public @Nullable Component translate(@NotNull TranslatableComponent data, @NotNull Locale locale) {
        return TranslationProvider.getInstance()
            .getTranslationService()
            .getComponent(locale, data.key(), null);
    }
}
