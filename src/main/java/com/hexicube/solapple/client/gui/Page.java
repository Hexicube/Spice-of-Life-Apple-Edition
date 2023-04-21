package com.hexicube.solapple.client.gui;

import com.hexicube.solapple.client.gui.elements.*;

import java.awt.*;

import static com.hexicube.solapple.lib.Localization.localized;

abstract class Page extends UIElement {
	final UIStack mainStack;
	final int spacing = 6;
	
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
	
	void updateMainStack() {
		mainStack.setCenterX(getCenterX());
		mainStack.setMinY(getMinY() + 17);
		mainStack.updateFrames();
	}
	
	String fraction(int numerator, int denominator) {
		return localized("gui", "food_book.fraction",
			numerator,
			denominator
		);
	}
	
	UIElement makeSeparatorLine() {
		return UIBox.horizontalLine(0, getWidth() / 2, 0, FoodBookScreen.leastBlack);
	}
	
	UIElement statWithIcon(ImageData icon, String value, String name) {
		UIStack valueStack = new UIStack();
		valueStack.axis = UIStack.Axis.HORIZONTAL;
		valueStack.spacing = 3;
		
		valueStack.addChild(new UIImage(icon));
		valueStack.addChild(new UILabel(value));
		
		UIStack fullStack = new UIStack();
		fullStack.axis = UIStack.Axis.VERTICAL;
		fullStack.spacing = 2;
		
		fullStack.addChild(valueStack);
		UILabel nameLabel = new UILabel(name);
		nameLabel.color = FoodBookScreen.lessBlack;
		fullStack.addChild(nameLabel);
		
		return fullStack;
	}
}
