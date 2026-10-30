package com.gregtechceu.gtceu.compat.client.model;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/** An immutable set of chunk render layers, like NeoForge's. */
public final class ChunkRenderTypeSet implements Iterable<ChunkSectionLayer> {

    private static final ChunkRenderTypeSet NONE = new ChunkRenderTypeSet(EnumSet.noneOf(ChunkSectionLayer.class));
    private static final ChunkRenderTypeSet ALL = new ChunkRenderTypeSet(EnumSet.allOf(ChunkSectionLayer.class));

    private final EnumSet<ChunkSectionLayer> layers;

    private ChunkRenderTypeSet(EnumSet<ChunkSectionLayer> layers) {
        this.layers = layers;
    }

    public static ChunkRenderTypeSet none() {
        return NONE;
    }

    public static ChunkRenderTypeSet all() {
        return ALL;
    }

    public static ChunkRenderTypeSet of(ChunkSectionLayer... layers) {
        return of(Arrays.asList(layers));
    }

    public static ChunkRenderTypeSet of(Collection<ChunkSectionLayer> layers) {
        if (layers.isEmpty()) return NONE;
        return new ChunkRenderTypeSet(EnumSet.copyOf(layers));
    }

    public static ChunkRenderTypeSet union(ChunkRenderTypeSet... sets) {
        return union(Arrays.asList(sets));
    }

    public static ChunkRenderTypeSet union(Collection<ChunkRenderTypeSet> sets) {
        EnumSet<ChunkSectionLayer> layers = EnumSet.noneOf(ChunkSectionLayer.class);
        for (ChunkRenderTypeSet set : sets) {
            layers.addAll(set.layers);
        }
        return layers.isEmpty() ? NONE : new ChunkRenderTypeSet(layers);
    }

    public boolean isEmpty() {
        return layers.isEmpty();
    }

    public boolean contains(ChunkSectionLayer layer) {
        return layers.contains(layer);
    }

    public List<ChunkSectionLayer> asList() {
        return List.copyOf(layers);
    }

    @Override
    public Iterator<ChunkSectionLayer> iterator() {
        return layers.iterator();
    }
}
