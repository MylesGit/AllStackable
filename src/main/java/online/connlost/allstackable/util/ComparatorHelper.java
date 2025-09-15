package online.connlost.allstackable.util;

import net.minecraft.item.ItemStack;
import online.connlost.allstackable.server.config.ConfigManager;

public class ComparatorHelper {
    
    public static boolean shouldUseVanillaComparatorBehavior(ItemStack stack) {
        if (ConfigManager.getConfigManager().getRuleSetting("vanillaComparatorBehavior") == 1) {
            return ItemsHelper.isModified(stack);
        }
        return false;
    }
    
    public static int getVanillaSignalStrength(ItemStack stack) {
        if (shouldUseVanillaComparatorBehavior(stack)) {
            int vanillaMaxCount = ((IItemMaxCount) stack.getItem()).getVanillaMaxCount();
            // 原版游戏中，不可堆叠物品每个贡献1信号强度，可堆叠物品按堆叠比例贡献
            return vanillaMaxCount == 1 ? 1 : Math.min(15, stack.getCount() * 15 / vanillaMaxCount);
        }
        return stack.getMaxCount() == 1 ? 1 : Math.min(15, stack.getCount() * 15 / stack.getMaxCount());
    }
}