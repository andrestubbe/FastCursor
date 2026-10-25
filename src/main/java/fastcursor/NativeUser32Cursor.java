package fastcursor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Direct Win32 bindings for user32.dll cursor functions.
 * High-performance native integration for Java 21+ without preview flags.
 */
final class NativeUser32Cursor {

    // Cache preloaded HCURSOR handles so LoadCursorW is called at most once per type
    private static final Map<Integer, Long> CURSOR_CACHE = new ConcurrentHashMap<>();

    private NativeUser32Cursor() {}

    static native long loadCursorW(int idcValue);
    static native long setCursor(long hCursor);
    static native long getCursor();
    static native int showCursor(boolean show);
    static native long setWindowCursor(long hwnd, long hCursor);

    /**
     * Resolves the HCURSOR handle for a standard Win32 IDC_* resource identifier.
     */
    static long loadSystemCursor(int idcValue) {
        return CURSOR_CACHE.computeIfAbsent(idcValue, id -> {
            try {
                return loadCursorW(id);
            } catch (Throwable t) {
                return 0L;
            }
        });
    }
}
