package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import org.jetbrains.annotations.NotNull;

public class WBoxCustom extends WBox {

    /**
     * Constructs a box.
     *
     * @param axis the box axis
     * @throws NullPointerException if the axis is null
     */
    public WBoxCustom(Axis axis) {
        super(axis);
    }

    @Override
    public void add(@NotNull WWidget widget) {
        add(widget, 18);
    }

    public void add(@NotNull WWidget widget, int sideLength) {
        switch (axis) {
            case HORIZONTAL -> add(widget, sideLength, 0);
            case VERTICAL -> add(widget, 0, sideLength);
        }
    }

    @Override
    public void layout() {
        int dimension = axis.choose(insets.left(), insets.top());

        // Set position offset from alignment along the box axis
        if (axis == Axis.HORIZONTAL && horizontalAlignment != HorizontalAlignment.LEFT) {
            int widgetWidth = spacing * (children.size() - 1);
            for (WWidget child : children) {
                widgetWidth += child.getWidth();
            }

            if (horizontalAlignment == HorizontalAlignment.CENTER) {
                dimension = (getWidth() - widgetWidth) / 2;
            } else { // right
                dimension = getWidth() - widgetWidth - insets.right();
            }
        } else if (verticalAlignment != VerticalAlignment.TOP) {
            int widgetHeight = spacing * (children.size() - 1);
            for (WWidget child : children) {
                widgetHeight += child.getHeight();
            }

            if (verticalAlignment == VerticalAlignment.CENTER) {
                dimension = (getHeight() - widgetHeight) / 2;
            } else { // bottom
                dimension = getHeight() - widgetHeight;
            }
        }

        for (int i = 0; i < children.size(); i++) {
            WWidget child = children.get(i);

            if (axis == Axis.HORIZONTAL) {
                int y = switch (verticalAlignment) {
                    case TOP -> insets.top();
                    case CENTER ->
                            insets.top() + (getHeight() - insets.top() - insets.bottom() - child.getHeight()) / 2;
                    case BOTTOM -> getHeight() - insets.bottom() - child.getHeight();
                };

                child.setLocation(dimension, y);
                if (child.canResize()) {
                    child.setSize(child.getWidth(), height - insets.top() - insets.bottom());
                }
            } else {
                int x = switch (horizontalAlignment) {
                    case LEFT -> insets.left();
                    case CENTER -> insets.left() + (getWidth() - insets.left() - insets.right() - child.getWidth()) / 2;
                    case RIGHT -> getWidth() - insets.right() - child.getWidth();
                };

                child.setLocation(x, dimension);
                if (child.canResize()) {
                    child.setSize(width - insets.left() - insets.right(), child.getHeight());
                }
            }

            if (child instanceof WPanel) ((WPanel) child).layout();
            //expandToFit(child, insets);

            if (i != children.size() - 1) {
                dimension += spacing;
            }

            dimension += axis.choose(child.getWidth(), child.getHeight());
        }
    }
}
