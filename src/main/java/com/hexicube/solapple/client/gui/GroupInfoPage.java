package com.hexicube.solapple.client.gui;

import com.hexicube.solapple.SOLAppleConfig;

import java.awt.*;

import static com.hexicube.solapple.lib.Localization.localized;

final class GroupInfoPage extends Page {
	GroupInfoPage(FoodData foodData, SOLAppleConfig.Server.FoodGroupConfig groupData, Rectangle frame) {
		super(frame, groupData.name, localized("gui", "food_book.group"));

		int eaten = groupData.filterList(foodData.eatenFoods).size();

		if (SOLAppleConfig.shouldShowUneatenFoods()) {
			mainStack.addChild(statWithFractionDynamic(
				FoodBookScreen.drumstickImage, FoodBookScreen.emptyDrumstickImage,
				FoodBookScreen.emptyBarBrown, FoodBookScreen.fullBarBrown,
				localized("gui", "food_book.stats.foods_tasted"),
				eaten, groupData.foods.size(),
				48, 12
			));
		} else {
			mainStack.addChild(basicStat(String.valueOf(eaten), localized("gui", "food_book.stats.foods_tasted")));
		}
		
		mainStack.addChild(makeSeparatorLine());
		
		int hearts = groupData.hearts;
		boolean complete = eaten == groupData.foods.size();
		
		mainStack.addChild(statWithFractionDynamic(
			FoodBookScreen.heartImage, FoodBookScreen.emptyHeartImage,
			FoodBookScreen.emptyBarRed, FoodBookScreen.fullBarRed,
			localized("gui", "food_book.stats.hearts_gained"),
			complete ? hearts : 0, hearts,
			48, 12
		));
		
		updateMainStack();
	}
}
