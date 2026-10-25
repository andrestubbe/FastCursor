# FastCursor 0.1.2 — Ultra-Lightweight Win32 Native Cursor & Pointer Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.2-brightgreen.svg)](https://github.com/andrestubbe/FastCursor/releases/tag/0.1.2)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+%20%7C%20Windows%2011%20(x64)-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.2-green.svg)](https://jitpack.io/#andrestubbe/FastCursor)

---

**🎯 Ultra-fast native Win32 cursor pointer shape management, capture, and alpha blending for Java.**

**FastCursor** delivers dual capabilities for high-performance Java applications:
1. **Pointer Mutation:** Sub-microsecond Win32 `user32.dll` cursor changes (`set`, `setForWindow`, `get`, `show`, `hide`) with zero preview flags.
2. **Cursor Capture & Blending:** Real-time extraction of the active OS mouse cursor (`captureCursor`) and hardware-accelerated alpha blending (`blendCursor`) directly into screen-recording and streaming frame buffers.

---

## Quick Start

```java
import fastcursor.FastCursor;
import fastcursor.CursorType;

public class QuickStart {
    public static void main(String[] args) {
        // 1. Immediately change active thread cursor to WAIT spinner
        FastCursor.set(CursorType.WAIT);

        // 2. Query native Win32 HCURSOR address
        long hCursor = FastCursor.get();
        System.out.println("Active HCURSOR: 0x" + Long.toHexString(hCursor));

        // 3. Restore default arrow
        FastCursor.set(CursorType.ARROW);

        // 4. Alpha-blend cursor into a frame buffer (for streaming/capture)
        int[] framePixels = new int[1920 * 1080];
        FastCursor.blendCursor(framePixels, 1920, 1080, 0, 0, false);
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
  - [3. Querying Active Cursor Handle](#3-querying-active-cursor-handle)
  - [4. Cursor Visibility (Show / Hide)](#4-cursor-visibility-show--hide)
- [Performance Benchmarks](#performance-benchmarks)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
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
- **Missing Direct Handle Access**: Standard APIs do not expose the underlying Win32 `HCURSOR` handle needed for integration with low-level graphic APIs or native window frames.
- **The DLL / JNI Overhead Dilemma**: Traditional native workarounds rely on heavyweight JNA wrappers or custom C++ DLLs that require cumbersome compilation, extraction, and platform-specific deployments.

**FastCursor** circumvents all these limitations through an ultra-fast native Win32 C++ & JNI engine backed by **FastCore**:

| Feature | Standard Java (`java.awt.Cursor`) | Legacy JNA Wrappers | FastCursor (Native JNI) |
|:---|:---|:---|:---|
| **Underlying Mechanism** | AWT Peer abstraction | Dynamic `libffi` wrappers | **Direct Win32 C++ Kernel Subsystem** |
| **Java Preview Flags Required** | No | No | **No (Zero Preview Flags, Java 21+ LTS)** |
| **Direct HCURSOR Query** | ❌ Not exposed | ⚠️ Heavy JNA overhead | **✅ Native `FastCursor.get()`** |
| **Window Class Binding** | ❌ Event-polling only | ⚠️ Complex bridge | **✅ Native `SetClassLongPtrW` (HWND)** |
| **Live Cursor Screen Capture** | ❌ None | ❌ None | **✅ Native `captureCursor()` (ARGB)** |
| **Frame Alpha Blending** | ❌ Manual CPU loops | ❌ None | **✅ Native `blendCursor()` (In-Place)** |
| **Invocation Latency** | ~2–5 ms (EDT dispatch) | ~50–100 µs (JNA boundary) | **< 1 µs (Direct JNI Dispatch)** |
| **Preloaded Handle Cache** | No | Manual implementation | **✅ Built-in Concurrent Cache** |

---

## Key Features

- ⚡ **Ultra-Fast Native Win32 C++ & JNI Engine**: Zero Java Preview Flags, Zero JNA Overhead. Built for seamless integration across all Java 21+ standard runtimes.
- 🎯 **Thread & Window Scoped Cursors**: Instantly switch the active thread cursor or register class-level cursor shapes (`SetClassLongPtrW`) per `HWND` so Windows applies them natively on hover.
- 🎥 **Live Cursor Capture & Blending**: High-speed cursor capture with hot-spot geometry and direct alpha compositing into video or framebuffer arrays via `blendCursor(...)`.
- 🔍 **Native Handle Inspection**: Retrieve the calling thread's active `HCURSOR` address directly via `FastCursor.get()`.
- 🗲 **Sub-Microsecond Latency**: Built-in concurrent handle caching avoids redundant `LoadCursorW` Win32 calls.
- 🛡️ **Fail-Safe Fallbacks**: Safe native lifecycle with zero JVM crashes, memory leaks, or GC allocations.

---

## Real-World Use Cases

- 🎮 **Desktop Streaming & Capture ([FastVideoStream](https://github.com/andrestubbe/FastVideoStream))**: Real-time cursor alpha blending into video pipelines via `FastCursor.blendCursor(...)` without frame jitter.
- 🎨 **Canvas, Diagramming & CAD ([FastGUI](https://github.com/andrestubbe/FastGUI))**: Native resize pointers (`IDC_SIZEWE`, `IDC_SIZENS`, `IDC_SIZENWSE`, `IDC_SIZEALL`) that feel 100% native and responsive even during heavy background rendering.
- 🪟 **Window Hover Feedback ([FastWindow](https://github.com/andrestubbe/FastWindow))**: Bind custom cursors per window handle (`setForWindow`) for effortless interaction cues.
- ⏳ **Background Task Indication**: Immediate system spinner (`IDC_WAIT` / `IDC_APPSTARTING`) triggered on the calling thread without EDT queue contention.

---

## Architecture & Design

```
┌─────────────────────────────────────────────────────────────┐
│                       Your Application                      │
│            (Editors, Snipping Tools, Canvas, UI)            │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    fastcursor.FastCursor                    │
│           (Facade: set, setForWindow, get, show, hide)       │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                fastcursor.NativeUser32Cursor                │
│             (Direct JNI Native Dispatch + Cache)            │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
        Direct Native JNI               Concurrent Cache
               │                         (HCURSOR handles)
               ▼                               ▼
┌─────────────────────────────────────────────────────────────┐
│            fastcursor.dll (Native C++ Subsystem)            │
│   • SetCursor              • LoadCursorW                    │
│   • SetClassLongPtrW       • GetCursor                      │
│   • ShowCursor             • Capture & Alpha Blending       │
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

### 3. Querying Active Cursor Handle

```java
import fastcursor.FastCursor;

// Get the native HCURSOR handle of the active thread (long address)
long hCursor = FastCursor.get();
System.out.println("Native HCURSOR address: 0x" + Long.toHexString(hCursor));
```

### 4. Cursor Visibility (Show / Hide)

```java
import fastcursor.FastCursor;

// Hide mouse pointer
FastCursor.hide();

// Restore visibility
FastCursor.show();
```

---

## Performance Benchmarks

FastCursor is rigorously benchmarked via **OpenJDK JMH** to guarantee zero JVM allocation and sub-microsecond Win32 dispatch latencies:

| Benchmark / Operation | Throughput (ops/ms) | Ops per Second | Latency |
|:---|:---:|:---:|:---:|
| **`benchmarkGetCursorHandle`** (Direct Win32 `GetCursor`) | **~2,704 ops/ms** | **> 2.70 Million** | **< 370 ns** |
| **`benchmarkSetCursorCached`** (IDC handle resolution + `SetCursor`) | **~1,642 ops/ms** | **> 1.64 Million** | **< 610 ns** |
| **`benchmarkSetCursorSwitch`** (Active shape alternation) | **~1,473 ops/ms** | **> 1.47 Million** | **< 680 ns** |

*Measured on Windows 11 (x64), JDK 21.0.12.1. Native C++ JNI dispatch with 0 bytes GC heap allocation and sub-microsecond latency.*

---

## Technical Demos & Benchmarks

| Case | Java Example | Launcher | Description |
|---|---|---|---|
| **Interactive Terminal Demo** | [Demo.java](examples/Demo/src/main/java/fastcursor/Demo.java) | `run-demo.bat` | Cycles through all Win32 system cursor shapes every 3 seconds directly over the console window. |
| **JMH Microbenchmark Suite** | [Benchmark.java](examples/Benchmark/src/main/java/fastcursor/benchmark/Benchmark.java) | `run-benchmark.bat` | JMH microbenchmark suite measuring Win32 downcall throughput and handle cache performance. |

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
        <version>0.1.2</version>
    </dependency>
    <!-- Required Native Platform Infrastructure -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastCursor:0.1.2'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest compiled JARs directly to add them to your classpath:

1. 🎯 **[FastCursor-0.1.2.jar](https://github.com/andrestubbe/FastCursor/releases/download/0.1.2/FastCursor-0.1.2.jar)** (The Core Cursor Engine)
2. ⚙️ **[FastCore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/FastCore-0.1.0.jar)** (The Native Platform Loader)

> [!NOTE]
> FastCursor 0.1.2+ uses standard JNI (fastcursor.dll loaded automatically via FastCore). No preview flags or JVM arguments are required.

---

## Documentation

- **[CHANGELOG.md](docs/CHANGELOG.md)**: Detailed version notes and update history.
- **[REFERENCE.md](docs/REFERENCE.md)**: Complete API manual and method signatures.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The engineering rationale for zero-DLL FFM performance.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and planned features (`.ani`/`.cur` custom cursors).
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
- **[FastRobot](https://github.com/andrestubbe/FastRobot)** — Low-latency native input automation and simulation.

---

## License

This project is licensed under the [MIT License](LICENSE).
Part of the **FastJava Ecosystem**.
