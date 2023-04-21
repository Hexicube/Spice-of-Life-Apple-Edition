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
		
		int totalFoods = FoodItems.getAllFoods().size();
		int validFoods = foodData.validFoods.size();
		int cheapFoods = totalFoods - validFoods;
		int eatenCheapFoods = (int) foodData.foodList.getEatenFoods().stream()
			.map(FoodInstance::getItem)
			.filter(food -> SOLAppleConfig.isAllowed(food) && !SOLAppleConfig.isHearty(food))
			.count();
		
		{
			int minValue = SOLAppleConfig.getMinimumFoodValue();
			String minValueDesc = String.valueOf(minValue / 2);
			if (minValue % 2 == 1) {
				minValueDesc += ".5";
			}
			
			UIElement minValueStat = statWithIcon(
				FoodBookScreen.drumstickImage,
				minValueDesc,
				localized("gui", "food_book.config.minimum_food_value")
			);
			minValueStat.tooltip = localized("gui", "food_book.config.tooltip.minimum_food_value");
			mainStack.addChild(minValueStat);
		}
		
		{
			UIElement cheapStat = statWithIcon(
				FoodBookScreen.spiderEyeImage,
				fraction(eatenCheapFoods, cheapFoods),
				localized("gui", "food_book.config.eaten_cheap_foods")
			);
			cheapStat.tooltip = localized("gui", "food_book.config.tooltip.eaten_cheap_foods", cheapFoods, eatenCheapFoods);
			mainStack.addChild(cheapStat);
		}
		
		mainStack.addChild(makeSeparatorLine());
		
		{
			boolean hasWhitelist = SOLAppleConfig.hasWhitelist();
			String listKey = hasWhitelist ? "whitelist" : "blacklist";
			
			ImageData listIcon = hasWhitelist ? FoodBookScreen.whitelistImage : FoodBookScreen.blacklistImage;
			
			int allFoods = FoodItems.getAllFoodsIgnoringBlacklist().size();
			int allowedFoods = FoodItems.getAllFoods().size();
			String fraction = hasWhitelist
				? fraction(allowedFoods, allFoods)
				: fraction(allFoods - allowedFoods, allFoods);
			UIElement listStat = statWithIcon(
				listIcon,
				fraction,
				localized("gui", "food_book.config." + listKey)
			);
			listStat.tooltip = localized("gui", "food_book.config.tooltip." + listKey);
			mainStack.addChild(listStat);
		}
		
		mainStack.addChild(makeSeparatorLine());
		
		updateMainStack();
	}
}
