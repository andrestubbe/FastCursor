package fastcursor;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Direct Win32 FFM bindings for user32.dll cursor functions.
 * Pure Java 21+ FFM with zero external DLLs and zero JNI overhead.
 */
final class NativeUser32Cursor {

    private static final MethodHandle SET_CURSOR;
    private static final MethodHandle GET_CURSOR;
    private static final MethodHandle LOAD_CURSOR_W;
    private static final MethodHandle SHOW_CURSOR;
    private static final MethodHandle CLIP_CURSOR;
    private static final MethodHandle SET_CLASS_LONG_PTR_W;
    private static final MethodHandle GET_CLASS_LONG_PTR_W;

    // GCLP_HCURSOR constant for 64-bit Windows (-12)
    static final int GCLP_HCURSOR = -12;

    // Cache preloaded HCURSOR handles so LoadCursorW is called at most once per type
    private static final Map<Integer, MemorySegment> CURSOR_CACHE = new ConcurrentHashMap<>();

    static {
        MethodHandle setCursor = null;
        MethodHandle getCursor = null;
        MethodHandle loadCursorW = null;
        MethodHandle showCursor = null;
        MethodHandle clipCursor = null;
        MethodHandle setClassLongPtrW = null;
        MethodHandle getClassLongPtrW = null;

        try {
            Linker linker = Linker.nativeLinker();
            SymbolLookup user32 = SymbolLookup.libraryLookup("user32.dll", Arena.global());

            // HCURSOR SetCursor(HCURSOR hCursor)
            setCursor = user32.find("SetCursor")
                    .map(addr -> linker.downcallHandle(addr,
                            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)))
                    .orElse(null);

            // HCURSOR GetCursor()
            getCursor = user32.find("GetCursor")
                    .map(addr -> linker.downcallHandle(addr,
                            FunctionDescriptor.of(ValueLayout.ADDRESS)))
                    .orElse(null);

            // HCURSOR LoadCursorW(HINSTANCE hInstance, LPCWSTR lpCursorName)
            loadCursorW = user32.find("LoadCursorW")
                    .map(addr -> linker.downcallHandle(addr,
                            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)))
                    .orElse(null);

            // int ShowCursor(BOOL bShow)
            showCursor = user32.find("ShowCursor")
                    .map(addr -> linker.downcallHandle(addr,
                            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)))
                    .orElse(null);

            // BOOL ClipCursor(const RECT *lpRect)
            clipCursor = user32.find("ClipCursor")
                    .map(addr -> linker.downcallHandle(addr,
                            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)))
                    .orElse(null);

            // ULONG_PTR SetClassLongPtrW(HWND hWnd, int nIndex, LONG_PTR dwNewLong)
            // On 64-bit Windows, dwNewLong and return are 64-bit (JAVA_LONG / ADDRESS)
            setClassLongPtrW = user32.find("SetClassLongPtrW")
                    .or(() -> user32.find("SetClassLongW"))
                    .map(addr -> linker.downcallHandle(addr,
                            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG)))
                    .orElse(null);

            // ULONG_PTR GetClassLongPtrW(HWND hWnd, int nIndex)
            getClassLongPtrW = user32.find("GetClassLongPtrW")
                    .or(() -> user32.find("GetClassLongW"))
                    .map(addr -> linker.downcallHandle(addr,
                            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT)))
                    .orElse(null);

        } catch (Throwable ignored) {
        }

        SET_CURSOR = setCursor;
        GET_CURSOR = getCursor;
        LOAD_CURSOR_W = loadCursorW;
        SHOW_CURSOR = showCursor;
        CLIP_CURSOR = clipCursor;
        SET_CLASS_LONG_PTR_W = setClassLongPtrW;
        GET_CLASS_LONG_PTR_W = getClassLongPtrW;
    }

    private NativeUser32Cursor() {}

    /**
     * Resolves the HCURSOR handle for a standard Win32 IDC_* resource identifier.
     */
    static MemorySegment loadSystemCursor(int idcValue) {
        return CURSOR_CACHE.computeIfAbsent(idcValue, id -> {
            if (LOAD_CURSOR_W == null) return MemorySegment.NULL;
            try {
                // MAKEINTRESOURCE(id) casts integer ID to pointer address
                MemorySegment resourceId = MemorySegment.ofAddress(id);
                return (MemorySegment) LOAD_CURSOR_W.invokeExact(MemorySegment.NULL, resourceId);
            } catch (Throwable t) {
                return MemorySegment.NULL;
            }
        });
    }

    /**
     * Sets the active cursor for the calling thread.
     */
    static MemorySegment setCursor(MemorySegment hCursor) {
        if (SET_CURSOR == null || hCursor == null) return MemorySegment.NULL;
        try {
            return (MemorySegment) SET_CURSOR.invokeExact(hCursor);
        } catch (Throwable t) {
            return MemorySegment.NULL;
        }
    }

    /**
     * Gets the current cursor handle of the calling thread.
     */
    static MemorySegment getCursor() {
        if (GET_CURSOR == null) return MemorySegment.NULL;
        try {
            return (MemorySegment) GET_CURSOR.invokeExact();
        } catch (Throwable t) {
            return MemorySegment.NULL;
        }
    }

    /**
     * Shows or hides the mouse cursor. Returns internal display counter.
     */
    static int showCursor(boolean show) {
        if (SHOW_CURSOR == null) return 0;
        try {
            return (int) SHOW_CURSOR.invokeExact(show ? 1 : 0);
        } catch (Throwable t) {
            return 0;
        }
    }

    /**
     * Sets the default class cursor for a window HWND.
     * When the mouse hovers over the window, Windows automatically applies this cursor.
     */
    static long setWindowCursor(long hwnd, long hCursor) {
        if (SET_CLASS_LONG_PTR_W == null || hwnd == 0) return 0;
        try {
            return (long) SET_CLASS_LONG_PTR_W.invokeExact(hwnd, GCLP_HCURSOR, hCursor);
        } catch (Throwable t) {
            return 0;
        }
    }

    /**
     * Confines the cursor to a specific rectangular screen region, or unclips if rect is null.
     */
    static boolean clipCursor(int left, int top, int right, int bottom) {
        if (CLIP_CURSOR == null) return false;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment rect = arena.allocate(ValueLayout.JAVA_INT, 4);
            rect.set(ValueLayout.JAVA_INT, 0, left);
            rect.set(ValueLayout.JAVA_INT, 4, top);
            rect.set(ValueLayout.JAVA_INT, 8, right);
            rect.set(ValueLayout.JAVA_INT, 12, bottom);
            return ((int) CLIP_CURSOR.invokeExact(rect)) != 0;
        } catch (Throwable t) {
            return false;
        }
    }

    static boolean unclipCursor() {
        if (CLIP_CURSOR == null) return false;
        try {
            return ((int) CLIP_CURSOR.invokeExact(MemorySegment.NULL)) != 0;
        } catch (Throwable t) {
            return false;
        }
    }
}
