package me.wolfii.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.font.UnbakedGlyph;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FontSet.class)
public class FontSetMixin {
    @WrapOperation(method = "lambda$selectProviders$0", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;computeIfAbsent(ILit/unimi/dsi/fastutil/ints/Int2ObjectFunction;)Ljava/lang/Object;"))
    private <V> V ensureIntegerWidth(
        Int2ObjectMap<V> instance,
        int key,
        Int2ObjectFunction<? extends V> mappingFunction,
        Operation<V> original,
        @Local(name = "glyph") UnbakedGlyph glyph
    ) {
        float width = glyph.info().getAdvance(false);
        if (Math.abs(width - Math.round(width)) < 0.001f) {
            return original.call(instance, key, mappingFunction);
        }
        return (V) new IntArrayList();
    }
}
