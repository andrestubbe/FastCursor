package fastcursor;

import fastcore.FastCore;

/**
 * <h1>FastCursor</h1>
 * Ultra-fast native Win32 cursor and pointer management for Java.
 * <p>
 * Provides dual capabilities:
 * 1. <b>Pointer Management:</b> Change current thread or window cursor shapes (hand, arrow, I-beam, etc.)
 * 2. <b>Cursor Capture &amp; Blending:</b> Extract real-time cursor shape, position, and alpha-blend into frame buffers.
 * </p>
 */
public final class FastCursor {

    private static boolean captureLoaded = false;

    static {
        try {
            FastCore.loadLibrary("fastcursor", FastCursor.class);
            captureLoaded = true;
        } catch (Throwable t) {
            try {
                System.loadLibrary("fastcursor");
                captureLoaded = true;
            } catch (Throwable ignored) {
                captureLoaded = false;
            }
        }
    }

    private FastCursor() {}

    /**
     * Changes the current thread cursor immediately to the specified cursor type.
     *
     * @param type The standard system cursor shape to display
     * @return true if successfully loaded and set, false otherwise
     */
    public static boolean set(CursorType type) {
        if (type == null) return false;
        long hCursor = NativeUser32Cursor.loadSystemCursor(type.getIdcValue());
        if (hCursor == 0L) {
            return false;
        }
        NativeUser32Cursor.setCursor(hCursor);
        return true;
    }

    /**
     * Binds a standard cursor permanently to a native Win32 window (HWND).
     * Windows automatically applies this cursor whenever the mouse pointer enters the window client area.
     *
     * @param hwnd Native window handle
     * @param type Standard cursor shape
     * @return true on success, false otherwise
     */
    public static boolean setForWindow(long hwnd, CursorType type) {
        if (hwnd == 0 || type == null) return false;
        long hCursor = NativeUser32Cursor.loadSystemCursor(type.getIdcValue());
        if (hCursor == 0L) {
            return false;
        }
        long previous = NativeUser32Cursor.setWindowCursor(hwnd, hCursor);
        return previous != 0;
    }

    /**
     * Retrieves the current thread cursor handle.
     */
    public static long get() {
        return NativeUser32Cursor.getCursor();
    }

    /**
     * Increments the cursor display counter.
     */
    public static int show() {
        return NativeUser32Cursor.showCursor(true);
    }

    /**
     * Decrements the cursor display counter.
     */
    public static int hide() {
        return NativeUser32Cursor.showCursor(false);
    }

    /**
     * Returns true if native Win32 cursor capture is available on this system.
     */
    public static boolean isCaptureAvailable() {
        return captureLoaded;
    }

    /**
     * Native hook to capture current cursor shape and coordinates.
     *
     * @param metaOut int[6] receiving: [posX, posY, hotspotX, hotspotY, cursorWidth, cursorHeight]
     * @return int[] array of cursor pixels (32-bit ARGB/BGRA with alpha), or null if invisible/failed.
     */
    public static native int[] nativeCaptureCursor(int[] metaOut);

    /**
     * Captures current active cursor shape, hotspot and screen coordinates.
     *
     * @param metaOut int[6] receiving: [posX, posY, hotspotX, hotspotY, cursorWidth, cursorHeight]
     * @return int[] array of cursor pixels (32-bit ARGB with alpha), or null if invisible/failed.
     */
    public static int[] captureCursor(int[] metaOut) {
        if (!captureLoaded || metaOut == null || metaOut.length < 6) return null;
        try {
            return nativeCaptureCursor(metaOut);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Overlays the active hardware cursor directly onto a screen pixel buffer.
     *
     * @param screenPixels destination screen buffer (32-bit ARGB or BGRA)
     * @param screenW screen width
     * @param screenH screen height
     * @param originX screen left origin (e.g. 0 for full screen, or crop X)
     * @param originY screen top origin (e.g. 0 for full screen, or crop Y)
     * @param isBgra true if screen buffer is BGRA, false if ARGB
     */
    public static void blendCursor(int[] screenPixels, int screenW, int screenH, int originX, int originY, boolean isBgra) {
        if (!captureLoaded || screenPixels == null) return;

        int[] meta = new int[6];
        int[] cursorPixels = captureCursor(meta);
        if (cursorPixels == null) return;

        int posX = meta[0];
        int posY = meta[1];
        int hotX = meta[2];
        int hotY = meta[3];
        int curW = meta[4];
        int curH = meta[5];

        int drawX = (posX - hotX) - originX;
        int drawY = (posY - hotY) - originY;

        for (int cy = 0; cy < curH; cy++) {
            int targetY = drawY + cy;
            if (targetY < 0 || targetY >= screenH) continue;

            int screenRowStart = targetY * screenW;
            int cursorRowStart = cy * curW;

            for (int cx = 0; cx < curW; cx++) {
                int targetX = drawX + cx;
                if (targetX < 0 || targetX >= screenW) continue;

                int curPixel = cursorPixels[cursorRowStart + cx];
                int alpha = (curPixel >> 24) & 0xFF;

                if (alpha == 0) {
                    continue; // Transparent
                }

                int dstIdx = screenRowStart + targetX;
                if (alpha == 255) {
                    screenPixels[dstIdx] = curPixel;
                } else {
                    int dstPixel = screenPixels[dstIdx];

                    int srcR, srcG, srcB;
                    int dstR, dstG, dstB;

                    if (isBgra) {
                        srcB = curPixel & 0xFF;
                        srcG = (curPixel >> 8) & 0xFF;
                        srcR = (curPixel >> 16) & 0xFF;

                        dstB = dstPixel & 0xFF;
                        dstG = (dstPixel >> 8) & 0xFF;
                        dstR = (dstPixel >> 16) & 0xFF;
                    } else {
                        srcR = (curPixel >> 16) & 0xFF;
                        srcG = (curPixel >> 8) & 0xFF;
                        srcB = curPixel & 0xFF;

                        dstR = (dstPixel >> 16) & 0xFF;
                        dstG = (dstPixel >> 8) & 0xFF;
                        dstB = dstPixel & 0xFF;
                    }

                    int outR = (srcR * alpha + dstR * (255 - alpha)) / 255;
                    int outG = (srcG * alpha + dstG * (255 - alpha)) / 255;
                    int outB = (srcB * alpha + dstB * (255 - alpha)) / 255;

                    screenPixels[dstIdx] = (dstPixel & 0xFF000000) | (outR << 16) | (outG << 8) | outB;
                }
            }
        }
    }
}
