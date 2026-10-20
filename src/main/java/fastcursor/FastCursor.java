package fastcursor;

import java.lang.foreign.MemorySegment;

/**
 * <h1>FastCursor</h1>
 * Ultra-fast native Win32 cursor, pointer and clipping management for Java 21+.
 * <p>
 * Backed by direct Foreign Function &amp; Memory (FFM) system calls to {@code user32.dll}.
 * Zero native DLLs required, zero JNI compilation, instant execution.
 * </p>
 */
public final class FastCursor {

    private FastCursor() {}

    /**
     * Changes the current thread cursor immediately to the specified cursor type.
     *
     * @param type The standard system cursor shape to display
     * @return true if successfully loaded and set, false otherwise
     */
    public static boolean set(CursorType type) {
        if (type == null) return false;
        MemorySegment hCursor = NativeUser32Cursor.loadSystemCursor(type.getIdcValue());
        if (hCursor == null || hCursor.equals(MemorySegment.NULL)) {
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
        MemorySegment hCursor = NativeUser32Cursor.loadSystemCursor(type.getIdcValue());
        if (hCursor == null || hCursor.equals(MemorySegment.NULL)) {
            return false;
        }
        long previous = NativeUser32Cursor.setWindowCursor(hwnd, hCursor.address());
        return previous != 0;
    }

    /**
     * Shows the mouse cursor.
     */
    public static void show() {
        NativeUser32Cursor.showCursor(true);
    }

    /**
     * Hides the mouse cursor completely.
     */
    public static void hide() {
        NativeUser32Cursor.showCursor(false);
    }

    /**
     * Confines mouse cursor movements to a specific rectangle on the primary desktop.
     * Useful for FPS games, dragging controls, or precision region tools.
     *
     * @param left   Left screen pixel
     * @param top    Top screen pixel
     * @param right  Right screen pixel
     * @param bottom Bottom screen pixel
     */
    public static boolean clip(int left, int top, int right, int bottom) {
        return NativeUser32Cursor.clipCursor(left, top, right, bottom);
    }

    /**
     * Releases cursor confinement and restores free desktop movement.
     */
    public static boolean unclip() {
        return NativeUser32Cursor.unclipCursor();
    }
}
