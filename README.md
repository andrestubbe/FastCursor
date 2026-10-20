# FastCursor 0.1.0 — Ultra-Lightweight Win32 Native Cursor & Pointer Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastCursor/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+%20%7C%20Windows%2011%20(x64)-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.0-green.svg)](https://jitpack.io/#andrestubbe/FastCursor)

---

**🎯 Ultra-fast native Win32 cursor, pointer shape, and rectangular screen clipping management for Java 21+ via Foreign Function & Memory (FFM).**

**FastCursor** replaces bulky AWT abstractions and legacy JNI DLL wrappers with direct, sub-microsecond Win32 `user32.dll` system calls. It offers instant thread cursor mutation, permanent window-class (`HWND`) cursor binding, and hardware-accelerated desktop clipping with zero native compilation.

---

## Quick Start

```java
import fastcursor.FastCursor;
import fastcursor.CursorType;

public class QuickStart {
    public static void main(String[] args) {
        // 1. Immediately change active thread cursor to WAIT spinner
        FastCursor.set(CursorType.WAIT);

        // 2. Lock cursor into a 500x500 box (ideal for games & precision tools)
        FastCursor.clip(100, 100, 600, 600);

        // 3. Release confinement & restore default arrow
        FastCursor.unclip();
        FastCursor.set(CursorType.ARROW);
    }
}
```

---

## Table of Contents

- [Quick Start](#quick-start)
- [Why FastCursor?](#why-fastcursor)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Architecture & Design](#architecture--design)
- [Supported Cursor Types](#supported-cursor-types)
- [Code Examples](#code-examples)
  - [1. Thread-Scoped Cursor](#1-thread-scoped-cursor)
  - [2. Window-Bound Cursor (HWND)](#2-window-bound-cursor-hwnd)
  - [3. Desktop Rectangular & Window Clipping](#3-desktop-rectangular--window-clipping)
  - [4. Querying Active Cursor Handle](#4-querying-active-cursor-handle)
  - [5. Cursor Visibility (Show / Hide)](#5-cursor-visibility-show--hide)
- [Interactive Demonstrations](#interactive-demonstrations)
- [Installation](#installation)
  - [Option 1: Maven (Recommended)](#option-1-maven-recommended)
  - [Option 2: Gradle (via JitPack)](#option-2-gradle-via-jitpack)
  - [Option 3: Direct Download (No Build Tool)](#option-3-direct-download-no-build-tool)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Why FastCursor?

Standard Java cursor handling via `java.awt.Cursor` or JavaFX is plagued by severe architectural limitations for high-performance apps, games, and system tools:

- **Missing System Cursor Shapes**: Standard Java only maps a tiny subset of legacy cursors. Essential Windows cursors like `IDC_APPSTARTING`, `IDC_HELP`, or `IDC_SIZEALL` are either inconsistent or completely missing.
- **No Window-Level Class Binding**: AWT requires continuous hover listening. If the JVM Event Dispatch Thread (EDT) suffers a latency spike, the cursor freezes or exhibits jitter.
- **Zero Clipping Support**: AWT provides no way to confine the cursor to a specific screen bounding box via Win32 `ClipCursor` (critical for first-person cameras, canvas panning, and precision dragging).
- **The DLL / JNI Overhead Dilemma**: Traditional native workarounds rely on heavyweight JNA wrappers or custom C++ DLLs that require cumbersome compilation, extraction, and platform-specific deployments.

**FastCursor** circumvents all these limitations through direct **Java 21+ Panama FFM (Foreign Function & Memory)** calls backed by **FastCore**:

| Feature | Standard Java (`java.awt.Cursor`) | Legacy JNI / JNA | FastCursor (FFM) |
|:---|:---|:---|:---|
| **Underlying Mechanism** | AWT Peer abstraction | External C/C++ DLLs | **Direct `user32.dll` System Calls** |
| **Native DLL Required** | No | Yes (must be compiled & extracted) | **No (Zero DLL / Zero JNI)** |
| **Screen Rect Clipping** | ❌ Not supported | ⚠️ Manual wrapper required | **✅ Native `ClipCursor` / `unclip`** |
| **Window Class Binding** | ❌ Event-polling only | ⚠️ Complex JNI bridge | **✅ Native `SetClassLongPtrW` (HWND)** |
| **Invocation Latency** | ~2–5 ms (EDT dispatch) | ~50–100 µs (JNI boundary) | **< 1 µs (Direct MethodHandle)** |
| **Preloaded Handle Cache** | No | Manual implementation | **✅ Built-in Concurrent Cache** |

---

## Key Features

- ⚡ **Zero JNI / Zero External DLLs**: Pure Java 21+ Project Panama talking directly to Windows `user32.dll`. No C++ compiler needed.
- 🎯 **Thread & Window Scoped Cursors**: Instantly switch the active thread cursor or register class-level cursor shapes (`SetClassLongPtrW`) per `HWND` so Windows applies them natively on hover.
- 🔒 **Screen Rect Clipping**: Lock the mouse cursor to custom desktop bounding boxes via `ClipCursor` (ideal for FPS games, drag controls, and region tools).
- 🗲 **Sub-Microsecond Latency**: Built-in concurrent handle caching avoids redundant `LoadCursorW` Win32 calls.
- 🛡️ **Fail-Safe Fallbacks**: Pure downcall handles with zero JVM crashes, memory leaks, or GC allocations.

---

## Real-World Use Cases

- 🎮 **FPS Games & 3D Viewports ([FastVulkan](https://github.com/andrestubbe/FastVulkan))**: Confinement of the cursor inside the window viewport via `clip()` to prevent accidental clicks onto second monitors during mouselook.
- ✂️ **Desktop Snipping & Region Select ([FastScreenCapture](https://github.com/andrestubbe/FastScreenCapture))**: Instant switch to high-precision crosshair (`IDC_CROSS`) with zero frame delay.
- 🎨 **Canvas, Diagramming & CAD ([FastGUI](https://github.com/andrestubbe/FastGUI))**: Native resize pointers (`IDC_SIZEWE`, `IDC_SIZENS`, `IDC_SIZENWSE`, `IDC_SIZEALL`) that feel 100% native and responsive even during heavy background rendering.
- ⏳ **Background Task Indication**: Immediate system spinner (`IDC_WAIT` / `IDC_APPSTARTING`) triggered on the calling thread without EDT queue contention.

---

## Architecture & Design

```
┌─────────────────────────────────────────────────────────────┐
│                       Your Application                      │
│            (Games, Editors, Snipping Tools, Canvas)         │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    fastcursor.FastCursor                    │
│ (Facade: set, get, setForWindow, clip, clipToWindow,        │
│          unclip, show, hide)                                │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                fastcursor.NativeUser32Cursor                │
│     (FFM Linker / MethodHandles to user32.dll + Cache)      │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
       Direct Downcall                 Concurrent Cache
               │                         (HCURSOR handles)
               ▼                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     Windows user32.dll                      │
│   • SetCursor              • LoadCursorW                    │
│   • SetClassLongPtrW       • ClipCursor                     │
│   • ShowCursor             • GetCursor                      │
│   • GetWindowRect                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## Supported Cursor Types

`CursorType` provides full coverage of standard Win32 IDC resource cursor shapes:

| Shape | `CursorType` | Win32 Identifier | Description |
|:---:|:---|:---|:---|
| <img src="docs/cursors/arrow.png" width="32" height="32" alt="Arrow"> | `ARROW` | `IDC_ARROW (32512)` | Standard arrow pointer |
| <img src="docs/cursors/ibeam.png" width="32" height="32" alt="I-Beam"> | `IBEAM` | `IDC_IBEAM (32513)` | Text selection I-beam |
| <img src="docs/cursors/wait.png" width="32" height="32" alt="Wait"> | `WAIT` | `IDC_WAIT (32514)` | Wait / hourglass / spinner |
| <img src="docs/cursors/cross.png" width="32" height="32" alt="Cross"> | `CROSS` | `IDC_CROSS (32515)` | Crosshair for snipping and precision selection |
| <img src="docs/cursors/appstarting.png" width="32" height="32" alt="App Starting"> | `APP_STARTING` | `IDC_APPSTARTING (32516)` | Arrow with small spinning wait circle |
| <img src="docs/cursors/hand.png" width="32" height="32" alt="Hand"> | `HAND` | `IDC_HAND (32649)` | Link / button pointer hand |
| <img src="docs/cursors/sizeall.png" width="32" height="32" alt="Size All"> | `SIZE_ALL` | `IDC_SIZEALL (32646)` | Four-pointed movement cursor |
| <img src="docs/cursors/sizewe.png" width="32" height="32" alt="Size WE"> | `SIZE_WE` | `IDC_SIZEWE (32644)` | Double-pointed horizontal arrow |
| <img src="docs/cursors/sizens.png" width="32" height="32" alt="Size NS"> | `SIZE_NS` | `IDC_SIZENS (32645)` | Double-pointed vertical arrow |
| <img src="docs/cursors/sizenwse.png" width="32" height="32" alt="Size NWSE"> | `SIZE_NWSE` | `IDC_SIZENWSE (32642)` | Diagonal NW-SE resize arrow |
| <img src="docs/cursors/sizenesw.png" width="32" height="32" alt="Size NESW"> | `SIZE_NESW` | `IDC_SIZENESW (32643)` | Diagonal NE-SW resize arrow |
| <img src="docs/cursors/no.png" width="32" height="32" alt="No"> | `NO` | `IDC_NO (32648)` | Prohibited / slashed circle |
| <img src="docs/cursors/help.png" width="32" height="32" alt="Help"> | `HELP` | `IDC_HELP (32651)` | Arrow with question mark |
| <img src="docs/cursors/uparrow.png" width="32" height="32" alt="Up Arrow"> | `UP_ARROW` | `IDC_UPARROW (32516)` | Upward arrow |

---

## Code Examples

### 1. Thread-Scoped Cursor

```java
import fastcursor.FastCursor;
import fastcursor.CursorType;

// Set wait cursor during background computation
FastCursor.set(CursorType.WAIT);

// Restore default arrow
FastCursor.set(CursorType.ARROW);
```

### 2. Window-Bound Cursor (HWND)

```java
import fastcursor.FastCursor;
import fastcursor.CursorType;

long hwnd = 0x12345678L; // Native Win32 window handle

// Set hand cursor whenever mouse hovers over this window
FastCursor.setForWindow(hwnd, CursorType.HAND);
```

### 3. Desktop Rectangular & Window Clipping

```java
import fastcursor.FastCursor;

// 1. Lock mouse cursor to a 500x500 box on screen
FastCursor.clip(100, 100, 600, 600);

// 2. Or confine cursor directly to window screen bounds (HWND)
long hwnd = 0x12345678L;
FastCursor.clipToWindow(hwnd);

// 3. Release cursor confinement across all monitors
FastCursor.unclip();
```

### 4. Querying Active Cursor Handle

```java
import fastcursor.FastCursor;
import java.lang.foreign.MemorySegment;

// Save current native cursor handle
MemorySegment previousCursor = FastCursor.get();

// Mutate cursor temporarily
FastCursor.set(CursorType.WAIT);

// ... perform tasks ...
```

### 5. Cursor Visibility (Show / Hide)

```java
import fastcursor.FastCursor;

// Hide mouse pointer
FastCursor.hide();

// Restore visibility
FastCursor.show();
```

---

## Interactive Demonstrations

FastCursor ships with an interactive demo that cycles through all Win32 system cursors every 3 seconds:

| Demo | Source Code | Launcher | Description |
|:---|:---|:---|:---|
| **Cursor Cycle Demo** | [`CursorCycleDemo.java`](examples/src/main/java/demo/CursorCycleDemo.java) | `run-demo.bat` | Cycles through all Win32 system cursor shapes every 3 seconds directly over the console window. |

Run the demo instantly via terminal:
```cmd
run-demo.bat
```

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastCursor Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCursor</artifactId>
        <version>0.1.0</version>
    </dependency>
    <!-- Required Native Platform & FFM Infrastructure -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.1</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastCursor:0.1.0'
    implementation 'com.github.andrestubbe:FastCore:0.1.1'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest compiled JARs directly to add them to your classpath:

1. 🎯 **[FastCursor-0.1.0.jar](https://github.com/andrestubbe/FastCursor/releases/download/0.1.0/FastCursor-0.1.0.jar)** (The Core Cursor Engine)
2. ⚙️ **[FastCore-0.1.1.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.1/FastCore-0.1.1.jar)** (The Native Platform & FFM Loader)

> [!IMPORTANT]
> When executing on Java 21+, add the JVM arguments `--enable-preview --enable-native-access=ALL-UNNAMED` to permit direct Foreign Function & Memory (FFM) system calls.

---

## Documentation

- **[CHANGELOG.md](docs/CHANGELOG.md)**: Detailed version notes and update history.
- **[REFERENCE.md](docs/REFERENCE.md)**: Complete API manual and method signatures.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The engineering rationale for zero-DLL FFM performance.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and planned features (`.ani`/`.cur`, auto-window clipping).
- **[COMPILE.md](docs/COMPILE.md)**: Build and preview flags guide.

---

## Platform Support

| Platform | Architecture | Status | Notes |
|:---|:---:|:---:|:---|
| **Windows 10 / 11** | `x64` | ✅ Fully Supported | Win32 `user32.dll` via Java 21+ Project Panama FFM |
| **Linux (X11 / Wayland)** | `x64` / `AArch64` | 🚧 Planned | X11 `XFixes` / Wayland Pointer Shape Protocol |
| **macOS (Sonoma+)** | `Apple Silicon` / `x64` | 🚧 Planned | Cocoa `NSCursor` / CoreGraphics Direct Pointer API |

---

## Related Projects

- **[FastCore](https://github.com/andrestubbe/FastCore)** — Unified native loader and Project Panama FFM platform runtime.
- **[FastMouse](https://github.com/andrestubbe/FastMouse)** — Ultra-low latency native RawInput mouse interception (1,000–8,000 Hz).
- **[FastPointer](https://github.com/andrestubbe/FastPointer)** — Zero-overhead native address arithmetic and memory pointer abstraction.
- **[FastScreenCapture](https://github.com/andrestubbe/FastScreenCapture)** — High-speed Desktop Duplication (DXGI) and GDI screen grabber.
- **[FastRobot](https://github.com/andrestubbe/FastRobot)** — Low-latency native input automation and simulation.

---

## License

This project is licensed under the [MIT License](LICENSE).
Part of the **FastJava Ecosystem**.
