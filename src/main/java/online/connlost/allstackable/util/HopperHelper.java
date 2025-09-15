package online.connlost.allstackable.util;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import online.connlost.allstackable.server.config.ConfigManager;

public class HopperHelper {
    
    public static boolean shouldUseVanillaHopperBehavior(ItemStack stack) {
        if (ConfigManager.getConfigManager().getRuleSetting("vanillaHopperBehavior") == 1) {
            return ItemsHelper.isModified(stack);
        }
        return false;
    }
    
    public static int getVanillaMaxCountForHopper(ItemStack stack) {
        if (shouldUseVanillaHopperBehavior(stack)) {
            // 返回1而不是当前堆叠数量，这样漏斗会认为槽位已满（模仿SSF逻辑）
            return 1;
        }
        return stack.getMaxCount();
    }

    public static boolean collectOneItem(Inventory inventory, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getStack().copyWithCount(1);
        ItemStack itemStack2 = HopperBlockEntity.transfer(null, inventory, itemStack, null);
        if (itemStack2.isEmpty()) {
            itemEntity.getStack().decrement(1);
            if (itemEntity.getStack().isEmpty()) {
                itemEntity.setStack(ItemStack.EMPTY);
                itemEntity.discard();
            }
            return true;
        }
        return false;
    }
}