package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.compat.client.model.QuadMetadata;

import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Type;

/** Reads {@code gtceu:face_layer} of model elements, the default of their faces. */
@Mixin(targets = "net.minecraft.client.resources.model.cuboid.CuboidModelElement$Deserializer")
public class CuboidModelElementDeserializerMixin {

    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/resources/model/cuboid/CuboidModelElement;",
                       at = @At("RETURN"))
    private CuboidModelElement gtceu$readElementLayer(CuboidModelElement element, JsonElement json, Type type,
                                                      JsonDeserializationContext context) {
        var object = json.getAsJsonObject();
        if (!object.has(FaceLayer.JSON_PROPERTY)) {
            return element;
        }

        String name = GsonHelper.getAsString(object, FaceLayer.JSON_PROPERTY);
        try {
            FaceLayer layer = FaceLayer.fromSerializedName(name);
            element.faces().values().forEach(face -> {
                if (QuadMetadata.getFaceLayer(face) == FaceLayer.UNCLASSIFIED) {
                    QuadMetadata.setFaceLayer(face, layer);
                }
            });
            return element;
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("Unknown GTCEu face layer: " + name, exception);
        }
    }
}
