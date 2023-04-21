package com.hexicube.solapple;

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

		var players = currentServer.getPlayerList();
		for (var player : players.getPlayers()) {
			FoodList.get(player).invalidateProgressInfo();
			CapabilityHandler.syncFoodList(player);
		}
	}

	public static int getBaseHearts() {
		return SERVER.baseHearts.get();
	}

	public static List<Server.FoodGroupConfig> getFoodGroups() { return new ArrayList<>(SERVER.foodGroupList); }

	public static int getHeartsPerMilestone() {
		return SERVER.heartsPerMilestone.get();
	}

	public static List<Integer> getMilestones() {
		return new ArrayList<>(SERVER.milestones.get());
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
		public final ArrayList<FoodGroupConfig> foodGroupList;

		public final IntValue heartsPerMilestone;
		public final ConfigValue<List<? extends Integer>> milestones;

		public final BooleanValue shouldResetOnDeath;
		public final BooleanValue limitProgressionToSurvival;

		public static class FoodGroupConfig {
			private static final HashMap<String, String> DEFAULT_NAMES = new HashMap<>();
			private static final HashMap<String, List<String>> DEFAULT_FOOD = new HashMap<>();

			static {
				DEFAULT_NAMES.put("harvest", "Harvestables");
				DEFAULT_FOOD.put("harvest", List.of("minecraft.apple", "minecraft.carrot", "minecraft.potato", "minecraft.beetroot"));

				DEFAULT_NAMES.put("fish", "Fish");
				DEFAULT_FOOD.put("fish", List.of("minecraft.cooked_cod", "minecraft.cooked_salmon", "minecraft.tropical_fish"));

				DEFAULT_NAMES.put("produce", "Produce");
				DEFAULT_FOOD.put("produce", List.of("minecraft.bread", "minecraft.baked_potato", "minecraft.pumpkin_pie", "minecraft.beetroot_soup", "minecraft.mushroom_stew", "minecraft.rabbit_stew"));

				DEFAULT_NAMES.put("meat", "Meat");
				DEFAULT_FOOD.put("meat", List.of("minecraft.cooked_beef", "minecraft.cooked_porkchop", "minecraft.cooked_chicken", "minecraft.cooked_mutton", "minecraft.cooked_rabbit"));

				DEFAULT_NAMES.put("treat", "Treats");
				DEFAULT_FOOD.put("treat", List.of("minecraft.cookie", "minecraft.melon_slice", "minecraft.honey_bottle", "minecraft.sweet_berries", "minecraft.glow_berries"));

				DEFAULT_NAMES.put("gold", "Golden");
				DEFAULT_FOOD.put("gold", List.of("minecraft.golden_apple", "minecraft.golden_carrot", "minecraft.enchanted_golden_apple"));
			}

			public final String groupID;

			public FoodGroupConfig(Builder builder, String group) {
				groupID = group;

				builder.push("group." + group);

				name = builder
						.translation("group_name")
						.comment("The displayed name of the group.")
						.define("name", DEFAULT_NAMES.getOrDefault(group, group));

				foods = builder
						.translation("group_food_list")
						.comment("The list of food items for this group.")
						.defineList("foodList", DEFAULT_FOOD.getOrDefault(group, Lists.newArrayList()), e -> e instanceof String);

				hearts = builder
						.translation("group_hearts")
						.comment("How many hearts this group grants.")
						.defineInRange("hearts", 2, 0, 1000);

				builder.pop();
			}

			public final ConfigValue<String> name;
			public final ConfigValue<List<? extends String>> foods;
			public final IntValue hearts;
		}

		Server(Builder builder) {
			builder.push("milestones");

			baseHearts = builder
					.translation(localizationPath("base_hearts"))
					.comment("Number of hearts you start out with.")
					.defineInRange("baseHearts", 10, 0, 1000);

			foodGroups = builder
					.translation(localizationPath("food_groups"))
					.comment("Names of the food groups below.")
					.defineList("foodGroups", Lists.newArrayList("harvest", "fish", "produce", "meat", "treat", "gold"), e -> e instanceof String);

			heartsPerMilestone = builder
					.translation(localizationPath("hearts_per_milestone"))
					.comment("Number of hearts you gain for reaching a new milestone.")
					.defineInRange("heartsPerMilestone", 2, 0, 1000);

			milestones = builder
					.translation(localizationPath("milestones"))
					.comment("A list of numbers of unique foods you need to eat to unlock each milestone, in ascending order. Naturally, adding more milestones lets you earn more hearts.")
					.defineList("milestones", Lists.newArrayList(5, 10, 15, 20, 25), e -> e instanceof Integer);

			builder.pop();

			foodGroupList = new ArrayList<>();
			foodGroups.get().forEach(groupName -> foodGroupList.add(new FoodGroupConfig(builder, groupName)));

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
					.comment("If true, reaching a new milestone plays a ding sound.")
					.define("shouldPlayMilestoneSounds", true);

			shouldSpawnIntermediateParticles = builder
					.translation(localizationPath("should_spawn_intermediate_particles"))
					.comment("If true, trying a new food spawns particles.")
					.define("shouldSpawnIntermediateParticles", true);

			shouldSpawnMilestoneParticles = builder
					.translation(localizationPath("should_spawn_milestone_particles"))
					.comment("If true, reaching a new milestone spawns particles.")
					.define("shouldSpawnMilestoneParticles", true);

			builder.pop();
			builder.push("miscellaneous");

			isFoodTooltipEnabled = builder
					.translation(localizationPath("is_food_tooltip_enabled"))
					.comment("If true, foods indicate in their tooltips whether or not they have been eaten.")
					.define("isFoodTooltipEnabled", true);

			shouldShowProgressAboveHotbar = builder
					.translation(localizationPath("should_show_progress_above_hotbar"))
					.comment("Whether the messages notifying you of reaching new milestones should be displayed above the hotbar or in chat.")
					.define("shouldShowProgressAboveHotbar", true);

			shouldShowUneatenFoods = builder
					.translation(localizationPath("should_show_uneaten_foods"))
					.comment("If true, the food book also lists foods that you haven't eaten, in addition to the ones you have.")
					.define("shouldShowUneatenFoods", true);

			builder.pop();
		}
	}

	// TODO: investigate performance of all these get() calls

	public static int milestone(int i) {
		return SERVER.milestones.get().get(i);
	}

	public static int getMilestoneCount() {
		return SERVER.milestones.get().size();
	}

	public static int highestMilestone() {
		return milestone(getMilestoneCount() - 1);
	}

	public static boolean isAllowed(Item food) {
        String id = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(food)).toString();
		return SERVER.foodGroupList.stream().anyMatch(group -> group.foods.get().contains(id));
		/*if (hasWhitelist()) {
			return matchesAnyPattern(id, SERVER.whitelist.get());
		} else {
			return !matchesAnyPattern(id, SERVER.blacklist.get());
		}*/
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
