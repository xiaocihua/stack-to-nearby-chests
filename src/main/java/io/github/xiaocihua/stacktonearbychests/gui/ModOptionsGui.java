package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.*;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import io.github.xiaocihua.stacktonearbychests.LockedSlots;
import io.github.xiaocihua.stacktonearbychests.ModOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static io.github.xiaocihua.stacktonearbychests.ModOptions.MOD_ID;

public class ModOptionsGui extends LightweightGuiDescription {
    public static final String PREFIX = MOD_ID + ".options.";
    public static final int TEXT_COLOR = 0xF5F5F5;
    public static final BackgroundPainter BACKGROUND_DARK = BackgroundPainter.createNinePatch(new Identifier(MOD_ID, "textures/background_dark.png"));
    public static final BackgroundPainter BACKGROUND_LIGHT = BackgroundPainter.createNinePatch(new Identifier(MOD_ID, "textures/background_light.png"));
    private static final Identifier CHECKED = new Identifier(MOD_ID, "textures/checkbox_checked.png");
    private static final Identifier UNCHECKED = new Identifier(MOD_ID, "textures/checkbox_unchecked.png");

    private static final int ROOT_WIDTH = 400;
    private final WPlainPanel root;
    @Nullable
    private WPanel currentDialog;

    private final ModOptions options = ModOptions.get();

    public ModOptionsGui() {
        root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(ROOT_WIDTH, 240);
        root.add(createTitle(), 0, 0, ROOT_WIDTH, 20);
        root.add(createTabs(), 0, 20, ROOT_WIDTH, 188);
        root.add(createBottomBar(), 0, 208, ROOT_WIDTH, 32);
        root.validate(this);
    }

    private WLabel createTitle() {
        return new WLabel(Text.translatable(PREFIX + "title"), TEXT_COLOR)
                .setVerticalAlignment(VerticalAlignment.CENTER)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
    }

    @NotNull
    private WTabPanelCustom createTabs() {
        var tabs = new WTabPanelCustom();
        tabs.add(new WScrollPanelCustom(createAppearance()), builder -> builder.title(Text.translatable(PREFIX + "appearance")));
        tabs.add(new WScrollPanelCustom(createBehavior()), builder -> builder.title(Text.translatable(PREFIX + "behavior")));
        tabs.add(new WScrollPanelCustom(createKeymap()), builder -> builder.title(Text.translatable(PREFIX + "keymap")));
        return tabs;
    }

    @NotNull
    private WBox createAppearance() {
        WBox appearance = createCard();

        MutableText favoriteItemStyleLabel = Text.translatable(PREFIX + "favoriteItemStyle");

        var favoriteItemStyle = new FlatColorButton() {
            private int index = LockedSlots.FAVORITE_ITEM_TAGS.indexOf(options.appearance.favoriteItemStyle);

            {
                setCurrent(options.appearance.favoriteItemStyle);
            }

            @Override
            public InputResult onClick(int x, int y, int button) {
                int amount = Screen.hasShiftDown() ? -1 : 1;
                index = MathHelper.floorMod(index + amount, LockedSlots.FAVORITE_ITEM_TAGS.size());
                setCurrent(LockedSlots.FAVORITE_ITEM_TAGS.get(index));
                return super.onClick(x, y, button);
            }

            public void setCurrent(Identifier id) {
                super.setLabel(Text.translatable(MOD_ID + ".resource." + id.getPath()));
                super.setIcon(new TextureIcon(new Identifier(id.getNamespace(), String.format(Locale.ROOT, "textures/%s%s", id.getPath(), ".png"))));
                options.appearance.favoriteItemStyle = id;
            }

            @Override
            public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
                ScreenDrawing.texturedRect(matrices, x + 1, y + 1, 18, 18, new Identifier(MOD_ID, "textures/slot_background.png"), 0xFF_FFFFFF);
                super.paint(matrices, x, y, mouseX, mouseY);
            }
        };

        favoriteItemStyle.setBorder();
        appearance.add(createLabeled(favoriteItemStyleLabel, favoriteItemStyle, 160), 350, 20);

        appearance.add(createCheckbox("showStackToNearbyContainersButton", options.appearance.showStackToNearbyContainersButton));
        appearance.add(createCheckbox("showRestockFromNearbyContainersButton", options.appearance.showRestockFromNearbyContainersButton));
        appearance.add(createCheckbox("showQuickStackButton", options.appearance.showQuickStackButton));
        appearance.add(createCheckbox("showRestockButton", options.appearance.showRestockButton));
        appearance.add(createIntTextField("stackToNearbyContainersButtonPosX", options.appearance.stackToNearbyContainersButtonPosX), 315, 20);
        appearance.add(createIntTextField("stackToNearbyContainersButtonPosY", options.appearance.stackToNearbyContainersButtonPosY), 315, 20);
        appearance.add(createIntTextField("restockFromNearbyContainersButtonPosX", options.appearance.restockFromNearbyContainersButtonPosX), 340, 20);
        appearance.add(createIntTextField("restockFromNearbyContainersButtonPosY", options.appearance.restockFromNearbyContainersButtonPosY), 340, 20);
        return appearance;
    }

    @NotNull
    private WBox createBehavior() {
        WBox behavior = createCard();

        TextFieldWithLabel searchInterval = createIntTextField("searchInterval", options.behavior.searchInterval)
                .withTooltip(PREFIX + "searchInterval.tooltip");
        searchInterval.getTextField().setTextPredicate(text -> NumberUtils.toInt(text, -1) >= 0);
        behavior.add(searchInterval, 230, 20);

        behavior.add(createCheckbox("doNotQuickStackItemsFromTheHotbar", options.behavior.doNotQuickStackItemsFromTheHotbar));

        behavior.add(createCheckbox("favoriteItemsCannotBePickedUp", options.behavior.favoriteItemsCannotBePickedUp));
        behavior.add(createCheckbox("favoriteItemStacksCannotBeQuickMoved", options.behavior.favoriteItemStacksCannotBeQuickMoved));
        behavior.add(createCheckbox("favoriteItemStacksCannotBeSwapped", options.behavior.favoriteItemStacksCannotBeSwapped));
        behavior.add(createCheckbox("favoriteItemStacksCannotBeThrown", options.behavior.favoriteItemStacksCannotBeThrown));
        behavior.add(createCheckbox("favoriteItemsCannotBeSwappedWithOffhand", options.behavior.favoriteItemsCannotBeSwappedWithOffhand));

        var stackingTargets = new BlackWhiteList(Text.translatable(PREFIX + "stackingTargets"),
                        options.behavior.stackingTargets,
                        BlockEntry::new,
                        consumer -> openDialog(new EntryPicker.ContainerPicker(consumer)),
                        data -> options.behavior.stackingTargets = data);
        behavior.add(stackingTargets, 230, 124);

        var itemsThatWillNotBeStacked = new BlackWhiteList(Text.translatable(PREFIX + "itemsThatWillNotBeStacked"),
                        options.behavior.itemsThatWillNotBeStacked,
                        ItemEntry::new,
                        consumer -> openDialog(new EntryPicker.ItemPicker(consumer)),
                        data -> options.behavior.itemsThatWillNotBeStacked = data);
        behavior.add(itemsThatWillNotBeStacked, 230, 124);

        var restockingSources = new BlackWhiteList(Text.translatable(PREFIX + "restockingSources"),
                        options.behavior.restockingSources,
                        BlockEntry::new,
                        consumer -> openDialog(new EntryPicker.ContainerPicker(consumer)),
                        data -> options.behavior.restockingSources = data);
        behavior.add(restockingSources, 230, 124);

        var itemsThatWillNotBeRestocked = new BlackWhiteList(Text.translatable(PREFIX + "itemsThatWillNotBeRestocked"),
                        options.behavior.itemsThatWillNotBeRestocked,
                        ItemEntry::new,
                        consumer -> openDialog(new EntryPicker.ItemPicker(consumer)),
                        data -> options.behavior.itemsThatWillNotBeRestocked = data);
        behavior.add(itemsThatWillNotBeRestocked, 230, 124);

        return behavior;
    }

    private WBox createKeymap() {
        WBox keymap = new WBoxCustom(Axis.VERTICAL).setInsets(Insets.ROOT_PANEL);

        keymap.add(new KeymapEntry(Text.translatable(PREFIX + "stackToNearbyContainers"), options.keymap.stackToNearbyContainersKey));
        keymap.add(new KeymapEntry(Text.translatable(PREFIX + "restockFromNearbyContainers"), options.keymap.restockFromNearbyContainersKey));
        keymap.add(new KeymapEntry(Text.translatable(PREFIX + "quickStack"), options.keymap.quickStackKey));
        keymap.add(new KeymapEntry(Text.translatable(PREFIX + "restock"), options.keymap.restockKey));
        keymap.add(new KeymapEntry(Text.translatable(PREFIX + "markAsFavorite"), options.keymap.markAsFavoriteKey));
        keymap.add(new KeymapEntry(Text.translatable(PREFIX + "openModOptionsScreen"), options.keymap.openModOptionsScreenKey));

        WLabel hint = new WLabel(Text.translatable(PREFIX + "keyMapHint").setStyle(Style.EMPTY.withItalic(true)), 0xBFBFBF)
                .setVerticalAlignment(VerticalAlignment.CENTER);
        keymap.add(hint);

        return keymap;
    }

    private WBox createBottomBar() {
        var bottom = (WBoxCustom) new WBoxCustom(Axis.HORIZONTAL).setInsets(new Insets(7))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        WButton doneButton = new FlatColorButton(Text.translatable(PREFIX + "done")).setBorder()
                .setOnClick(() -> {
                    options.write();
                    MinecraftClient.getInstance().currentScreen.close();
                });
        bottom.add(doneButton, 160);

        return bottom;
    }

    private WBox createCard() {
        return new WBox(Axis.VERTICAL).setInsets(Insets.ROOT_PANEL).setSpacing(10);
    }

    private WBox createLabeled(Text label, WWidget widget, int widgetWidth) {
        var wBox = new WBoxCustom(Axis.HORIZONTAL);
        wBox.add(new WLabel(label, TEXT_COLOR).setVerticalAlignment(VerticalAlignment.CENTER),
                MinecraftClient.getInstance().textRenderer.getWidth(label.asOrderedText()));
        wBox.add(widget, widgetWidth);
        return wBox;
    }

    private WToggleButton createCheckbox(String s, MutableBoolean isOn) {
        var checkbox = new WToggleButton(CHECKED, UNCHECKED, Text.translatable(PREFIX + s));

        checkbox.setColor(TEXT_COLOR, TEXT_COLOR);
        checkbox.setToggle(isOn.booleanValue());
        checkbox.setOnToggle(isOn::setValue);
        return checkbox;
    }

    private TextFieldWithLabel createIntTextField(String s, ModOptions.IntOption value) {
        var textField = new TextFieldWithLabel(Text.translatable(PREFIX + s), TEXT_COLOR, value::reset);
        textField.getTextField().setText(String.valueOf(value.intValue()));
        textField.getTextField().setChangedListener(text -> value.setValue(NumberUtils.toInt(text)));
        textField.getTextField().setTextPredicate(text -> text.matches("-?\\d+"));
        return textField;
    }

    public void openDialog(EntryPicker dialog) {
        if (currentDialog == null) {
            var operationBlockingPanel = new WPlainPanel();
            root.add(operationBlockingPanel, root.getX(), root.getY(), root.getWidth(), root.getHeight());

            currentDialog = dialog;
            int width = dialog.getWidth();
            int height = dialog.getHeight();
            root.add(dialog, (root.getWidth() - width) / 2, (root.getHeight() - height) / 2, width, height);

            dialog.setOnClose(() -> {
                currentDialog = null;
                root.remove(dialog);
                root.remove(operationBlockingPanel);
            });

            root.validate(this);
        }
    }

    @Override
    public void addPainters() {
        if (this.rootPanel != null && !fullscreen) {
            this.rootPanel.setBackgroundPainter(BACKGROUND_DARK);
        }
    }
}
