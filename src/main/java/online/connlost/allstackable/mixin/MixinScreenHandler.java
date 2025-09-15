package online.connlost.allstackable.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import online.connlost.allstackable.util.ComparatorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenHandler.class)
public class MixinScreenHandler {
    
    @WrapOperation(
        method = "calculateComparatorOutput(Lnet/minecraft/inventory/Inventory;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;getMaxCount(Lnet/minecraft/item/ItemStack;)I")
    )
    private static int fixComparatorSignalStrength(Inventory instance, ItemStack stack, Operation<Integer> original) {
        return ComparatorHelper.shouldUseVanillaComparatorBehavior(stack) ? 
            ((online.connlost.allstackable.util.IItemMaxCount) stack.getItem()).getVanillaMaxCount() : 
            original.call(instance, stack);
    }
    
    @ModifyReturnValue(
        method = "calculateComparatorOutput(Lnet/minecraft/inventory/Inventory;)I",
        at = @At("RETURN")
    )
    private static int adjustComparatorOutput(int original) {
        // 这里可以添加额外的信号强度调整逻辑
        return original;
    }
}