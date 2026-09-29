package me.wolfii.obfuscatedtextcleanwidth.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import me.wolfii.obfuscatedtextcleanwidth.ObfuscatedTextCleanWidthFilter;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Set;

@Mixin(
    value = FontSet.class,
    priority = 1100
)
public class FontSetMixin {
    @Shadow
    @Final
    private Int2ObjectMap<IntList> glyphsByWidth;

    @WrapMethod(method = "selectProviders")
    private List<GlyphProvider> filterAllowedGlyphsByWidth(
        List<GlyphProvider.Conditional> providers,
        Set<FontOption> options,
        Operation<List<GlyphProvider>> original
    ) {
        List<GlyphProvider> result = original.call(providers, options);
        ObfuscatedTextCleanWidthFilter.filterGlyphsByWidth(this.glyphsByWidth, result);
        return result;
    }
}
