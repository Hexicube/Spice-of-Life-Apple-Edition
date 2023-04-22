package com.hexicube.solapple.tracking;

import com.hexicube.solapple.SOLApple;
import com.hexicube.solapple.SOLAppleConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import static com.hexicube.solapple.lib.Localization.localizedComponent;
import static com.hexicube.solapple.lib.Localization.localizedQuantityComponent;

@Mod.EventBusSubscriber(modid = SOLApple.MOD_ID)
public final class FoodTracker {
	@SubscribeEvent
	public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
		if (!(event.getEntity() instanceof Player player)) return;

		if (player.level.isClientSide) return;
		var world = (ServerLevel) player.level;

		var serverPlayer = (ServerPlayer) player;
		boolean isInSurvival = serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL;
		if (SOLAppleConfig.limitProgressionToSurvival() && !isInSurvival) return;

		var usedItem = event.getItem().getItem();
		if (!usedItem.isEdible()) return;

		FoodList foodList = FoodList.get(player);
		var progressInfo = foodList.getProgressInfo();
		ArrayList<String> completeGroups = new ArrayList<>();
		for (SOLAppleConfig.Server.FoodGroupConfig group : progressInfo.completedGroups) completeGroups.add(group.name);
		boolean hasTriedNewFood = foodList.addFood(usedItem);

		// check this before syncing, because the sync entails an hp update
		boolean newMilestoneReached = MaxHealthHandler.updateFoodHPModifier(player);

		CapabilityHandler.syncFoodList(player);

		if (newMilestoneReached) {
			progressInfo = foodList.getProgressInfo();
			ArrayList<String> newGroups = new ArrayList<>();
			int extraHearts = 0;
			for (SOLAppleConfig.Server.FoodGroupConfig group : progressInfo.completedGroups) {
				if (!completeGroups.contains(group.name)) {
					newGroups.add(group.name);
					extraHearts += group.hearts;
				}
			}

			if (SOLAppleConfig.shouldPlayMilestoneSounds()) {
				// passing the player makes it not play for some reason
				world.playSound(
					null,
					player.blockPosition(),
					SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
					1.0F, 1.0F
				);
			}

			if (SOLAppleConfig.shouldSpawnMilestoneParticles()) {
				spawnParticles(world, player, ParticleTypes.HEART, 12);

				if (progressInfo.hasReachedMax()) {
					spawnParticles(world, player, ParticleTypes.HAPPY_VILLAGER, 16);
				}
			}

			// TODO: show completed group names?
			var heartsDescription = localizedQuantityComponent("message", "hearts", extraHearts);

			if (SOLAppleConfig.shouldShowProgressAboveHotbar()) {
				String messageKey = progressInfo.hasReachedMax() ? "finished.hotbar" : "milestone_achieved";
				player.displayClientMessage(localizedComponent("message", messageKey, heartsDescription), true);
			} else {
				showChatMessage(player, ChatFormatting.DARK_AQUA, localizedComponent("message", "milestone_achieved", heartsDescription));
				if (progressInfo.hasReachedMax()) {
					showChatMessage(player, ChatFormatting.GOLD, localizedComponent("message", "finished.chat"));
				}
			}
		} else if (hasTriedNewFood) {
			if (SOLAppleConfig.shouldSpawnIntermediateParticles()) {
				spawnParticles(world, player, ParticleTypes.END_ROD, 12);
			}
		}
	}

	private static void spawnParticles(ServerLevel world, Player player, ParticleOptions type, int count) {
		// this overload sends a packet to the client
		world.sendParticles(
				type,
				player.getX(), player.getY() + player.getEyeHeight(), player.getZ(),
				count,
				0.5F, 0.5F, 0.5F,
				0.0F
		);
	}

	private static void showChatMessage(Player player, ChatFormatting color, Component message) {
		var component = localizedComponent("message", "chat_wrapper", message)
				.withStyle(color);
		player.displayClientMessage(component, false);
	}

	private FoodTracker() {}
}
