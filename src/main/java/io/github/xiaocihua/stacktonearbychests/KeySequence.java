package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.event.OnKeyCallback;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class KeySequence {
    public static final int MOUSE_BUTTON_CODE_OFFSET = 100;

    private static final KeySequence EMPTY = new KeySequence(new ArrayList<>());

    private static final IntArrayList PRESSING_KEYS = new IntArrayList();

    private List<Integer> keys;
    private final List<Integer> defaultKeys;

    public KeySequence(List<Integer> keys) {
        this.keys = new ArrayList<>(keys);
        this.defaultKeys = new ArrayList<>(keys);
    }

    public static KeySequence empty() {
        return EMPTY;
    }

    public static void init() {
        OnKeyCallback.PRESS.register(key -> {
            PRESSING_KEYS.add(key);
            return ActionResult.PASS;
        });

        OnKeyCallback.RELEASE.register(key -> {
            PRESSING_KEYS.rem(key);
            return ActionResult.PASS;
        });
    }

    public void addKey(int key) {
        if (keys.size() >= 3) {
            keys.clear();
        }
        if (!keys.contains(key)) {
            keys.add(key);
        }
    }

    public void addMouseButton(int button) {
        addKey(button - MOUSE_BUTTON_CODE_OFFSET);
    }

    public void clear() {
        keys.clear();
    }

    public void reset() {
        keys = new ArrayList<>(defaultKeys);
    }

    public Text getLocalizedText() {
        if (keys.isEmpty()) {
            return new TranslatableText("key.keyboard.unknown");
        }

        String localized = keys.stream()
                .map(key -> {
                    if (key < -1) {
                        return InputUtil.Type.MOUSE.createFromCode(key + KeySequence.MOUSE_BUTTON_CODE_OFFSET);
                    } else {
                        return InputUtil.Type.KEYSYM.createFromCode(key);
                    }
                })
                .map(key -> key.getLocalizedText().getString())
                .collect(Collectors.joining(" + "));
        return Text.of(localized);
    }

    public void testThenRun(Runnable action) {
        if (!keys.isEmpty() && PRESSING_KEYS.equals(keys)) {
            action.run();
        }
    }

    public KeySequence register(Runnable action, ActionResult result) {
        OnKeyCallback.PRESS.register(key -> {
            if (!keys.isEmpty() && PRESSING_KEYS.equals(keys)) {
                action.run();
                return result;
            } else {
                return ActionResult.PASS;
            }
        });

        return this;
    }

    public KeySequence registerOnScreen(Class<? extends Screen> screenClass, Consumer<Screen> action, ActionResult result) {
        return register(() -> {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            if (screenClass.isInstance(currentScreen)) {
                action.accept(currentScreen);
            }
        }, result);
    }

    public KeySequence registerNotOnScreen(Runnable action, ActionResult result) {
        return register(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.currentScreen == null) {
                action.run();
            }
        }, result);
    }
}