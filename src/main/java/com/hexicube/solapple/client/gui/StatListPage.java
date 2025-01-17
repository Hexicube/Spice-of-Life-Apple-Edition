package com.hexicube.solapple.client.gui;

import com.hexicube.solapple.SOLAppleConfig;
import com.hexicube.solapple.client.FoodItems;
import com.hexicube.solapple.client.gui.elements.UILabel;
import com.hexicube.solapple.tracking.ProgressInfo;

import java.awt.*;
import java.util.List;

import static com.hexicube.solapple.lib.Localization.localized;

final class StatListPage extends Page {
	StatListPage(FoodData foodData, List<SOLAppleConfig.Server.FoodGroupConfig> foodGroups, Rectangle frame) {
		super(frame, localized("gui", "food_book.stats"));

		ProgressInfo progressInfo = foodData.progressInfo;
		if (SOLAppleConfig.shouldShowUneatenFoods()) {
			mainStack.addChild(statWithBarFraction(
				FoodBookScreen.emptyBarGreen, FoodBookScreen.fullBarGreen,
				localized("gui", "food_book.stats.foods_tasted"),
				progressInfo.foodsEaten.size(), foodData.validFoods.size())
			);
		} else {
			mainStack.addChild(basicStat(
				String.valueOf(progressInfo.foodsEaten.size()),
				localized("gui", "food_book.stats.foods_tasted")
			));
		}

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
			complete, foodGroups.size(),
			24, 12
		));

		mainStack.addChild(makeSeparatorLine());

		mainStack.addChild(statWithFractionDynamic(
			FoodBookScreen.heartImage, FoodBookScreen.emptyHeartImage,
			FoodBookScreen.emptyBarRed, FoodBookScreen.fullBarRed,
			localized("gui", "food_book.stats.hearts_gained"),
			gainedHearts, totalHearts,
			36, 12
		));

		/*int allFoods = FoodItems.getAllFoodsIgnoringBlacklist().size();
		int allowedFoods = FoodItems.getAllFoods().size();
		int unused = allFoods - allowedFoods;
		if (unused > 0) {
			mainStack.addChild(makeSeparatorLine());
			UILabel unusedLabel = new UILabel(unused + " " + localized("gui", "food_book.stats.unused", unused));
			unusedLabel.color = FoodBookScreen.lessBlack;
			mainStack.addChild(unusedLabel);
		}*/

		updateMainStack();
	}
}
