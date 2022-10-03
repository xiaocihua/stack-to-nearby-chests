package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

/**
 * A panel that is clipped to only render widgets inside its bounds.
 */
public class WClippedPanelCustom extends WPanel {
	@Environment(EnvType.CLIENT)
	@Override
	public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		if (getBackgroundPainter() != null) getBackgroundPainter().paintBackground(matrices, x, y, this);

		ScissorsBugFix.push(x, y, width, height);
		for (WWidget child : children) {
			child.paint(matrices, x + child.getX(), y + child.getY(), mouseX - child.getX(), mouseY - child.getY());
		}
		ScissorsBugFix.pop();
	}
}
