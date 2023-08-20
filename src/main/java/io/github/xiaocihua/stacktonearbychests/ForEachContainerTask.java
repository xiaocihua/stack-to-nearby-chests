package io.github.xiaocihua.stacktonearbychests;

import io.github.xiaocihua.stacktonearbychests.event.OnKeyCallback;
import io.github.xiaocihua.stacktonearbychests.event.SetScreenCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class ForEachContainerTask {

    private static final ScheduledThreadPoolExecutor TIMER = new ScheduledThreadPoolExecutor(1);
    private static ForEachContainerTask currentTask;

    protected final MinecraftClient client;
    protected final ClientPlayerEntity player;
    protected final Consumer<ScreenHandler> action;

    private boolean interrupted;
    private final int searchInterval;

    @Nullable
    private ForEachContainerTask after;

    public ForEachContainerTask(MinecraftClient client, ClientPlayerEntity player, Consumer<ScreenHandler> action) {
        this.client = client;
        this.player = player;
        this.action = action;
        searchInterval = ModOptions.get().behavior.searchInterval.intValue();
    }

    public static void init() {
        SetScreenCallback.EVENT.register(screen -> {
            if (isRunning()) {
                if (screen instanceof DeathScreen) {
                    currentTask.interrupt();
                    return ActionResult.PASS;
                }

                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        OnKeyCallback.PRESS.register(key -> {
            if (isRunning()) {
                if (key == GLFW.GLFW_KEY_ESCAPE) {
                    currentTask.interrupt();
                }

                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    public static ForEachContainerTask getCurrentTask() {
        return currentTask;
    }

    public static boolean isRunning() {
        return currentTask != null;
    }

    public void start() {
        currentTask = this;
        openNextContainer();
    }

    protected void stop() {
        player.closeHandledScreen();
        TIMER.getQueue().clear();
        currentTask = null;
    }

    public void interrupt() {
        sendChatMessage("stack-to-nearby-chests.message.actionInterrupted");
        interrupted = true;
    }

    private void openNextContainer() {
        // This method may be submitted to MinecraftClient for execution, so exceptions need to be handled here
        try {
            if (openNextContainerInternal()) {
                setTimeout();
            } else if (after != null) {
                after.start();
            } else {
                stop();
            }
        } catch (Exception e) {
            sendChatMessage("stack-to-nearby-chests.message.exceptionOccurred");
            StackToNearbyChests.LOGGER.error("An exception occurred", e);
            stop();
        }
    }

    /**
     * Open the next container.
     * @return {@code true} if successfully found and interacted with an eligible container
     */
    protected abstract boolean openNextContainerInternal();

    private void setTimeout() {
        TIMER.schedule(() -> client.execute(() -> {
            sendChatMessage("stack-to-nearby-chests.message.interruptedByTimeout");
            stop();
        }), 2, TimeUnit.SECONDS);
    }

    public void onNext(ScreenHandler screenHandler) {
        if (StackToNearbyChests.IS_PLAYEREX_MOD_LOADED
                && "com.github.clevernucleus.playerex.factory.ExScreenFactory.Handler".equals(screenHandler.getClass().getCanonicalName())) {
            return;
        }

        clearTimeout();

        action.accept(screenHandler);

        if (interrupted) {
            stop();
            return;
        }

        if (searchInterval == 0) {
            openNextContainer();
        } else {
            TIMER.schedule(() -> client.execute(this::openNextContainer), searchInterval, TimeUnit.MILLISECONDS);
        }
    }

    private void clearTimeout() {
        TIMER.getQueue().clear();
    }

    public void thenStart(ForEachContainerTask after) {
        this.after = after;
    }

    private void sendChatMessage(String key) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable(key));
    }
}
