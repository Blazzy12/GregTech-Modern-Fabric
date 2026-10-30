package com.gregtechceu.gtceu.compat.data.models.blockstates;

import net.minecraft.world.level.block.Block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/** A {@code "multipart"} blockstate. */
public class MultiPartGenerator implements BlockStateGenerator {

    private final Block block;
    private final List<JsonObject> parts = new ArrayList<>();

    private MultiPartGenerator(Block block) {
        this.block = block;
    }

    public static MultiPartGenerator multiPart(Block block) {
        return new MultiPartGenerator(block);
    }

    public MultiPartGenerator with(List<Variant> variants) {
        JsonObject part = new JsonObject();
        part.add("apply", Variant.convertList(variants));
        parts.add(part);
        return this;
    }

    public MultiPartGenerator with(Variant variant) {
        return with(List.of(variant));
    }

    public MultiPartGenerator with(Condition condition, List<Variant> variants) {
        JsonObject part = new JsonObject();
        part.add("when", condition.get());
        part.add("apply", Variant.convertList(variants));
        parts.add(part);
        return this;
    }

    public MultiPartGenerator with(Condition condition, Variant... variants) {
        return with(condition, List.of(variants));
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public JsonElement get() {
        JsonArray array = new JsonArray();
        parts.forEach(array::add);
        JsonObject json = new JsonObject();
        json.add("multipart", array);
        return json;
    }
}
