# FastCursor 0.1.0 — Ultra-Lightweight Win32 Native Cursor & Pointer Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastCursor/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+%20%7C%20Windows%2011%20(x64)-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.0-green.svg)](https://jitpack.io/#andrestubbe/FastCursor)

**Ultra-fast native Win32 cursor, pointer shape, and rectangular screen clipping management for Java 21+ via Foreign Function & Memory (FFM).**

Zero external C/C++ DLLs required, zero JNI compilation, and instant sub-microsecond execution directly via Windows `user32.dll`.

---

## Table of Contents

- [Key Features](#key-features)
- [Architecture & Design](#architecture--design)
- [Supported Cursor Types](#supported-cursor-types)
- [Quick Start & Code Examples](#quick-start--code-examples)
  - [1. Change Thread Cursor](#1-change-thread-cursor)
  - [2. Assign Cursor to a Window (HWND)](#2-assign-cursor-to-a-window-hwnd)
  - [3. Confine Cursor to a Screen Region (Clipping)](#3-confine-cursor-to-a-screen-region-clipping)
  - [4. Hide / Show Cursor](#4-hide--show-cursor)
- [Installation](#installation)
  - [Maven](#maven)
  - [Gradle](#gradle)
  - [Direct Download](#direct-download)
- [Documentation](#documentation)
- [License](#license)

---

## Key Features

- ⚡ **Zero JNI / Zero DLLs**: Backed entirely by Java 21+ Project Panama (Foreign Function & Memory API) talking directly to `user32.dll`.
- 🎯 **Thread & Window Scoped Cursors**: Set the immediate thread cursor or register class-level cursor shapes (`SetClassLongPtrW`) per `HWND` so Windows automatically applies them on hover.
- 🔒 **Screen Rect Clipping**: Lock the cursor to custom screen bounding boxes via `ClipCursor` (ideal for games, pan/drag tools, drawing apps, and precision controls).
- 🗲 **Sub-Microsecond Latency**: Built-in concurrent handle caching avoids redundant `LoadCursorW` Win32 calls.
- 🛡️ **Fail-Safe Fallbacks**: Graceful error handling with zero JVM crashes or leaks.

---

## Architecture & Design

```
+-------------------------------------------------------------+
|                       Your Application                      |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                    fastcursor.FastCursor                    |
|      (Facade: set, setForWindow, clip, unclip, show, hide)  |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                fastcursor.NativeUser32Cursor                |
|     (FFM Linker / MethodHandles to user32.dll + Cache)      |
+-------------------------------------------------------------+
                               | Direct System Call
                               v
+-------------------------------------------------------------+
|                     Windows user32.dll                      |
|       (SetCursor, LoadCursorW, SetClassLongPtrW, ClipCursor) |
+-------------------------------------------------------------+
```

---

## Supported Cursor Types

`CursorType` provides all standard Win32 IDC resource cursor shapes:

| `CursorType` | Win32 Identifier | Description |
|:---|:---|:---|
| `ARROW` | `IDC_ARROW (32512)` | Standard arrow pointer |
| `IBEAM` | `IDC_IBEAM (32513)` | Text selection I-beam |
| `WAIT` | `IDC_WAIT (32514)` | Wait / hourglass / spinner |
| `CROSS` | `IDC_CROSS (32515)` | Crosshair for snipping and precision selection |
| `APP_STARTING` | `IDC_APPSTARTING (32516)` | Arrow with small spinning wait circle |
| `HAND` | `IDC_HAND (32649)` | Link / button pointer hand |
| `SIZE_ALL` | `IDC_SIZEALL (32646)` | Four-pointed movement cursor |
| `SIZE_WE` | `IDC_SIZEWE (32644)` | Horizontal resize arrow |
| `SIZE_NS` | `IDC_SIZENS (32645)` | Vertical resize arrow |
| `SIZE_NWSE` | `IDC_SIZENWSE (32642)` | Diagonal NW-SE resize arrow |
| `SIZE_NESW` | `IDC_SIZENESW (32643)` | Diagonal NE-SW resize arrow |
| `NO` | `IDC_NO (32648)` | Prohibited / slashed circle |
| `HELP` | `IDC_HELP (32651)` | Arrow with question mark |
| `UP_ARROW` | `IDC_UPARROW (32516)` | Upward arrow |

---

## Quick Start & Code Examples

### 1. Change Thread Cursor

```java
import fastcursor.FastCursor;
import fastcursor.CursorType;

// Set wait cursor during background task
FastCursor.set(CursorType.WAIT);

// Restore arrow cursor
FastCursor.set(CursorType.ARROW);
```

### 2. Assign Cursor to a Window (HWND)

```java
import fastcursor.FastCursor;
import fastcursor.CursorType;

long hwnd = 0x12345678L; // Your native Win32 window handle

// Set hand cursor whenever mouse hovers over this window
FastCursor.setForWindow(hwnd, CursorType.HAND);
```

### 3. Confine Cursor to a Screen Region (Clipping)

```java
import fastcursor.FastCursor;

// Lock mouse cursor to a 500x500 box on screen
FastCursor.clip(100, 100, 600, 600);

// Release cursor confinement
FastCursor.unclip();
```

### 4. Hide / Show Cursor

```java
import fastcursor.FastCursor;

// Hide mouse pointer
FastCursor.hide();

// Show mouse pointer
FastCursor.show();
```

---

## Installation

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>FastCursor</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastCursor:0.1.0'
}
```

### Direct Download

Download the compiled JAR directly from GitHub Releases:
- [FastCursor-0.1.0.jar](https://github.com/andrestubbe/FastCursor/releases/download/0.1.0/FastCursor-0.1.0.jar)

---

## Documentation

- [Changelog](docs/CHANGELOG.md) — Detailed version notes and update history.
- [API Reference](docs/REFERENCE.md) — Full API manual and method signatures.

---

## License

This project is licensed under the [MIT License](LICENSE).
