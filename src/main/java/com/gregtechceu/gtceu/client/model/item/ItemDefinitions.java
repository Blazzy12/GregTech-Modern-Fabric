package com.gregtechceu.gtceu.client.model.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.common.item.LampBlockItem;
import com.gregtechceu.gtceu.compat.client.color.ColorHandlers;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 26.x items need an item definition ({@code assets/<ns>/items/<id>.json}), which GT's resources (made for
 * 1.21.1) do not have. Every GT item without one gets one here, pointing at its {@code item/<id>} model with GT's
 * legacy item colours as tints. Items that used a block entity renderer get the matching special model: machines
 * draw their dynamic parts over the model, lamps and facades are drawn by their special renderer.
 */
public final class ItemDefinitions {

    private static final Identifier FACADE_COVER = GTCEu.id("facade_cover");

    private ItemDefinitions() {}

    /** Called while resources reload, after GT's dynamic assets are generated. */
    public static void generateMissing(ResourceManager resources) {
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) continue;
            Identifier file = GTDynamicResourcePack.ITEM_DEFINITION_ID_CONVERTER.idToFile(id);
            if (resources.getResource(file).isPresent()) continue;

            Identifier model = id.withPrefix("item/");
            JsonObject definition = new JsonObject();
            if (item instanceof MetaMachineItem) {
                definition.add("model", composite(model(model), special(model, "machine_dynamic")));
            } else if (item instanceof LampBlockItem) {
                definition.add("model", special(model, "lamp"));
            } else if (id.equals(FACADE_COVER)) {
                definition.add("model", composite(model(model), special(model, "facade")));
            } else {
                definition.add("model", model(model));
            }
            GTDynamicResourcePack.addResource(file, definition);
        }
    }

    private static JsonObject model(Identifier model) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:model");
        json.addProperty("model", model.toString());
        JsonArray tints = new JsonArray();
        for (int i = 0; i < ColorHandlers.BLOCK_TINT_LAYERS; i++) {
            JsonObject tint = new JsonObject();
            tint.addProperty("type", ColorHandlers.LEGACY_ITEM_TINT.toString());
            tint.addProperty("index", i);
            tints.add(tint);
        }
        json.add("tints", tints);
        return json;
    }

    private static JsonObject special(Identifier base, String renderer) {
        JsonObject special = new JsonObject();
        special.addProperty("type", GTCEu.id(renderer).toString());
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:special");
        json.addProperty("base", base.toString());
        json.add("model", special);
        return json;
    }

    private static JsonObject composite(JsonObject... models) {
        JsonArray list = new JsonArray();
        for (JsonObject model : models) list.add(model);
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:composite");
        json.add("models", list);
        return json;
    }
}
