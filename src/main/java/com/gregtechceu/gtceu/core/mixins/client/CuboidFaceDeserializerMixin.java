package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.compat.client.model.QuadMetadata;

import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Type;

/** Reads {@code gtceu:face_layer} of model faces. */
@Mixin(targets = "net.minecraft.client.resources.model.cuboid.CuboidFace$Deserializer")
public class CuboidFaceDeserializerMixin {

    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/resources/model/cuboid/CuboidFace;",
                       at = @At("RETURN"))
    private CuboidFace gtceu$readFaceLayer(CuboidFace face, JsonElement json, Type type,
                                           JsonDeserializationContext context) {
        var object = json.getAsJsonObject();
        if (!object.has(FaceLayer.JSON_PROPERTY)) {
            return face;
        }

        String name = GsonHelper.getAsString(object, FaceLayer.JSON_PROPERTY);
        try {
            QuadMetadata.setFaceLayer(face, FaceLayer.fromSerializedName(name));
            return face;
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("Unknown GTCEu face layer: " + name, exception);
        }
    }
}
