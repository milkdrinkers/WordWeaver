package io.github.milkdrinkers.wordweaver.service.impl;

import io.github.milkdrinkers.wordweaver.LocaleUtil;
import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import io.github.milkdrinkers.wordweaver.storage.TranslationBundleEntry;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleEntryImpl;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleImpl;
import io.github.milkdrinkers.wordweaver.storage.impl.TranslationBundleRegistryImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalTranslatorBridgeTest {
    private static final Locale EN = Locale.forLanguageTag("en-US");
    private static final Locale FR = Locale.forLanguageTag("fr-FR");

    private GlobalTranslatorBridge bridge;

    @BeforeEach
    void setUp() {
        final TranslationConfig config = TranslationConfig.builder()
            .namespace("wordweaver:test")
            .translationDirectory(Paths.get("unused"))
            .locale("en_US")
            .defaultLocale("en_US")
            .build();

        final Map<String, TranslationBundleEntry> entries = new HashMap<>();
        entries.put("greeting", new TranslationBundleEntryImpl(TranslationBundleEntry.Type.STRING, "<red>Hello</red>"));
        entries.put("indexed", new TranslationBundleEntryImpl(TranslationBundleEntry.Type.STRING, "Hello, <arg:0>!"));
        entries.put("named", new TranslationBundleEntryImpl(TranslationBundleEntry.Type.STRING, "Hello, <name>!"));

        final TranslationBundleRegistryImpl registry = new TranslationBundleRegistryImpl(config);
        registry.register(new TranslationBundleImpl(LocaleUtil.fromTag("en_US"), entries));

        bridge = new GlobalTranslatorBridge(config, registry);
    }

    /**
     * Flattens a component tree to its plain text, ignoring styling, for content assertions.
     */
    private static String plain(Component component) {
        final StringBuilder builder = new StringBuilder();
        if (component instanceof TextComponent)
            builder.append(((TextComponent) component).content());
        for (Component child : component.children())
            builder.append(plain(child));
        return builder.toString();
    }

    @Test
    void resolvesKnownKeyAsMiniMessage() {
        final Component result = bridge.translate(Component.translatable("greeting"), EN);

        assertNotNull(result);
        // MiniMessage tags are parsed away, leaving the plain content
        assertEquals("Hello", plain(result));
    }

    @Test
    void returnsNullForUnknownKey() {
        assertNull(bridge.translate(Component.translatable("block.minecraft.stone"), EN));
    }

    @Test
    void fallsBackToDefaultLocale() {
        final Component result = bridge.translate(Component.translatable("greeting"), FR);

        assertNotNull(result);
        assertEquals("Hello", plain(result));
    }

    @Test
    void substitutesIndexedArgument() {
        final Component result = bridge.translate(Component.translatable("indexed", Component.text("Kezz")), EN);

        assertEquals("Hello, Kezz!", plain(result));
    }

    @Test
    void substitutesNamedArgument() {
        final TranslatableComponent component = Component.translatable("named", Argument.component("name", Component.text("Kezz")));

        final Component result = bridge.translate(component, EN);

        assertEquals("Hello, Kezz!", plain(result));
    }

    @Test
    void preservesChildren() {
        final TranslatableComponent component = Component.translatable("greeting")
            .children(Collections.singletonList(Component.text("!")));

        final Component result = bridge.translate(component, EN);

        assertEquals("Hello!", plain(result));
    }
}
