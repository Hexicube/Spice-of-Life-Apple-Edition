package com.hexicube.solapple.tracking;

import com.hexicube.solapple.SOLAppleConfig;

/** contains all relevant variables for current progress */
public final class ProgressInfo {
	/** the number of unique foods eaten */
	public final int foodsEaten;
	
	ProgressInfo(FoodList foodList) {
		foodsEaten = (int) foodList.getEatenFoods().stream()
			.filter(food -> SOLAppleConfig.isAllowed(food.item))
			.count();
	}
	
	public boolean hasReachedMax() {
		return foodsEaten >= SOLAppleConfig.highestMilestone();
	}
	
	/** the next milestone to reach, or a negative value if the maximum has been reached */
	public int nextMilestone() {
		return hasReachedMax() ? -1 : SOLAppleConfig.milestone(milestonesAchieved());
	}
	
	/** the number of foods remaining until the next milestone, or a negative value if the maximum has been reached */
	public int foodsUntilNextMilestone() {
		return nextMilestone() - foodsEaten;
	}
	
	/** the number of milestones achieved, doubling as the index of the next milestone */
	public int milestonesAchieved() {
		return (int) SOLAppleConfig.getMilestones().stream()
			.filter(milestone -> foodsEaten >= milestone).count();
	}
}
