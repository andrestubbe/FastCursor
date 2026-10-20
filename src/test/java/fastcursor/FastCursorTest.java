package fastcursor;

import org.junit.jupiter.api.Test;
import java.lang.foreign.MemorySegment;
import static org.junit.jupiter.api.Assertions.*;

class FastCursorTest {

    @Test
    void testCursorTypeValues() {
        assertNotNull(CursorType.ARROW);
        assertEquals(32512, CursorType.ARROW.getIdcValue());
        assertEquals(32513, CursorType.IBEAM.getIdcValue());
        assertEquals(32514, CursorType.WAIT.getIdcValue());
        assertEquals(32515, CursorType.CROSS.getIdcValue());
        assertEquals(32649, CursorType.HAND.getIdcValue());
    }

    @Test
    void testSetNullCursor() {
        assertFalse(FastCursor.set(null));
        assertFalse(FastCursor.setForWindow(0, null));
        assertFalse(FastCursor.setForWindow(12345, null));
    }

    @Test
    void testStandardCursorOperations() {
        // Should execute smoothly on Windows user32
        boolean setArrow = FastCursor.set(CursorType.ARROW);
        assertTrue(setArrow, "Setting standard arrow cursor should succeed on Windows");

        // Query active cursor handle via FastCursor.get()
        MemorySegment cursorHandle = FastCursor.get();
        assertNotNull(cursorHandle, "Cursor handle should not be null");

        // Show and hide counter calls
        assertDoesNotThrow(FastCursor::show);
        assertDoesNotThrow(FastCursor::hide);
        assertDoesNotThrow(FastCursor::show); // restore visibility
    }
}
