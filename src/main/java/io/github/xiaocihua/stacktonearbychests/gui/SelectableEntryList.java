package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.WScrollBar;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.data.Texture;
import juuxel.libninepatch.NinePatch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.xiaocihua.stacktonearbychests.ModOptions.MOD_ID;

/**
 * Copy from {@link WListPanel}.
 */
public class SelectableEntryList<D> extends WClippedPanelCustom {
	/**
	 * The widgets whose host hasn't been set yet.
	 */
	private final List<Entry<D>> requiresHost = new ArrayList<>();
	private final List<D> selectedData = new ArrayList<>();
	/**
	 * The list of data that this list represents.
	 */
	protected List<D> data;
	/**
	 * The supplier of new empty widgets.
	 */
	protected Function<D, Entry<D>> supplier;
	protected HashMap<D, Entry<D>> configured = new HashMap<>();
	protected List<Entry<D>> unconfigured = new ArrayList<>();
	/**
	 * The height of each child cell.
	 */
	protected int cellHeight = 20;
	protected int margin = 0;
	/**
	 * The scroll bar of this list.
	 */
	protected WScrollBar scrollBar = new WScrollBarCustom(Axis.VERTICAL);
	private int lastScroll = -1;

	private Optional<Consumer<List<D>>> changedListener = Optional.empty();

	public SelectableEntryList(Function<D, Entry<D>> supplier) {
		this(Collections.emptyList(), supplier);
	}

	public SelectableEntryList(Collection<D> data, Function<D, Entry<D>> supplier) {
		this.supplier = supplier;
		scrollBar.setParent(this);
		this.data = new ArrayList<>(data);
	}

	public void addData(Collection<D> data) {
		this.data.addAll(data);
		this.data = this.data.stream().distinct().collect(Collectors.toList());
		onChanged();
		layout();
	}

	public void setData(List<D> data) {
		this.data = data;
		onChanged();
		layout();
	}

	public List<D> getSelectedData() {
		return selectedData;
	}

	public void removeSelected() {
		data.removeAll(selectedData);
		onChanged();
		configured.clear();
		unconfigured.clear();
		selectedData.clear();
		layout();
	}

	public SelectableEntryList<D> setChangedListener(Consumer<List<D>> changedListener) {
		this.changedListener = Optional.ofNullable(changedListener);
		return this;
	}

	private void onChanged() {
		changedListener.ifPresent(listener -> listener.accept(this.data));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
		ScreenDrawing.coloredRect(context, x, y, this.width, this.height, 0xFF_262626);

		if (scrollBar.getValue() != lastScroll) {
			layout();
			lastScroll = scrollBar.getValue();
		}

		super.paint(context, x, y, mouseX, mouseY);
	}

	private Entry<D> createChild(D d) {
		Entry<D> child = supplier.apply(d);
		child.setParent(this);
		child.setParentList(this);
		// Set up the widget's host
		if (host != null) {
			// setHost instead of validate since we cannot have independent validations
			child.setHost(host);
		} else {
			requiresHost.add(child);
		}
		return child;
	}

	@Override
	public void validate(GuiDescription c) {
		super.validate(c);
		setRequiredHosts(c);
	}

	@Override
	public void setHost(GuiDescription host) {
		super.setHost(host);
		setRequiredHosts(host);
	}

	private void setRequiredHosts(GuiDescription host) {
		for (Entry<D> widget : requiresHost) {
			widget.setHost(host);
		}
		requiresHost.clear();
	}

	@Override
	public void layout() {
		children.clear();
		children.add(scrollBar);
		scrollBar.setLocation(this.width - scrollBar.getWidth(), 0);
		scrollBar.setSize(8, this.height);

		//super.layout();

		//System.out.println("Validating");

		int layoutHeight = this.getHeight() - (margin * 2);
		int cellsHigh = Math.max((layoutHeight + margin) / (cellHeight + margin), 1); // At least one cell is always visible

		//System.out.println("Adding children...");

		//this.children.clear();
		//this.children.add(scrollBar);
		//scrollBar.setLocation(this.width-scrollBar.getWidth(), 0);
		//scrollBar.setSize(8, this.height);

		//Fix up the scrollbar handle and track metrics
		scrollBar.setWindow(cellsHigh);
		scrollBar.setMaxValue(data.size());
		int scrollOffset = scrollBar.getValue();
		//System.out.println(scrollOffset);

		int presentCells = Math.min(data.size() - scrollOffset, cellsHigh);

		if (presentCells > 0) {
			for (int i = 0; i < presentCells; i++) {
				int index = i + scrollOffset;
				if (index >= data.size()) break;
				if (index < 0) continue; //THIS IS A THING THAT IS HAPPENING >:(
				D d = data.get(index);
				Entry<D> w = configured.get(d);
				if (w == null) {
					if (unconfigured.isEmpty()) {
						w = createChild(d);
					} else {
						w = unconfigured.remove(0);
					}
					configured.put(d, w);
				}

				//At this point, w is nonnull and configured by d
				if (w.canResize()) {
					w.setSize(this.width - (margin * 2) - scrollBar.getWidth(), cellHeight);
				}
				w.setLocation(margin, margin + ((cellHeight + margin) * i));
				this.children.add(w);
			}
		}

		//System.out.println("Children: "+children.size());
	}

	@Override
	public InputResult onMouseScroll(int x, int y, double horizontalAmount, double verticalAmount) {
		return scrollBar.onMouseScroll(0, 0, horizontalAmount, verticalAmount);
	}

	public void select(D data) {
		this.selectedData.add(data);
		layout();
	}

	public void unSelect(D data) {
		this.selectedData.remove(data);
		layout();
	}

	public static abstract class Entry<D> extends WWidget {

		protected static final int TEXT_COLOR = 0xFF_F5F5F5;
		private static final BackgroundPainter UNSELECTED = BackgroundPainter.createNinePatch(Identifier.of(MOD_ID, "textures/background_dark.png"));
		private static final BackgroundPainter SELECTED = BackgroundPainter.createNinePatch(new Texture(Identifier.of(MOD_ID, "textures/background_dark_selected.png")),
				builder -> builder.mode(NinePatch.Mode.STRETCHING).cornerSize(4).cornerUv(0.25f));
		protected Optional<SelectableEntryList<D>> parentList = Optional.empty();
		protected boolean isSelected = false;

		protected D data;

		public Entry(D data) {
			this.data = data;
		}

		public D getData() {
			return data;
		}

		public void setParentList(SelectableEntryList<D> parentList) {
			this.parentList = Optional.ofNullable(parentList);
		}

		@Override
		public InputResult onClick(Click click, boolean doubled) {
			this.isSelected = !this.isSelected;
			if (isSelected) {
				parentList.ifPresent(parent -> parent.select(data));
			} else {
				parentList.ifPresent(parent -> parent.unSelect(data));
			}
			return InputResult.PROCESSED;
		}

		@Override
		public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
			if (isSelected) {
				SELECTED.paintBackground(context, x, y, this);
			} else {
				UNSELECTED.paintBackground(context, x, y, this);
			}

		}

		@Override
		public boolean canResize() {
			return true;
		}
	}
}
