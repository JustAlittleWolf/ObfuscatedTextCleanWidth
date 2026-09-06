package me.wolfii.obfuscatedtextcleanwidth.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.font.UnbakedGlyph;
import it.unimi.dsi.fastutil.ints.IntList;
import me.wolfii.obfuscatedtextcleanwidth.ObfuscatedTextCleanWidthFilter;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FontSet.class)
public class FontSetMixin {
    @SuppressWarnings("WrapWithConditionTargetsNonVoid")
    @WrapWithCondition(
        method = "lambda$selectProviders$0",
        at = @At(
            value = "INVOKE",
            target = "Lit/unimi/dsi/fastutil/ints/IntList;add(I)Z"
        )
    )
    private boolean qualifiesAsObfuscationChar(
        IntList instance,
        int codepoint,
        @Local(name = "glyph") UnbakedGlyph glyph
    ) {
        return ObfuscatedTextCleanWidthFilter.qualifiesAsObfuscationChar(glyph);
    }
}
