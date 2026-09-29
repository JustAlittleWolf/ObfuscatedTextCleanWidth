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

    public static void filterGlyphsByWidth(Int2ObjectMap<IntList> glyphsByWidth, List<GlyphProvider> providers) {
        for (int targetWidth : glyphsByWidth.keySet().toIntArray()) {
            IntList codepoints = glyphsByWidth.get(targetWidth);
            if (codepoints == null || codepoints.isEmpty()) {
                continue;
            }

            List<GlyphEntry> glyphEntries = codepoints.intStream()
                .mapToObj(codepoint -> {
                        float glyphWidth = providers.stream()
                            .map(p -> p.getGlyph(codepoint))
                            .filter(Objects::nonNull)
                            .findFirst()
                            .map(glyph -> glyph.info().getAdvance(false))
                            .orElse(0.0f);
                        return new GlyphEntry(
                            codepoint,
                            glyphWidth
                        );
                    }
                )
                .sorted(Comparator.comparingDouble(GlyphEntry::width))
                .toList();
            List<CodepointWidthGroup> entriesGroupedByWidth = new ArrayList<>();
            for (GlyphEntry glyphEntry : glyphEntries) {
                if (entriesGroupedByWidth.isEmpty() || glyphEntry.width - entriesGroupedByWidth.getLast().width > ObfuscatedTextCleanWidthFilter.MAX_GLYPH_WIDTH_CHANGE) {
                    entriesGroupedByWidth.add(new CodepointWidthGroup(glyphEntry.width));
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
        entriesGroupedByWidth.sort(Comparator.comparingDouble(g -> Math.abs(g.width - targetWidth)));

        for (CodepointWidthGroup group : entriesGroupedByWidth) {
            if (group.codepoints.size() > 10) {
                return group;
            }
        }
        return entriesGroupedByWidth.stream()
            .max(Comparator.comparingInt(group -> group.codepoints.size()))
            .orElse(null);
    }

    private record GlyphEntry(int codepoint, float width) {
    }

    private static class CodepointWidthGroup {
        final float width;
        final IntList codepoints = new IntArrayList();

        CodepointWidthGroup(float width) {
            this.width = width;
        }
    }
}
