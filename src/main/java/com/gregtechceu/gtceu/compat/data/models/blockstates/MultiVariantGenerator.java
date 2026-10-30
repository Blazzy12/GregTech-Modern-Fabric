package com.gregtechceu.gtceu.compat.data.models.blockstates;

import net.minecraft.world.level.block.Block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/** A {@code "variants"} blockstate, combining property dispatches with base variants. */
public class MultiVariantGenerator implements BlockStateGenerator {

    private final Block block;
    private final List<Variant> baseVariants;
    private final List<PropertyDispatch> declaredProperties = new ArrayList<>();

    private MultiVariantGenerator(Block block, List<Variant> baseVariants) {
        this.block = block;
        this.baseVariants = baseVariants;
    }

    public static MultiVariantGenerator multiVariant(Block block) {
        return new MultiVariantGenerator(block, List.of(Variant.variant()));
    }

    public static MultiVariantGenerator multiVariant(Block block, Variant variant) {
        return new MultiVariantGenerator(block, List.of(variant));
    }

    public static MultiVariantGenerator multiVariant(Block block, Variant... variants) {
        return new MultiVariantGenerator(block, List.of(variants));
    }

    public MultiVariantGenerator with(PropertyDispatch dispatch) {
        declaredProperties.add(dispatch);
        return this;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public JsonElement get() {
        Map<String, List<Variant>> merged = new LinkedHashMap<>();
        merged.put("", baseVariants);
        for (PropertyDispatch dispatch : declaredProperties) {
            Map<String, List<Variant>> next = new LinkedHashMap<>();
            merged.forEach((key, variants) -> dispatch.getEntries().forEach((dKey, dVariants) -> {
                String combined = key.isEmpty() ? dKey : key + "," + dKey;
                List<Variant> out = new ArrayList<>();
                for (Variant base : variants) for (Variant v : dVariants) out.add(Variant.merge(base, v));
                next.put(combined, out);
            }));
            merged = next;
        }
        JsonObject variants = new JsonObject();
        merged.forEach((key, list) -> variants.add(key, Variant.convertList(list)));
        JsonObject json = new JsonObject();
        json.add("variants", variants);
        return json;
    }
}
