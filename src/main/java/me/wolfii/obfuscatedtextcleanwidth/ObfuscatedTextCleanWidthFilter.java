package me.wolfii.obfuscatedtextcleanwidth;

import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ObfuscatedTextCleanWidthFilter {
    private static final float MAX_GLYPH_WIDTH_CHANGE = 0.001f;
    private static final int OPTIMAL_MINIMUM_GLYPHS_PER_GROUP = 10;

    public static void filterGlyphsByWidth(Int2ObjectMap<IntList> glyphsByWidth, List<GlyphProvider> providers) {
        for (int targetWidth : glyphsByWidth.keySet().toIntArray()) {
            IntList codepoints = glyphsByWidth.get(targetWidth);
            if (codepoints == null || codepoints.isEmpty()) {
                continue;
            }

            List<GlyphEntry> glyphEntries = codepoints.intStream()
                .mapToObj(codepoint -> providers.stream()
                    .map(p -> p.getGlyph(codepoint))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(glyph -> new GlyphEntry(codepoint, glyph.info().getAdvance(false), glyph.info().getAdvance(true)))
                    .orElseGet(() -> new GlyphEntry(codepoint, 0.0f, 0.0f))
                )
                .sorted(Comparator.comparingDouble(GlyphEntry::width).thenComparingDouble(GlyphEntry::boldWidth))
                .toList();
            List<CodepointWidthGroup> entriesGroupedByWidth = new ArrayList<>();
            for (GlyphEntry glyphEntry : glyphEntries) {
                if (entriesGroupedByWidth.isEmpty()
                    || glyphEntry.width - entriesGroupedByWidth.getLast().width > ObfuscatedTextCleanWidthFilter.MAX_GLYPH_WIDTH_CHANGE
                    || Math.abs(glyphEntry.boldWidth - entriesGroupedByWidth.getLast().boldWidth) > ObfuscatedTextCleanWidthFilter.MAX_GLYPH_WIDTH_CHANGE) {
                    entriesGroupedByWidth.add(new CodepointWidthGroup(glyphEntry.width, glyphEntry.boldWidth));
                }
                entriesGroupedByWidth.getLast().codepoints.add(glyphEntry.codepoint);
            }

            CodepointWidthGroup bestMatch = ObfuscatedTextCleanWidthFilter.getBestMatch(entriesGroupedByWidth, targetWidth);
            if (bestMatch == null) {
                continue;
            }
            codepoints.clear();
            codepoints.addAll(bestMatch.codepoints);
        }
    }

    private static @Nullable CodepointWidthGroup getBestMatch(List<CodepointWidthGroup> entriesGroupedByWidth, int targetWidth) {
        entriesGroupedByWidth.sort(
            Comparator.<CodepointWidthGroup>comparingDouble(group -> Math.abs(group.width - targetWidth))
                .thenComparingDouble(group -> Math.abs(group.boldWidth - (targetWidth + 1.0f)))
        );

        for (CodepointWidthGroup group : entriesGroupedByWidth) {
            if (group.codepoints.size() >= ObfuscatedTextCleanWidthFilter.OPTIMAL_MINIMUM_GLYPHS_PER_GROUP) {
                return group;
            }
        }
        return entriesGroupedByWidth.stream()
            .max(Comparator.comparingInt(group -> group.codepoints.size()))
            .orElse(null);
    }

    private record GlyphEntry(int codepoint, float width, float boldWidth) {
    }

    private static class CodepointWidthGroup {
        final float width;
        final float boldWidth;
        final IntList codepoints = new IntArrayList();

        CodepointWidthGroup(float width, float boldWidth) {
            this.width = width;
            this.boldWidth = boldWidth;
        }
    }
}
