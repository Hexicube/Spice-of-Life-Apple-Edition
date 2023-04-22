package com.hexicube.solapple.client.gui;

import com.hexicube.solapple.SOLAppleConfig;
import com.hexicube.solapple.client.gui.elements.UIBox;
import com.hexicube.solapple.tracking.ProgressInfo;

import java.awt.*;

import static com.hexicube.solapple.lib.Localization.localized;

final class GroupInfoPage extends Page {
	GroupInfoPage(FoodData foodData, SOLAppleConfig.Server.FoodGroupConfig groupData, Rectangle frame) {
		super(frame, groupData.name, localized("gui", "food_book.group"));

		int eaten = groupData.filterList(foodData.eatenFoods).size();
		
		String foodsTasted;
		if (SOLAppleConfig.shouldShowUneatenFoods()) {
			foodsTasted = fraction(eaten, groupData.foods.size());
		} else {
			foodsTasted = String.valueOf(eaten);
		}
		
		mainStack.addChild(statWithIcon(
			FoodBookScreen.appleImage,
			foodsTasted,
			localized("gui", "food_book.stats.foods_tasted")
		));
		
		mainStack.addChild(makeSeparatorLine());
		
		int hearts = groupData.hearts;
		boolean complete = eaten == groupData.foods.size();
		
		mainStack.addChild(statWithFractionDynamic(
			FoodBookScreen.heartImage, FoodBookScreen.emptyHeartImage,
			FoodBookScreen.emptyBarRed, FoodBookScreen.fullBarRed,
			localized("gui", "food_book.stats.hearts_gained"),
			complete ? hearts : 0, hearts
		));
		
		mainStack.addChild(makeSeparatorLine());
		
		updateMainStack();
	}
}
