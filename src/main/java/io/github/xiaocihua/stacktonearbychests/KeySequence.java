package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.event.OnKeyCallback;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class KeySequence {
    public static final int MOUSE_BUTTON_CODE_OFFSET = 100;

    private static final IntArrayList PRESSING_KEYS = new IntArrayList();

    private List<Integer> keys;
    private final List<Integer> defaultKeys;

    public KeySequence(List<Integer> keys) {
        this.keys = new ArrayList<>(keys);
        this.defaultKeys = new ArrayList<>(keys);
    }

    public static KeySequence empty() {
        return new KeySequence(new ArrayList<>());
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

    // Fix the problem that all key bindings cannot be triggered because other mods cancel the key release event
    // The solution comes from malilib https://github.com/maruohon/malilib/issues/59
    public static void reCheckPressedKeys() {
        for (int key : PRESSING_KEYS) {
            if (!isKeyPressed(key)) {
                PRESSING_KEYS.rem(key);
            }
        }
    }

    public static boolean isKeyPressed(int key) {
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        return isMouseButton(key)
                ? GLFW.glfwGetMouseButton(window, key + MOUSE_BUTTON_CODE_OFFSET) == GLFW.GLFW_PRESS
                : InputUtil.isKeyPressed(window, key);
    }

    private static boolean isMouseButton(int key) {
        return key < -1;
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
                    if (isMouseButton(key)) {
                        return InputUtil.Type.MOUSE.createFromCode(key + MOUSE_BUTTON_CODE_OFFSET);
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

    public KeySequence register(Supplier<ActionResult> action) {
        OnKeyCallback.PRESS.register(key -> {
            if (!keys.isEmpty() && PRESSING_KEYS.equals(keys)) {
                return action.get();
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
                return result;
            } else {
                return ActionResult.PASS;
            }
        });
    }

    public KeySequence registerNotOnScreen(Runnable action, ActionResult result) {
        return register(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.currentScreen == null) {
                action.run();
                return result;
            } else {
                return ActionResult.PASS;
            }
        });
    }
}