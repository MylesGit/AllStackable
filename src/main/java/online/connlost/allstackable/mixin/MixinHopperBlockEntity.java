package online.connlost.allstackable.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import online.connlost.allstackable.util.HopperHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class MixinHopperBlockEntity {
    
    @WrapOperation(
        method = "isFull",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I")
    )
    private int modifyMaxCountForHopper(ItemStack instance, Operation<Integer> original) {
        return HopperHelper.getVanillaMaxCountForHopper(instance);
    }
    
    @WrapOperation(
        method = "isInventoryFull",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I")
    )
    private static int modifyMaxCountForHopperStatic(ItemStack instance, Operation<Integer> original) {
        return HopperHelper.getVanillaMaxCountForHopper(instance);
    }

    @WrapOperation(
        method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z")
    )
    private static boolean limitCollectCount(Inventory inventory, ItemEntity itemEntity, Operation<Boolean> original) {
        if (!HopperHelper.shouldUseVanillaHopperBehavior(itemEntity.getStack())) {
            return original.call(inventory, itemEntity);
        }
        return HopperHelper.collectOneItem(inventory, itemEntity);
    }

    @Inject(
        method = "canMergeItems",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void cancelItemMerging(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir) {
        if (HopperHelper.shouldUseVanillaHopperBehavior(first) || HopperHelper.shouldUseVanillaHopperBehavior(second)) {
            cir.setReturnValue(false);
        }
    }
}