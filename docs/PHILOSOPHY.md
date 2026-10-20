# FastCursor Philosophie

## 1. Native-First & Das Dilemma von Java AWT/Swing Cursorn

Traditionell abstrahiert die Java Virtual Machine Betriebssystem-Eigenschaften hinter Klassen wie `java.awt.Cursor` oder `java.awt.Toolkit`. Was für herkömmliche Formularanwendungen ausreicht, scheitert in modernen Hochleistungs-Workloads, Spielen und interaktiven Systemtools fundamental:

- **Eingeschränkter Shape-Katalog**: `java.awt.Cursor` deckt nur eine veraltete Untermenge an System-Zeigern ab. Spezialisierte System-Cursor (wie `IDC_APPSTARTING`, `IDC_HELP`, `IDC_SIZEALL`, etc.) sind plattformübergreifend unzuverlässig oder gar nicht abrufbar.
- **Fehlende Window-Level Bindung (`HWND`)**: In AWT/Swing muss der Cursor über Frame- und Component-Events manuell überwacht werden. Verlässt der Cursor das Fenster oder gerät die Event-Queue (EDT) ins Stocken, friert der Cursor ein oder springt fehlerhaft zurück.
- **Kein Clipping-Support**: Die Win32-API bietet mit `ClipCursor` ein mächtiges primitives Werkzeug, um den Mauszeiger in exakten Bildschirm-Rechtecken einzusperren (essenziell für FPS-Kamerasteuerungen, Canvas-Drag-Operationen und Zeichenwerkzeuge). Java bot hierfür ab Werk schlichtweg keine native Möglichkeit.
- **DLL- und JNI-Altlasten**: Bisherige Lösungen für Windows-APIs setzten auf schwergewichtige JNA-Wrappern oder handkompilierte C/C++ JNI-DLLs, die mitgeliefert, entpackt und gepflegt werden mussten.

**FastCursor** löst dieses Dilemma durch die Nutzung der modernen **Foreign Function & Memory (FFM) API** (Java 21+): Direkter, nativer Aufruf von `user32.dll` ohne eine einzige Zeile C/C++-Code und ohne externe DLL-Abhängigkeiten.

---

## 2. Die Kernsäulen von FastCursor

### I. Pure Java 21+ Project Panama (Zero-DLL / Zero-JNI)
FastCursor benötigt keine native `.dll` auf der Festplatte. Über `Linker.nativeLinker()` und `SymbolLookup.libraryLookup("user32.dll")` bindet FastCursor Win32-Systemfunktionen direkt als `MethodHandle` zur Laufzeit ein. Die JVM generiert daraus Maschinencode, der direkt auf Assembler-Ebene die Windows-Systemaufrufe ausführt.

### II. Sub-Mikrosekunden-Latenz & Handle-Caching
Das Laden von Win32-System-Ressourcen (`LoadCursorW`) erzeugt bei jedem Aufruf Betriebssystem-Overhead. FastCursor kapselt alle Standard-Cursor (`IDC_*`) in einer internen, threadsicheren Lookup-Tabelle (`ConcurrentHashMap`). Ein Systemcursor wird maximal einmal gelöst und danach im Sub-Mikrosekunden-Bereich direkt an den Thread oder das Zielfenster übergeben.

### III. Dauerhafte Fenster-Bindung (`SetClassLongPtrW`)
Statt Cursor-Hover-Events mühsam in Java-Event-Schleifen abzufragen, überschreibt FastCursor auf Wunsch das Klassenattribut des nativen Win32-Fenster-Handles (`GCLP_HCURSOR`). Windows selbst übernimmt die Cursor-Darstellung direkt im DWM/Kernel-Subsystem, sobald die Maus die Client-Area des Zielfensters betritt – selbst wenn der Java-Prozess unter Volllast steht.

### IV. Hardwarenahe Bewegungsbegrenzung (`ClipCursor`)
Mit `FastCursor.clip(left, top, right, bottom)` lässt sich der physische Cursor instantan auf einen Desktop-Bereich eingrenzen. Das verhindert ungewolltes Ausbrechen auf Multi-Monitor-Setups bei Vollbild-Tools oder Drag-Interaktionen und stellt die volle Kontrolle über den Zeiger sicher.
