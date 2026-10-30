package brachy.modularui.core.mixins.common;

import com.gregtechceu.gtceu.compat.items.IItemHandlerModifiable;
import com.gregtechceu.gtceu.compat.items.wrapper.CombinedInvWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CombinedInvWrapper.class, remap = false)
public interface CombinedInvWrapperAccessor {

    @Accessor
    IItemHandlerModifiable[] getItemHandler();
}
