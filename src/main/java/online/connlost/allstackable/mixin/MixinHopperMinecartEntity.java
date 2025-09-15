package online.connlost.allstackable.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.ItemStack;
import online.connlost.allstackable.util.HopperHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HopperMinecartEntity.class)
public class MixinHopperMinecartEntity {
    
    // HopperMinecartEntity doesn't have isFull method, removing this mixin
    // The hopper behavior will be handled by the container mixins instead
}