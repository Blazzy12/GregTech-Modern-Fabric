package com.gregtechceu.gtceu.compat.client.model;

import java.util.List;

/**
 * Transforms {@link BakedQuad}s in place. The constants describe the vertex layout of {@link BakedQuad#getVertices()}.
 */
@FunctionalInterface
public interface IQuadTransformer {

    int STRIDE = 8;
    int POSITION = 0;
    int COLOR = 3;
    int UV0 = 4;
    int UV1 = 6;
    int UV2 = 6;
    int NORMAL = 7;

    void processInPlace(BakedQuad quad);

    default void processInPlace(List<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
            processInPlace(quad);
        }
    }

    /** @return a transformed copy of the quad */
    default BakedQuad process(BakedQuad quad) {
        BakedQuad copy = new BakedQuad(quad.getVertices().clone(), quad.getTintIndex(), quad.getDirection(),
                quad.getSprite(), quad.isShade(), quad.getLightEmission(), quad.hasAmbientOcclusion())
                .copyMetadataFrom(quad);
        processInPlace(copy);
        return copy;
    }

    default List<BakedQuad> process(List<BakedQuad> quads) {
        return quads.stream().map(this::process).toList();
    }

    default IQuadTransformer andThen(IQuadTransformer other) {
        return quad -> {
            processInPlace(quad);
            other.processInPlace(quad);
        };
    }
}
