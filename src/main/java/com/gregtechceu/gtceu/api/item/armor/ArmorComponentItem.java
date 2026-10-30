package com.gregtechceu.gtceu.api.item.armor;

import net.minecraft.world.item.equipment.ArmorMaterial;

import net.minecraft.world.item.Item;

import net.minecraft.world.item.equipment.ArmorType;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import com.gregtechceu.gtceu.compat.common.ItemAbility;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@Accessors(chain = true)
@NotNullByDefault
public class ArmorComponentItem extends com.gregtechceu.gtceu.api.item.armor.GTArmorBaseItem implements IComponentItem {

    @Getter
    private @Nullable IArmorLogic armorLogic = null;
    @Getter
    protected List<IItemComponent> components;

    public ArmorComponentItem(ArmorMaterial material, ArmorType type, Properties properties) {
        // Some trickery to always receive damage events without ever actually breaking the armor
        super(material, type, properties.durability(Integer.MAX_VALUE));
        components = new ArrayList<>();
    }

    public ArmorComponentItem setArmorLogic(IArmorLogic armorLogic) {
        Preconditions.checkNotNull(armorLogic, "Cannot set ArmorLogic to null");
        this.armorLogic = armorLogic;
        this.armorLogic.addToolComponents(this);
        return this;
    }

    public void attachComponents(IItemComponent... components) {
        this.components.addAll(Arrays.asList(components));
        for (IItemComponent component : components) {
            component.onAttached(this);
        }
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        IArmorLogic armorLogic = getArmorLogic();
        List<ItemAttributeModifiers.Entry> list = new ArrayList<>(
                super.getDefaultAttributeModifiers(stack).modifiers());
        if (armorLogic != null)
            list.addAll(armorLogic.getDefaultAttributeModifiers(getEquipmentSlot(), stack));
        return new ItemAttributeModifiers(list);
    }

    @Override
    public ArmorType getType() {
        return super.getType();
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return super.getEquipmentSlot();
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity,
                              @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        // 26.x only ticks on the server and passes the equipment slot instead of an inventory index
        int slotId = slot == null ? -1 : slot.getIndex();
        boolean isSelected = slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND;
        for (IItemComponent component : components) {
            if (component instanceof IItemLifeCycle lifeCycle) {
                lifeCycle.inventoryTick(stack, level, entity, slotId, isSelected);
            }
        }
        if (slot != null && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && entity instanceof Player player) {
            if (armorLogic == null) return;
            this.armorLogic.onArmorTick(level, player, stack);
        }
    }

    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    public int getEnchantmentValue() {
        return 50;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {}

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity,
                                                   Consumer<Item> onBroken) {
        if (armorLogic == null) return super.damageItem(stack, amount, entity, onBroken);
        return armorLogic.damageArmor(entity, stack, amount, this.getEquipmentSlot());
    }

    @Override
    public @Nullable Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      com.gregtechceu.gtceu.api.item.armor.ArmorLayer layer, boolean innerModel) {
        if (armorLogic == null) return super.getArmorTexture(stack, entity, slot, layer, innerModel);
        return armorLogic.getArmorTexture(stack, entity, slot, layer);
    }

    ///////////////////////////////////////////
    ///// ALL component item things ///////
    ///////////////////////////////////////////

    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        boolean found = false;
        for (IItemComponent component : components) {
            if (component instanceof ISubItemHandler subItemHandler) {
                subItemHandler.fillItemCategory(this, category, items);
                found = true;
            }
        }
        if (found) return;
        items.add(new ItemStack(this));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltipAdder, TooltipFlag flag) {
        List<Component> lines = new java.util.ArrayList<>();
        appendHoverText(stack, context, lines, flag);
        lines.forEach(tooltipAdder);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        for (IItemComponent component : components) {
            if (component instanceof IAddInformation addInformation) {
                addInformation.appendHoverText(stack, context, tooltipComponents, isAdvanced);
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IDurabilityBar durabilityBar) {
                return durabilityBar.isBarVisible(stack);
            }
        }
        return super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IDurabilityBar durabilityBar) {
                return durabilityBar.getBarWidth(stack);
            }
        }
        return super.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IDurabilityBar durabilityBar) {
                return durabilityBar.getBarColor(stack);
            }
        }
        return super.getBarColor(stack);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        for (IItemComponent component : components) {
            if (component instanceof IAbilityItem abilityItem && abilityItem.canPerformAction(stack, action)) {
                return true;
            }
        }
        return super.canPerformAction(stack, action);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.useOn(context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.use(player.getItemInHand(usedHand), level, player, usedHand);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                stack = interactionItem.finishUsingItem(stack, level, livingEntity);
            }
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.onItemUseFirst(itemStack, context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
                                                  InteractionHand usedHand) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.interactLivingEntity(stack, player, interactionTarget, usedHand);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getName(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof ICustomDescriptionId customDescriptionId) {
                Component name = customDescriptionId.getItemName(stack);
                if (name != null) {
                    return name;
                }
            }
        }
        String langId = getDescriptionId(stack);
        if (!langId.equals(getDescriptionId())) {
            return Component.translatable(langId);
        }
        return super.getName(stack);
    }

    /** Per-stack translation key from {@link ICustomDescriptionId}; 26.x items only have a fixed one. */
    public String getDescriptionId(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof ICustomDescriptionId customDescriptionId) {
                String langId = customDescriptionId.getItemDescriptionId(stack);
                if (langId != null) {
                    return langId;
                }
            }
        }
        return getDescriptionId();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(itemStack);
            }
        }
        return super.getCraftingRemainingItem(itemStack);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(stack) != ItemStack.EMPTY;
            }
        }
        return super.hasCraftingRemainingItem(stack);
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return stack.is(GTItems.NANO_BOOTS.asItem()) || stack.is(GTItems.QUANTUM_BOOTS.asItem());
    }

    public boolean isPPE(ItemStack stack) {
        if (armorLogic != null) return armorLogic.isPPE(stack);
        return false;
    }
}
