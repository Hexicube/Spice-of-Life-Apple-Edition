package com.hexicube.solapple.client.gui;

import com.hexicube.solapple.client.gui.elements.*;

import java.awt.*;
import java.util.ArrayList;

import static com.hexicube.solapple.lib.Localization.localized;

abstract class Page extends UIElement {
	final UIStack mainStack;
	final int spacing = 5;
	
	Page(Rectangle frame, String header) {
		super(frame);
		
		mainStack = new UIStack();
		mainStack.axis = UIStack.Axis.VERTICAL;
		mainStack.spacing = spacing;
		
		UILabel headerLabel = new UILabel(header);
		mainStack.addChild(headerLabel);
		
		mainStack.addChild(makeSeparatorLine());
		
		children.add(mainStack);
		updateMainStack();
	}

	Page(Rectangle frame, String header, String subHeader) {
		super(frame);

		mainStack = new UIStack();
		mainStack.axis = UIStack.Axis.VERTICAL;
		mainStack.spacing = spacing;

		UILabel headerLabel = new UILabel(header);
		mainStack.addChild(headerLabel);

		UILabel headerLabel2 = new UILabel(subHeader);
		mainStack.addChild(headerLabel2);

		mainStack.addChild(makeSeparatorLine());

		children.add(mainStack);
		updateMainStack();
	}
	
	void updateMainStack() {
		mainStack.setCenterX(getCenterX());
		mainStack.setMinY(getMinY() + 17);
		mainStack.updateFrames();
	}
	
	static String fraction(int numerator, int denominator) {
		return localized("gui", "food_book.fraction",
			numerator,
			denominator
		);
	}
	
	UIElement makeSeparatorLine() {
		return UIBox.horizontalLine(0, getWidth() / 2, 0, FoodBookScreen.leastBlack);
	}
	
	UIElement basicStat(String value, String name) {
		UIStack fullStack = new UIStack();
		fullStack.axis = UIStack.Axis.HORIZONTAL;
		fullStack.spacing = 0;

		fullStack.addChild(new UILabel(value + " "));
		UILabel nameLabel = new UILabel(name);
		nameLabel.color = FoodBookScreen.lessBlack;
		fullStack.addChild(nameLabel);
		
		return fullStack;
	}

	UIElement statWithFractionDynamic(ImageData icon, ImageData altIcon, ImageData barEmpty, ImageData barFull, String name, int count, int total) {
		if (total > 20) return statWithBarFraction(barEmpty, barFull, name, count, total);
		if (total > 16) return statWithIconFraction(icon, altIcon, name, count, total, 10);
		if (total > 12) return statWithIconFraction(icon, altIcon, name, count, total, 8);
		if (total > 10) return statWithIconFraction(icon, altIcon, name, count, total, 6);
		return statWithIconFraction(icon, altIcon, name, count, total, 10);
	}

	UIElement statWithIconFraction(ImageData icon, ImageData altIcon, String name, int count, int total, int maxPerRow) {
		ArrayList<UIImage> iconList = new ArrayList<>();
		for (int a = 0; a < count; a++) iconList.add(new UIImage(icon));
		for (int a = 0; a < total - count; a++) iconList.add(new UIImage(altIcon));

		UIStack valueStack = new UIStack();
		valueStack.axis = UIStack.Axis.HORIZONTAL;
		valueStack.spacing = 0;

		valueStack.addChild(new UILabel(fraction(count, total)));
		UILabel nameLabel = new UILabel(" " + name);
		nameLabel.color = FoodBookScreen.lessBlack;
		valueStack.addChild(nameLabel);

		UIStack iconStackContainer = new UIStack();
		iconStackContainer.axis = UIStack.Axis.VERTICAL;
		iconStackContainer.spacing = 1;

		UIStack iconStack = null;
		for (int a = 0; a < iconList.size(); a++) {
			if (a % maxPerRow == 0) {
				if (iconStack != null) iconStackContainer.addChild(iconStack);
				iconStack = new UIStack();
				iconStack.spacing = -1;
			}
			iconStack.addChild(iconList.get(a));
		}
		if (iconStack != null) iconStackContainer.addChild(iconStack);

		UIStack fullStack = new UIStack();
		fullStack.axis = UIStack.Axis.VERTICAL;
		fullStack.spacing = 4;

		fullStack.addChild(valueStack);
		fullStack.addChild(iconStackContainer);

		return fullStack;
	}

	UIElement statWithBarFraction(ImageData barEmpty, ImageData barFull, String name, int count, int total) {
		UIStack valueStack = new UIStack();
		valueStack.axis = UIStack.Axis.HORIZONTAL;
		valueStack.spacing = 0;

		valueStack.addChild(new UILabel(fraction(count, total)));
		UILabel nameLabel = new UILabel(" " + name);
		nameLabel.color = FoodBookScreen.lessBlack;
		valueStack.addChild(nameLabel);

		UIStack barStack = new UIStack();
		barStack.axis = UIStack.Axis.HORIZONTAL;
		barStack.spacing = 0;
		if (count == 0) barStack.addChild(new UIImage(barEmpty));
		else if (count == total) barStack.addChild(new UIImage(barFull));
		else {
			int width = 110 * count / total;
			ImageData full = new ImageData(barFull.textureLocation,
				new Rectangle(barFull.partOfTexture.x, barFull.partOfTexture.y, width, 5),
				width, 5
			);
			barStack.addChild(new UIImage(full));
			ImageData empty = new ImageData(barEmpty.textureLocation,
				new Rectangle(barEmpty.partOfTexture.x + width, barEmpty.partOfTexture.y, 110 - width, 5),
				110 - width, 5
			);
			barStack.addChild(new UIImage(empty));
		}

		UIStack fullStack = new UIStack();
		fullStack.axis = UIStack.Axis.VERTICAL;
		fullStack.spacing = 4;

		fullStack.addChild(valueStack);
		fullStack.addChild(barStack);

		return fullStack;
	}
}
