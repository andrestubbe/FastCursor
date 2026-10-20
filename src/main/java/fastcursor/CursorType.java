package fastcursor;

/**
 * Standard Win32 cursor shapes and system resources (IDC_*).
 */
public enum CursorType {
    /** Standard arrow cursor (IDC_ARROW = 32512). */
    ARROW(32512),

    /** Text I-beam cursor for editable text (IDC_IBEAM = 32513). */
    IBEAM(32513),

    /** Hourglass / spinner wait cursor (IDC_WAIT = 32514). */
    WAIT(32514),

    /** Crosshair cursor for snipping, selection and precise positioning (IDC_CROSS = 32515). */
    CROSS(32515),

    /** Standard arrow and small spinning wait circle (IDC_APPSTARTING = 32516). */
    APP_STARTING(32516),

    /** Hand / pointer cursor for links and interactive buttons (IDC_HAND = 32649). */
    HAND(32649),

    /** Four-pointed arrow sizing cursor for moving elements (IDC_SIZEALL = 32646). */
    SIZE_ALL(32646),

    /** Double-pointed horizontal arrow for resizing (IDC_SIZEWE = 32644). */
    SIZE_WE(32644),

    /** Double-pointed vertical arrow for resizing (IDC_SIZENS = 32645). */
    SIZE_NS(32645),

    /** Double-pointed diagonal NW-SE arrow for resizing (IDC_SIZENWSE = 32642). */
    SIZE_NWSE(32642),

    /** Double-pointed diagonal NESW arrow for resizing (IDC_SIZENESW = 32643). */
    SIZE_NESW(32643),

    /** Circle with a diagonal slash indicating action not permitted (IDC_NO = 32648). */
    NO(32648),

    /** Help cursor with arrow and question mark (IDC_HELP = 32651). */
    HELP(32651),

    /** Vertical bar / split resize cursor (IDC_UPARROW = 32516). */
    UP_ARROW(32516);

    private final int idcValue;

    CursorType(int idcValue) {
        this.idcValue = idcValue;
    }

    public int getIdcValue() {
        return idcValue;
    }
}
