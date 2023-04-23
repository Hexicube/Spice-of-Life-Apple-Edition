package com.hexicube.solapple.client.gui.elements;

import java.awt.*;

public class UIFiller extends UIElement {
    public UIFiller(Rectangle frame) {
        super(frame);
    }

    public static UIFiller horizontal(int targetWidth, int spacing, UIElement... otherElems) {
        int width = targetWidth;
        for (UIElement elem : otherElems) {
            width -= spacing;
            width -= elem.getWidth();
        }
        return new UIFiller(new Rectangle(0, 0, width, 1));
    }
}
