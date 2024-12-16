package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.client.Scissors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

/**
 * Copy from {@link io.github.cottonmc.cotton.gui.client.Scissors}
 */

/**
 * Contains a stack for GL scissors for restricting the drawn area of a widget.
 *
 * @since 2.0.0
 */
@Environment(EnvType.CLIENT)
public final class ScissorsBugFix {
    private static final ArrayDeque<Frame> STACK = new ArrayDeque<>();

    private ScissorsBugFix() {
    }

    /**
     * Pushes a new scissor frame onto the stack and refreshes the scissored area.
     *
     * @param x the frame's X coordinate
     * @param y the frame's Y coordinate
     * @param width the frame's width in pixels
     * @param height the frame's height in pixels
     * @return the pushed frame
     */
    public static Frame push(int x, int y, int width, int height) {
        return push(null, x, y, width, height);
    }

    /**
     * Pushes a new scissor frame onto the stack and refreshes the scissored area.
     *
     * <p>If the draw context is not null, any buffered content in it will be drawn
     * when refreshing the scissor state.
     *
     * @param context the associated draw context, or null if not provided
     * @param x the frame's X coordinate
     * @param y the frame's Y coordinate
     * @param width the frame's width in pixels
     * @param height the frame's height in pixels
     * @return the pushed frame
     */
    public static Frame push(@Nullable DrawContext context, int x, int y, int width, int height) {
        Frame frame = new Frame(x, y, width, height, context);
        STACK.push(frame);
        if (context != null) context.draw();
        refreshScissors();

        return frame;
    }

    /**
     * Pops the topmost scissor frame and refreshes the scissored area.
     *
     * @throws IllegalStateException if there are no scissor frames on the stack
     */
    public static void pop() {
        if (STACK.isEmpty()) {
            throw new IllegalStateException("No scissors on the stack!");
        }

        var frame = STACK.pop();
        if (frame.context != null) frame.context.draw();
        refreshScissors();
    }

    static void refreshScissors() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (STACK.isEmpty()) {
            // Just use the full window framebuffer as a scissor
            GL11.glScissor(0, 0, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
            return;
        }

        int x = Integer.MIN_VALUE;
        int y = Integer.MIN_VALUE;
        int width = -1;
        int height = -1;

        for (Frame frame : STACK) {
            if (x < frame.x) {
                x = frame.x;
            }
            if (y < frame.y) {
                y = frame.y;
            }
            if (width == -1 || x + width > frame.x + frame.width) {
                width = frame.width - (x - frame.x);
            }
            if (height == -1 || y + height > frame.y + frame.height) {
                height = frame.height - (y - frame.y);
            }
        }

        int windowHeight = mc.getWindow().getFramebufferHeight();
        double scale = mc.getWindow().getScaleFactor();
        int scaledWidth = (int) (width * scale);
        int scaledHeight = (int) (height * scale);

        // Expression for Y coordinate adapted from vini2003's Spinnery (code snippet released under WTFPL)
        // Prevent OpenGL debug message: id=1281, source=API, type=ERROR, severity=HIGH, message='GL_INVALID_VALUE error generated. Width and height must not be negative.'
        GL11.glScissor(
                (int) (x * scale),
                (int) (windowHeight - (y * scale) - scaledHeight),
                Math.max(0, scaledWidth),
                Math.max(0, scaledHeight)
        );
    }

    /**
     * Internal method. Throws an {@link IllegalStateException} if the scissor stack is not empty.
     */
    static void checkStackIsEmpty() {
        if (!STACK.isEmpty()) {
            throw new IllegalStateException("Unpopped scissor frames: " + STACK.stream().map(Frame::toString).collect(Collectors.joining(", ")));
        }
    }

    /**
     * A single scissor frame in the stack.
     */
    public static final class Frame implements AutoCloseable {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final @Nullable DrawContext context;

        private Frame(int x, int y, int width, int height, @Nullable DrawContext context) {
            if (width < 0) throw new IllegalArgumentException("Negative width for a stack frame");
            if (height < 0) throw new IllegalArgumentException("Negative height for a stack frame");

            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.context = context;
        }

        /**
         * Pops this frame from the stack.
         *
         * @throws IllegalStateException if: <ul>
         *                               <li>this frame is not on the stack, or</li>
         *                               <li>this frame is not the topmost element on the stack</li>
         *                               </ul>
         * @see Scissors#pop()
         */
        @Override
        public void close() {
            if (STACK.peekLast() != this) {
                if (STACK.contains(this)) {
                    throw new IllegalStateException(this + " is not on top of the stack!");
                } else {
                    throw new IllegalStateException(this + " is not on the stack!");
                }
            }

            pop();
        }

        @Override
        public String toString() {
            return "Frame{ at = (" + x + ", " + y + "), size = (" + width + ", " + height + ") }";
        }
    }
}
