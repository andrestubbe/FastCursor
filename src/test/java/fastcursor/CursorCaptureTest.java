package fastcursor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CursorCaptureTest {

    @Test
    void testCursorCaptureAvailability() {
        assertTrue(FastCursor.isCaptureAvailable(), "Win32 Native cursor capture should be available on Windows");
    }

    @Test
    void testCaptureAndBlend() {
        int[] meta = new int[6];
        int[] cursorPixels = FastCursor.captureCursor(meta);
        // On headless CI this might be null if no desktop session, but on real Windows session it returns pixels
        if (cursorPixels != null) {
            assertTrue(meta[4] > 0, "Cursor width should be positive");
            assertTrue(meta[5] > 0, "Cursor height should be positive");

            int[] screen = new int[100 * 100];
            assertDoesNotThrow(() -> FastCursor.blendCursor(screen, 100, 100, 0, 0, false));
            assertDoesNotThrow(() -> FastCursor.blendCursor(screen, 100, 100, 0, 0, true));
        }
    }
}
