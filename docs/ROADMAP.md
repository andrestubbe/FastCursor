# FastCursor Roadmap

Entwicklungsplan und Meilensteine für **FastCursor**:

| Version | Status | Geplante Features & Meilensteine |
|:---|:---|:---|
| **`v0.1.0`** | **Abgeschlossen** | • Win32 FFM Direct Bindings (`user32.dll`) via Java 21+<br>• Standard System Cursors (`IDC_*`)<br>• `FastCursor.set`, `setForWindow(hwnd)`, `clip`, `unclip`, `show`, `hide`<br>• Preloaded Handle Caching<br>• Interaktive Cycle Demo |
| **`v0.2.0`** | Geplant | • Dynamisches Laden benutzerdefinierter Cursor (`.cur` / `.ani` animierte Cursor)<br>• Hotspot-Offset-Definition (`xHotspot`, `yHotspot`)<br>• Abfrage der aktuellen Cursor-Position (`GetCursorPos`) mit Sub-Mikrosekunden-Latenz |
| **`v0.3.0`** | Geplant | • Window Client-Area Auto-Clipping (`clipToWindow(hwnd)`) mit automatischer DPI-Skalierung<br>• Multi-Monitor Monitor-Bounding-Box Clipping |
| **`v0.4.0`** | Geplant | • Cursor-Icon Capture & Bitmap-Extraktion (`GetCursorInfo` / `GetIconInfo`) zur Bildanalyse und KI-Vision-Pipelines |
| **`v1.0.0`** | Geplant | • Zero-Allocation API Stabilisierung, Multi-OS Abstraktionslayer für Linux X11/Wayland |
