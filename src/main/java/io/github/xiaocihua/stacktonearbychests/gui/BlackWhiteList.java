package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.xiaocihua.stacktonearbychests.gui.ModOptionsGui.TEXT_COLOR;

public class BlackWhiteList extends WBoxCustom {

    private final SelectableEntryList<Identifier> list;

    public BlackWhiteList(Text title,
                          Collection<String> data,
                          Function<Identifier, SelectableEntryList.Entry<Identifier>> entrySupplier,
                          Consumer<Consumer<List<Identifier>>> onAddButtonClick,
                          Consumer<Set<String>> dataChangeListener) {
        super(Axis.VERTICAL);

        var titleLabel = new WLabel(title, TEXT_COLOR).setVerticalAlignment(VerticalAlignment.CENTER);
        add(titleLabel, 12);

        this.list = new SelectableEntryList<>(data.stream().map(Identifier::of).toList(), entrySupplier)
                .setChangedListener(identifiers -> dataChangeListener.accept(identifiers.stream().map(Identifier::toString).collect(Collectors.toSet())));

        var buttons = new WBoxCustom(Axis.HORIZONTAL);

        var addButton = new FlatColorButton(Text.of("+"))
                .setOnClick(() -> onAddButtonClick.accept(list::addData));
        buttons.add(addButton, 12);

        var removeButton = new FlatColorButton(Text.of("-"))
                .setOnClick(list::removeSelected);
        buttons.add(removeButton, 12);

        add(buttons, 12);

        add(list, 100);

        setSpacing(0);
    }
}
