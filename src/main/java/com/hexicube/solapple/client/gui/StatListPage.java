package com.hexicube.solapple.client.gui;

import com.hexicube.solapple.SOLAppleConfig;
import com.hexicube.solapple.client.gui.elements.UIBox;
import com.hexicube.solapple.tracking.ProgressInfo;

import java.awt.*;
import java.util.List;

import static com.hexicube.solapple.lib.Localization.localized;

final class StatListPage extends Page {
	StatListPage(FoodData foodData, List<SOLAppleConfig.Server.FoodGroupConfig> foodGroups, Rectangle frame) {
		super(frame, localized("gui", "food_book.stats"));

		ProgressInfo progressInfo = foodData.progressInfo;
		String foodsTasted;
		if (SOLAppleConfig.shouldShowUneatenFoods()) {
			foodsTasted = fraction(progressInfo.foodsEaten.size(), foodData.validFoods.size());
		} else {
			foodsTasted = String.valueOf(progressInfo.foodsEaten.size());
		}
		
		mainStack.addChild(statWithIcon(
			FoodBookScreen.appleImage,
			foodsTasted,
			localized("gui", "food_book.stats.foods_tasted")
		));
		
		mainStack.addChild(makeSeparatorLine());

		int complete = 0;
		int gainedHearts = 0;
		int totalHearts = 0;
		for (SOLAppleConfig.Server.FoodGroupConfig group : foodGroups) {
			if (group.filterList(foodData.eatenFoods).size() == group.foods.size()) {
				complete++;
				gainedHearts += group.hearts;
			}
			totalHearts += group.hearts;
		}

		mainStack.addChild(statWithFractionDynamic(
			FoodBookScreen.drumstickImage, FoodBookScreen.emptyDrumstickImage,
			FoodBookScreen.emptyBarBrown, FoodBookScreen.fullBarBrown,
			localized("gui", "food_book.stats.groups_finished"),
			complete, foodGroups.size()
		));

		mainStack.addChild(makeSeparatorLine());

		mainStack.addChild(statWithFractionDynamic(
			FoodBookScreen.heartImage, FoodBookScreen.emptyHeartImage,
			FoodBookScreen.emptyBarRed, FoodBookScreen.fullBarRed,
			localized("gui", "food_book.stats.hearts_gained"),
			gainedHearts, totalHearts
		));
		
		mainStack.addChild(makeSeparatorLine());
		
		updateMainStack();
	}
}
