package com.hexicube.solapple.client.gui;

import com.hexicube.solapple.SOLAppleConfig;
import com.hexicube.solapple.client.FoodItems;
import com.hexicube.solapple.client.gui.elements.ImageData;
import com.hexicube.solapple.client.gui.elements.UIElement;
import com.hexicube.solapple.tracking.FoodInstance;

import java.awt.*;

import static com.hexicube.solapple.lib.Localization.localized;

final class ConfigInfoPage extends Page {
	ConfigInfoPage(FoodData foodData, Rectangle frame) {
		super(frame, localized("gui", "food_book.config"));
		
		{
			int allFoods = FoodItems.getAllFoodsIgnoringBlacklist().size();
			int allowedFoods = FoodItems.getAllFoods().size();
			UIElement listStat = statWithIcon(
				FoodBookScreen.blacklistImage,
				fraction(allFoods - allowedFoods, allFoods),
				localized("gui", "food_book.config.blacklist")
			);
			listStat.tooltip = localized("gui", "food_book.config.tooltip.blacklist");
			mainStack.addChild(listStat);
		}
		
		mainStack.addChild(makeSeparatorLine());
		
		updateMainStack();
	}
}