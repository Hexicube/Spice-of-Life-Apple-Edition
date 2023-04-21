package com.hexicube.solapple.api;

import com.hexicube.solapple.tracking.CapabilityHandler;
import com.hexicube.solapple.tracking.FoodList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;

/**
 Provides a stable API for interfacing with Spice of Life: Apple Edition.
 */
public final class SOLAppleAPI {
	public static Capability<FoodCapability> foodCapability = CapabilityManager.get(new CapabilityToken<>() { });
	
	private SOLAppleAPI() { }
	
	/**
	 Retrieves the {@link FoodCapability} for the given player.
	 */
	public static FoodCapability getFoodCapability(Player player) {
		return FoodList.get(player);
	}
	
	/**
	 Synchronizes the food list for the given player to the client, updating their max health in the process.
	 */
	public static void syncFoodList(Player player) {
		CapabilityHandler.syncFoodList(player);
	}
}
