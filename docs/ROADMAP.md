# FastCursor Roadmap

Entwicklungsplan und Meilensteine für **FastCursor**:

| Version | Status | Geplante Features & Meilensteine |
|:---|:---|:---|
| **`v0.1.0`** | **Abgeschlossen** | • Win32 FFM Direct Bindings (`user32.dll`) via Java 21+<br>• Standard System Cursors (`IDC_*`)<br>• `FastCursor.set`, `setForWindow(hwnd)`, `get()`, `show()`, `hide()`<br>• Preloaded Handle Caching<br>• Interaktive Demo & JMH Benchmark Suite |
| **`v0.2.0`** | Geplant | • Dynamisches Laden benutzerdefinierter Cursor (`.cur` / `.ani` animierte Cursor)<br>• Hotspot-Offset-Definition (`xHotspot`, `yHotspot`)<br>• Cursor-Form-Erkennung des aktiven Fensters |
| **`v0.3.0`** | Geplant | • Cursor-Icon Capture & Bitmap-Extraktion (`GetCursorInfo` / `GetIconInfo`) zur Bildanalyse und KI-Vision-Pipelines |
| **`v1.0.0`** | Geplant | • Zero-Allocation API Stabilisierung, Multi-OS Abstraktionslayer für Linux X11/Wayland & macOS |
