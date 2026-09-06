package me.wolfii.obfuscatedtextcleanwidth;

import com.mojang.blaze3d.font.UnbakedGlyph;

public class ObfuscatedTextCleanWidthFilter {
    public static boolean qualifiesAsObfuscationChar(UnbakedGlyph unbakedGlyph) {
        float normalWidth = unbakedGlyph.info().getAdvance(false);
        float boldWidth = unbakedGlyph.info().getAdvance(true);
        return isInteger(normalWidth) && isInteger(boldWidth);
    }

    private static boolean isInteger(float value) {
        return Math.abs(value - Math.round(value)) < 0.001f;
    }
}
