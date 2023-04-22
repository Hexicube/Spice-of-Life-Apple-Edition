package com.hexicube.solapple.client;

import com.hexicube.solapple.SOLApple;
import com.hexicube.solapple.SOLAppleConfig;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SOLApple.MOD_ID, bus = MOD)
public final class FoodItems {
	private static List<Item> foodsBeforeBlacklist;
	private static List<Item> foods;
	
	/** @return a list of all item stacks that can be eaten, including blacklisted/hidden ones */
	public static List<Item> getAllFoodsIgnoringBlacklist() {
		return new ArrayList<>(foodsBeforeBlacklist);
	}
	
	/** @return a list of all item stacks that can be eaten */
	public static List<Item> getAllFoods() {
		return new ArrayList<>(foods);
	}
	
	@SubscribeEvent
	public static void setUp(FMLCommonSetupEvent event) {
		foodsBeforeBlacklist = ForgeRegistries.ITEMS.getValues().stream()
			.filter(Item::isEdible)
			// sort by name
			.sorted(Comparator.comparing(food -> I18n.get(food.getDescriptionId() + ".name")))
			.collect(Collectors.toList());
		
		applyBlacklist();
	}
	
	@SubscribeEvent
	public static void onConfigUpdate(ModConfigEvent event) {
		if (event.getConfig().getType() == ModConfig.Type.CLIENT) return;
		
		applyBlacklist();
	}
	
	private static void applyBlacklist() {
		foods = SOLAppleConfig.filterAllowed(foodsBeforeBlacklist.stream()).toList();
	}
}
