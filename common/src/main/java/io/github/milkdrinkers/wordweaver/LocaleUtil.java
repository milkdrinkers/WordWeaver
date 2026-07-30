package io.github.milkdrinkers.wordweaver;

import java.util.Locale;

public final class LocaleUtil {
    /**
     * Convert a <a href="https://gist.github.com/typpo/b2b828a35e683b9bf8db91b5404f1bd1">BCP 47</a> language tag to a {@link Locale} object. This method replaces underscores with hyphens before parsing the language tag, to handle legacy Java formats.
     *
     * @param languageTag The BCP 47 language tag to convert
     * @return The corresponding Locale object
     */
    public static Locale serialize(String languageTag) {
        return Locale.forLanguageTag(languageTag.replace('_', '-'));
    }

    /**
     * Convert a {@link Locale} object to a <a href="https://gist.github.com/typpo/b2b828a35e683b9bf8db91b5404f1bd1">BCP 47</a> language tag. This method replaces hyphens with underscores in the resulting string, so the final format is ("en_US", "xx_XX").
     *
     * @param locale The Locale object to convert
     * @return A BCP 47 language tag
     */
    public static String deserialize(Locale locale) {
        return locale.toLanguageTag().replace('-', '_');
    }
}
