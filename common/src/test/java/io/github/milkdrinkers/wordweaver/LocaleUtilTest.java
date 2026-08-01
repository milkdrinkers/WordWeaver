package io.github.milkdrinkers.wordweaver;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static io.github.milkdrinkers.wordweaver.LocaleUtil.fromTag;
import static io.github.milkdrinkers.wordweaver.LocaleUtil.toTag;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleUtilTest {
    @Test
    void fromTagAcceptsUnderscoreAndHyphen() {
        assertEquals(Locale.forLanguageTag("en-US"), fromTag("en_US"));
        assertEquals(Locale.forLanguageTag("en-US"), fromTag("en-US"));
    }

    @Test
    void toTagUsesUnderscores() {
        assertEquals("en_US", toTag(Locale.forLanguageTag("en-US")));
        assertEquals("sv_SE", toTag(Locale.forLanguageTag("sv-SE")));
    }

    @Test
    void roundTripsBothSeparators() {
        assertEquals("fr_FR", toTag(fromTag("fr_FR")));
        assertEquals("fr_FR", toTag(fromTag("fr-FR")));
    }
}
