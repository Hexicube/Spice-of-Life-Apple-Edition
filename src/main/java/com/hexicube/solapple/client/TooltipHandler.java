package com.hexicube.solapple.client;

import com.hexicube.solapple.SOLApple;
import com.hexicube.solapple.SOLAppleConfig;
import com.hexicube.solapple.tracking.FoodList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.hexicube.solapple.lib.Localization.localizedComponent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SOLApple.MOD_ID)
public final class TooltipHandler {
	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		if (!SOLAppleConfig.isFoodTooltipEnabled()) return;
		
		Player player = event.getPlayer();
		if (player == null) return;
		
		Item food = event.getItemStack().getItem();
		if (!food.isEdible()) return;
		
		FoodList foodList = FoodList.get(player);
		boolean hasBeenEaten = foodList.hasEaten(food);
		boolean isAllowed = SOLAppleConfig.isAllowed(food);
		
		var tooltip = event.getToolTip();
		if (!isAllowed) {
			if (hasBeenEaten) {
				tooltip.add(localizedTooltip("disabled.eaten", ChatFormatting.DARK_RED));
			}
			tooltip.add(localizedTooltip("disabled.blacklist", ChatFormatting.DARK_GRAY));
		} else {
			if (hasBeenEaten) {
				tooltip.add(localizedTooltip("hearty.eaten", ChatFormatting.DARK_GREEN));
			} else {
				tooltip.add(localizedTooltip("hearty.not_eaten", ChatFormatting.DARK_AQUA));
			}
		}
	}
	
	private static MutableComponent localizedTooltip(String path, ChatFormatting color) {
		return localizedComponent("tooltip", path).withStyle(color);
	}
	
	private TooltipHandler() {}
}
