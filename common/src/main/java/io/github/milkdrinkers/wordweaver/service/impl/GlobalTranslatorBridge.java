package io.github.milkdrinkers.wordweaver.service.impl;

import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundle;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Bridges WordWeaver into Adventures {@link GlobalTranslator} so translations resolve for {@link net.kyori.adventure.text.TranslatableComponent}s in the viewers locale.
 */
final class GlobalTranslatorBridge extends MiniMessageTranslator {
    private final Key name;
    private final TranslationBundleRegistry registry;

    GlobalTranslatorBridge(TranslationConfig config, TranslationBundleRegistry registry) {
        super(config.getMiniMessage());
        // noinspection PatternValidation
        this.name = Key.key(config.getNamespace());
        this.registry = registry;
    }

    /**
     * Register this bridge as a source on the global translator.
     */
    void register() {
        GlobalTranslator.translator().addSource(this);
    }

    /**
     * Remove this bridge from the global translator.
     */
    void unregister() {
        GlobalTranslator.translator().removeSource(this);
    }

    @Override
    public @NotNull Key name() {
        return name;
    }

    @Override
    public @Nullable String getMiniMessageString(@NotNull String key, @NotNull Locale locale) {
        TranslationBundleEntry entry = entryFor(registry.get(locale), key);
        if (entry == null)
            entry = entryFor(registry.getDefault(), key);

        return entry == null ? null : entry.getValue();
    }

    private static @Nullable TranslationBundleEntry entryFor(@Nullable TranslationBundle bundle, String key) {
        return bundle == null ? null : bundle.getEntry(key);
    }
}
