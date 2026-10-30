package com.gregtechceu.gtceu.compat.common.crafting;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.mojang.serialization.MapCodec;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** Matches stacks that every child ingredient matches. */
public class IntersectionIngredient implements ICustomIngredient {

    public static final MapCodec<IntersectionIngredient> CODEC = ExtraCodecs.nonEmptyList(Ingredient.CODEC.listOf())
            .fieldOf("children").xmap(IntersectionIngredient::new, IntersectionIngredient::children);

    public static final IngredientType<IntersectionIngredient> TYPE = new IngredientType<>(CODEC);

    private final List<Ingredient> children;

    public IntersectionIngredient(List<Ingredient> children) {
        if (children.size() < 2) {
            throw new IllegalArgumentException("Cannot create an IntersectionIngredient with fewer than 2 children");
        }
        this.children = List.copyOf(children);
    }

    public static Ingredient of(Ingredient... ingredients) {
        return new IntersectionIngredient(Arrays.asList(ingredients)).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient child : children) {
            if (!child.test(stack)) return false;
        }
        return true;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(children.getFirst().getItems())
                .filter(stack -> children.stream().skip(1).allMatch(child -> child.test(stack)));
    }

    @Override
    public boolean isSimple() {
        return children.stream().allMatch(Ingredient::isSimple);
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    public List<Ingredient> children() {
        return children;
    }
}
