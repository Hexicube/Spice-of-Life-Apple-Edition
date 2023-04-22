package com.hexicube.solapple;

import com.google.gson.*;
import com.hexicube.solapple.tracking.CapabilityHandler;
import com.hexicube.solapple.tracking.FoodList;
import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod.EventBusSubscriber(modid = SOLApple.MOD_ID, bus = MOD)
public final class SOLAppleConfig {
	private static String localizationPath(String path) {
		return "config." + SOLApple.MOD_ID + "." + path;
	}

	public static final Server SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;

	static {
		Pair<Server, ForgeConfigSpec> specPair = new Builder().configure(Server::new);
		SERVER = specPair.getLeft();
		SERVER_SPEC = specPair.getRight();
	}

	public static final Client CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;

	static {
		Pair<Client, ForgeConfigSpec> specPair = new Builder().configure(Client::new);
		CLIENT = specPair.getLeft();
		CLIENT_SPEC = specPair.getRight();
	}

	public static void setUp() {
		ModLoadingContext context = ModLoadingContext.get();
		context.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
		context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
	}

	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent.Reloading event) {
		MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
		if (currentServer == null) return;

		groupCache = null;
		var players = currentServer.getPlayerList();
		for (var player : players.getPlayers()) {
			FoodList.get(player).invalidateProgressInfo();
			CapabilityHandler.syncFoodList(player);
		}
	}

	public static int getBaseHearts() {
		return SERVER.baseHearts.get();
	}

	private static List<Server.FoodGroupConfig> groupCache = null;
	public static List<Server.FoodGroupConfig> getFoodGroups() {
		if (groupCache == null) groupCache = SERVER.foodGroups.get().stream().map(Server.FoodGroupConfig::decode).toList();
		return groupCache;
	}

	public static boolean shouldResetOnDeath() {
		return SERVER.shouldResetOnDeath.get();
	}

	public static boolean limitProgressionToSurvival() {
		return SERVER.limitProgressionToSurvival.get();
	}

	public static class Server {
		public final IntValue baseHearts;

		public final ConfigValue<List<? extends String>> foodGroups;

		public final BooleanValue shouldResetOnDeath;
		public final BooleanValue limitProgressionToSurvival;

		public static class FoodGroupConfig {
			public String encode() {
				JsonObject obj = new JsonObject();
				obj.addProperty("name", name);
				obj.addProperty("hearts", hearts);
				JsonArray list = new JsonArray();
				for (String food : foods) list.add(food);
				obj.add("foodList", list);
				return obj.toString();
			}
			public static FoodGroupConfig decode(String data) {
				JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
				JsonArray foodList = obj.getAsJsonArray("foodList");
				ArrayList<String> list = new ArrayList<>();
				for (int a = 0; a < foodList.size(); a++) list.add(foodList.get(a).getAsString());
				return new FoodGroupConfig(obj.get("name").getAsString(), list, obj.get("hearts").getAsInt());
			}

			protected static final List<String> DEFAULT_GROUPS = List.of(
				new FoodGroupConfig("Harvestables", List.of("minecraft:apple", "minecraft:carrot", "minecraft:potato", "minecraft:beetroot"), 1).encode(),
				new FoodGroupConfig("Fish", List.of("minecraft:cooked_cod", "minecraft:cooked_salmon", "minecraft:tropical_fish"), 1).encode(),
				new FoodGroupConfig("Produce", List.of("minecraft:bread", "minecraft:baked_potato", "minecraft:pumpkin_pie", "minecraft:beetroot_soup", "minecraft:mushroom_stew", "minecraft:rabbit_stew"), 1).encode(),
				new FoodGroupConfig("Meat", List.of("minecraft:cooked_beef", "minecraft:cooked_porkchop", "minecraft:cooked_chicken", "minecraft:cooked_mutton", "minecraft:cooked_rabbit"), 1).encode(),
				new FoodGroupConfig("Treats", List.of("minecraft:cookie", "minecraft:melon_slice", "minecraft:honey_bottle", "minecraft:sweet_berries", "minecraft:glow_berries"), 1).encode(),
				new FoodGroupConfig("Golden", List.of("minecraft:golden_apple", "minecraft:golden_carrot", "minecraft:enchanted_golden_apple"), 5).encode()
			);

			public FoodGroupConfig(String name, List<String> foods, int hearts) {
				this.name = name;
				this.foods = foods;
				this.hearts = hearts;
			}
			public final String name;
			public final List<String> foods;
			public final int hearts;

			public List<Item> filterList(List<Item> list) {
				return list.stream().filter(food -> {
					String id = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(food)).toString();
					return foods.contains(id);
				}).toList();
			}

			public boolean isComplete(List<Item> list) {
				List<Item> relevant = filterList(list);
				return relevant.size() >= foods.size();
			}
		}

		Server(Builder builder) {
			builder.push("milestones");

			baseHearts = builder
					.translation(localizationPath("base_hearts"))
					.comment("Number of hearts you start out with.")
					.defineInRange("baseHearts", 10, 0, 1000);

			foodGroups = builder
					.translation(localizationPath("food_groups"))
					.comment("Collection of food groups.")
					.defineList("foodGroups", FoodGroupConfig.DEFAULT_GROUPS, e -> e instanceof String);

			builder.pop();
			builder.push("miscellaneous");

			shouldResetOnDeath = builder
					.translation(localizationPath("reset_on_death"))
					.comment("Whether or not to reset the food list on death, effectively losing all bonus hearts.")
					.define("resetOnDeath", false);

			limitProgressionToSurvival = builder
					.translation(localizationPath("limit_progression_to_survival"))
					.comment("If true, eating foods outside of survival mode (e.g. creative/adventure) is not tracked and thus does not contribute towards progression.")
					.define("limitProgressionToSurvival", false);

			builder.pop();
		}
	}

	public static boolean shouldPlayMilestoneSounds() {
		return CLIENT.shouldPlayMilestoneSounds.get();
	}

	public static boolean shouldSpawnIntermediateParticles() {
		return CLIENT.shouldSpawnIntermediateParticles.get();
	}

	public static boolean shouldSpawnMilestoneParticles() {
		return CLIENT.shouldSpawnMilestoneParticles.get();
	}

	public static boolean isFoodTooltipEnabled() {
		return CLIENT.isFoodTooltipEnabled.get();
	}

	public static boolean shouldShowProgressAboveHotbar() {
		return CLIENT.shouldShowProgressAboveHotbar.get();
	}

	public static boolean shouldShowUneatenFoods() {
		return CLIENT.shouldShowUneatenFoods.get();
	}

	public static class Client {
		public final BooleanValue shouldPlayMilestoneSounds;
		public final BooleanValue shouldSpawnIntermediateParticles;
		public final BooleanValue shouldSpawnMilestoneParticles;

		public final BooleanValue isFoodTooltipEnabled;
		public final BooleanValue shouldShowProgressAboveHotbar;
		public final BooleanValue shouldShowUneatenFoods;

		Client(Builder builder) {
			builder.push("milestone celebration");

			shouldPlayMilestoneSounds = builder
					.translation(localizationPath("should_play_milestone_sounds"))
					.comment("If true, completing a group plays a ding sound.")
					.define("shouldPlayMilestoneSounds", true);

			shouldSpawnIntermediateParticles = builder
					.translation(localizationPath("should_spawn_intermediate_particles"))
					.comment("If true, trying a new food spawns particles.")
					.define("shouldSpawnIntermediateParticles", true);

			shouldSpawnMilestoneParticles = builder
					.translation(localizationPath("should_spawn_milestone_particles"))
					.comment("If true, completing a group spawns particles.")
					.define("shouldSpawnMilestoneParticles", true);

			builder.pop();
			builder.push("miscellaneous");

			isFoodTooltipEnabled = builder
					.translation(localizationPath("is_food_tooltip_enabled"))
					.comment("If true, foods indicate in their tooltips whether or not they have been eaten.")
					.define("isFoodTooltipEnabled", true);

			shouldShowProgressAboveHotbar = builder
					.translation(localizationPath("should_show_progress_above_hotbar"))
					.comment("Whether the messages notifying you of completing groups should be displayed above the hotbar or in chat.")
					.define("shouldShowProgressAboveHotbar", true);

			shouldShowUneatenFoods = builder
					.translation(localizationPath("should_show_uneaten_foods"))
					.comment("If true, the food book also lists foods that you haven't eaten, in addition to the ones you have.")
					.define("shouldShowUneatenFoods", true);

			builder.pop();
		}
	}

	public static boolean isAllowed(Item food) {
        String id = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(food)).toString();
		//System.out.println("[SoL:Apple] Testing food ID: " + id);
		return getFoodGroups().stream().anyMatch(group -> group.foods.contains(id));
		/*if (hasWhitelist()) {
			return matchesAnyPattern(id, SERVER.whitelist.get());
		} else {
			return !matchesAnyPattern(id, SERVER.blacklist.get());
		}*/
	}

	// more efficient filtering for blacklist
	public static Stream<Item> filterAllowed(Stream<Item> foodList) {
		List<String> allValid = getFoodGroups().stream().map(it -> it.foods).flatMap(List::stream).distinct().toList();
		return foodList.filter(food -> {
			String id = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(food)).toString();
			return allValid.contains(id);
		});
	}

	private static boolean matchesAnyPattern(String query, Collection<? extends String> patterns) {
		for (String glob : patterns) {
			StringBuilder pattern = new StringBuilder(glob.length());
			for (String part : glob.split("\\*", -1)) {
				if (!part.isEmpty()) { // not necessary
					pattern.append(Pattern.quote(part));
				}
				pattern.append(".*");
			}

			// delete extraneous trailing ".*" wildcard
			pattern.delete(pattern.length() - 2, pattern.length());

			if (Pattern.matches(pattern.toString(), query)) {
				return true;
			}
		}
		return false;
	}
}
