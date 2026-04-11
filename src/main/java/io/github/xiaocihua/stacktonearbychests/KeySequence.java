package io.github.xiaocihua.stacktonearbychests;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.xiaocihua.stacktonearbychests.event.OnKeyCallback;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

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
            return InteractionResult.PASS;
        });

        OnKeyCallback.RELEASE.register(key -> {
            PRESSING_KEYS.rem(key);
            return InteractionResult.PASS;
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
        Window window = Minecraft.getInstance().getWindow();
        return isMouseButton(key)
                ? GLFW.glfwGetMouseButton(window.handle(), key + MOUSE_BUTTON_CODE_OFFSET) == GLFW.GLFW_PRESS
                : InputConstants.isKeyDown(window, key);
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

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public boolean isPressed() {
        return !isEmpty() && PRESSING_KEYS.equals(keys);
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

    public Component getLocalizedText() {
        if (keys.isEmpty()) {
            return Component.translatable("key.keyboard.unknown");
        }

        String localized = keys.stream()
                .map(key -> {
                    if (isMouseButton(key)) {
                        return InputConstants.Type.MOUSE.getOrCreate(key + MOUSE_BUTTON_CODE_OFFSET);
                    } else {
                        return InputConstants.Type.KEYSYM.getOrCreate(key);
                    }
                })
                .map(key -> key.getDisplayName().getString())
                .collect(Collectors.joining(" + "));
        return Component.nullToEmpty(localized);
    }

    public boolean testThenRun(Runnable action) {
        if (isPressed()) {
            action.run();
            return true;
        }

        return false;
    }

    public KeySequence register(Supplier<InteractionResult> action) {
        OnKeyCallback.PRESS.register(key -> {
            if (!keys.isEmpty() && PRESSING_KEYS.equals(keys)) {
                return action.get();
            } else {
                return InteractionResult.PASS;
            }
        });

        return this;
    }

    public KeySequence registerOnScreen(Class<? extends Screen> screenClass, Consumer<Screen> action, InteractionResult result) {
        return register(() -> {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (screenClass.isInstance(currentScreen)) {
                action.accept(currentScreen);
                return result;
            } else {
                return InteractionResult.PASS;
            }
        });
    }

    public KeySequence registerNotOnScreen(Runnable action, InteractionResult result) {
        return register(() -> {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null && client.screen == null) {
                action.run();
                return result;
            } else {
                return InteractionResult.PASS;
            }
        });
    }
}