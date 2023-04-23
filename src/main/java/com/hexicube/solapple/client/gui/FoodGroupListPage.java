package com.hexicube.solapple.client.gui;

import com.hexicube.solapple.SOLAppleConfig;
import com.hexicube.solapple.client.gui.elements.*;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.hexicube.solapple.lib.Localization.localized;

final class FoodGroupListPage extends Page {
	private static final int itemsPerPage = 4;

	static List<FoodGroupListPage> pages(FoodData data, Rectangle frame, List<SOLAppleConfig.Server.FoodGroupConfig> items) {
		List<FoodGroupListPage> pages = new ArrayList<>();
		for (int startIndex = 0; startIndex < items.size(); startIndex += FoodGroupListPage.itemsPerPage) {
			int endIndex = Math.min(startIndex + FoodGroupListPage.itemsPerPage, items.size());
			pages.add(new FoodGroupListPage(data, frame, items.subList(startIndex, endIndex)));
		}
		return pages;
	}

	private FoodGroupListPage(FoodData data, Rectangle frame, List<SOLAppleConfig.Server.FoodGroupConfig> items) {
		super(frame, localized("gui", "food_book.groups"));

		boolean showUneaten = SOLAppleConfig.shouldShowUneatenFoods();
		for (int i = 0; i < items.size(); i++) {
			if (i != 0) mainStack.addChild(makeSeparatorLine());

			SOLAppleConfig.Server.FoodGroupConfig item = items.get(i);

			int eaten = item.filterList(data.eatenFoods).size();
			boolean done = eaten >= item.foods.size();
			String progress = showUneaten ?
					fraction(eaten, item.foods.size()) :
					String.valueOf(eaten);
			ImageData icon = done ?
					FoodBookScreen.drumstickImage :
					FoodBookScreen.emptyDrumstickImage;
			String hearts = fraction(done ? item.hearts : 0, item.hearts);
			ImageData secondIcon = done ?
					FoodBookScreen.heartImage :
					FoodBookScreen.emptyHeartImage;

			mainStack.addChild(dualStatWithIcons(progress, hearts, item.name, icon, secondIcon));
		}

		updateMainStack();
	}
}
