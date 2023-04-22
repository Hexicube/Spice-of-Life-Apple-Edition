package com.hexicube.solapple.tracking;

import com.hexicube.solapple.SOLAppleConfig;
import com.hexicube.solapple.client.FoodItems;
import net.minecraft.world.item.Item;

import java.util.List;

/** contains all relevant variables for current progress */
public final class ProgressInfo {
	/** the number of unique foods eaten */
	public final List<Item> foodsEaten;
	public final int foodsUneaten;

	public final List<SOLAppleConfig.Server.FoodGroupConfig> completedGroups;
	public final int groupsLeft;

	private final int totalGroups;

	ProgressInfo(FoodList foodList, List<SOLAppleConfig.Server.FoodGroupConfig> groups) {
		foodsEaten = foodList.getEatenFoods().stream().map(it -> it.item).filter(SOLAppleConfig::isAllowed).toList();
		foodsUneaten = (int) FoodItems.getAllFoods().stream().filter(food -> !foodsEaten.contains(food)).count();

		completedGroups = groups.stream().filter(it -> it.isComplete(foodsEaten)).toList();
		groupsLeft = groups.size() - completedGroups.size();

		totalGroups = groups.size();
	}

	public boolean hasReachedMax() {
		return completedGroups.size() == totalGroups;
	}
}
