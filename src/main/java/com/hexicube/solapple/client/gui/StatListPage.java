package com.hexicube.solapple.client.gui;

import com.hexicube.solapple.SOLAppleConfig;
import com.hexicube.solapple.client.gui.elements.UIBox;
import com.hexicube.solapple.tracking.ProgressInfo;

import java.awt.*;

import static com.hexicube.solapple.lib.Localization.localized;

final class StatListPage extends Page {
	StatListPage(FoodData foodData, Rectangle frame) {
		super(frame, localized("gui", "food_book.stats"));
		
		ProgressInfo progressInfo = foodData.progressInfo;
		ProgressGraph progressGraph = new ProgressGraph(foodData, getCenterX(), (int) mainStack.frame.getMinY() + 43);
		children.add(progressGraph);
		
		mainStack.addChild(new UIBox(progressGraph.frame, new Color(0, 0, 0, 0))); // invisible placeholder box
		
		mainStack.addChild(makeSeparatorLine());
		
		String foodsTasted;
		if (SOLAppleConfig.shouldShowUneatenFoods()) {
			foodsTasted = fraction(progressInfo.foodsEaten, foodData.validFoods.size());
		} else {
			foodsTasted = String.valueOf(progressInfo.foodsEaten);
		}
		
		mainStack.addChild(statWithIcon(
			FoodBookScreen.appleImage,
			foodsTasted,
			localized("gui", "food_book.stats.foods_tasted")
		));
		
		mainStack.addChild(makeSeparatorLine());
		
		int heartsPerMilestone = SOLAppleConfig.getHeartsPerMilestone();
		String heartsGained = fraction(
			heartsPerMilestone * progressInfo.milestonesAchieved(),
			heartsPerMilestone * SOLAppleConfig.getMilestoneCount()
		);
		
		mainStack.addChild(statWithIcon(
			FoodBookScreen.heartImage,
			heartsGained,
			localized("gui", "food_book.stats.hearts_gained")
		));
		
		mainStack.addChild(makeSeparatorLine());
		
		updateMainStack();
	}
}
