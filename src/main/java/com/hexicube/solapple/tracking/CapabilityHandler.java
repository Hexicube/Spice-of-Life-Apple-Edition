package com.hexicube.solapple.tracking;

import com.hexicube.solapple.SOLApple;
import com.hexicube.solapple.SOLAppleConfig;
import com.hexicube.solapple.api.FoodCapability;
import com.hexicube.solapple.communication.FoodListMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod.EventBusSubscriber(modid = SOLApple.MOD_ID)
public final class CapabilityHandler {
	private static final ResourceLocation FOOD = SOLApple.resourceLocation("food");
	
	@Mod.EventBusSubscriber(modid = SOLApple.MOD_ID, bus = MOD)
	private static final class Setup {
		@SubscribeEvent
		public static void registerCapabilities(RegisterCapabilitiesEvent event) {
			event.register(FoodCapability.class);
		}
	}
	
	@SubscribeEvent
	public static void attachPlayerCapability(AttachCapabilitiesEvent<Entity> event) {
		if (!(event.getObject() instanceof Player)) return;
		
		event.addCapability(FOOD, new FoodList());
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		// server needs to send any loaded data to the client
		syncFoodList(event.getPlayer());
	}
	
	@SubscribeEvent
	public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		syncFoodList(event.getPlayer());
	}
	
	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		if (event.isWasDeath() && SOLAppleConfig.shouldResetOnDeath()) return;
		
		var originalPlayer = event.getOriginal();
		originalPlayer.revive(); // so we can access the capabilities; entity will get removed either way
		FoodList original = FoodList.get(originalPlayer);
		FoodList newInstance = FoodList.get(event.getPlayer());
		newInstance.deserializeNBT(original.serializeNBT());
		// can't sync yet; client hasn't attached capabilities yet
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		syncFoodList(event.getPlayer());
	}
	
	public static void syncFoodList(Player player) {
		if (player.level.isClientSide) return;
		
		var target = (ServerPlayer) player;
		SOLApple.channel.sendTo(
			new FoodListMessage(FoodList.get(player)),
			target.connection.getConnection(),
			NetworkDirection.PLAY_TO_CLIENT
		);
		
		MaxHealthHandler.updateFoodHPModifier(player);
	}
}
