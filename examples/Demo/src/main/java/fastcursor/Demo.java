package fastcursor;

import fastcursor.CursorType;
import fastcursor.FastCursor;

public class Demo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==================================================");
        System.out.println("  FastCursor Demo - Cycling System Cursor Shapes  ");
        System.out.println("==================================================");
        System.out.println("Every 3 seconds, a different Win32 system cursor");
        System.out.println("will be applied to your active thread/pointer.");
        System.out.println("Move your mouse over this console window to see!");
        System.out.println("--------------------------------------------------\n");

        CursorType[] demoSequence = {
            CursorType.WAIT,
            CursorType.CROSS,
            CursorType.IBEAM,
            CursorType.HAND,
            CursorType.SIZE_ALL,
            CursorType.SIZE_WE,
            CursorType.SIZE_NS,
            CursorType.NO,
            CursorType.HELP,
            CursorType.ARROW
        };

        for (int i = 0; i < demoSequence.length; i++) {
            CursorType type = demoSequence[i];
            System.out.printf("[%d/%d] Setting cursor: %-12s (IDC: %d)...%n", 
                (i + 1), demoSequence.length, type.name(), type.getIdcValue());

            FastCursor.set(type);

            // Wait 3 seconds
            Thread.sleep(3000);
        }

        // Final restore
        FastCursor.set(CursorType.ARROW);
        System.out.println("\nDemo completed! Restored standard ARROW cursor.");
    }
}
