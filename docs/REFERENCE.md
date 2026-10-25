# FastCursor API Reference Manual

FastCursor provides high-performance native Win32 cursor and pointer management for Java 21+ via native C++ JNI with zero preview flags and sub-microsecond latency.

---

## Table of Contents

- [1. Core Facade API (`FastCursor`)](#1-core-facade-api-fastcursor)
- [2. System Cursor Types (`CursorType`)](#2-system-cursor-types-cursortype)
- [3. Low-Level FFM Bindings (`NativeUser32Cursor`)](#3-low-level-ffm-bindings-nativeuser32cursor)

---

## 1. Core Facade API (`FastCursor`)

### `set`
```java
public static boolean set(CursorType type)
```
Changes the current thread cursor immediately to the specified cursor type.
- **Parameters:** `type` - The standard system cursor shape to display.
- **Returns:** `true` if successfully loaded and set, `false` otherwise.

### `setForWindow`
```java
public static boolean setForWindow(long hwnd, CursorType type)
```
Binds a standard cursor permanently to a native Win32 window (`HWND`). Windows automatically applies this cursor whenever the mouse pointer enters the window client area.
- **Parameters:**
  - `hwnd` - Native Win32 window handle.
  - `type` - Standard cursor shape.
- **Returns:** `true` on success, `false` otherwise.

### `get`
```java
public static long get()
```
Returns the native Win32 cursor handle (`HCURSOR`) of the calling thread.
- **Returns:** 64-bit integer pointer address (`long`) representing the native `HCURSOR`.

### `captureCursor`
```java
public static CursorShape captureCursor()
```
Captures the currently active Win32 cursor, including position, hot-spot coordinates, width, height, and ARGB pixel buffer.
- **Returns:** `CursorShape` record or `null` if capture unavailable.

### `blendCursor`
```java
public static boolean blendCursor(int[] pixels, int screenWidth, int screenHeight, int offsetX, int offsetY, boolean captureHidden)
```
Blends the live cursor shape directly into an existing ARGB/BGRA frame pixel array with proper alpha channel compositing.
- **Parameters:**
  - `pixels`: Target frame buffer.
  - `screenWidth`: Frame width.
  - `screenHeight`: Frame height.
  - `offsetX`: Screen X-origin offset.
  - `offsetY`: Screen Y-origin offset.
  - `captureHidden`: Whether to blend when the cursor is logically hidden.
- **Returns:** `true` if cursor was successfully captured and blended, `false` otherwise.

### `isCaptureAvailable`
```java
public static boolean isCaptureAvailable()
```
Checks if the native cursor capture library (`fastcursor.dll`) is loaded and operational.
- **Returns:** `true` if capture is available.

### `show`
```java
public static void show()
```
Increments Windows cursor display counter to make the mouse cursor visible.

### `hide`
```java
public static void hide()
```
Decrements Windows cursor display counter to hide the mouse cursor completely.

---

## 2. System Cursor Types (`CursorType`)

```java
public enum CursorType
```

| Constant | IDC Value | Description |
|:---|:---|:---|
| `ARROW` | `32512` | Standard arrow cursor (`IDC_ARROW`) |
| `IBEAM` | `32513` | Text selection I-beam (`IDC_IBEAM`) |
| `WAIT` | `32514` | Hourglass / spinner wait cursor (`IDC_WAIT`) |
| `CROSS` | `32515` | Crosshair cursor for snipping / precision (`IDC_CROSS`) |
| `APP_STARTING` | `32516` | Standard arrow and small spinning wait circle (`IDC_APPSTARTING`) |
| `HAND` | `32649` | Hand / pointer cursor for links and buttons (`IDC_HAND`) |
| `SIZE_ALL` | `32646` | Four-pointed arrow sizing cursor for moving elements (`IDC_SIZEALL`) |
| `SIZE_WE` | `32644` | Double-pointed horizontal arrow for resizing (`IDC_SIZEWE`) |
| `SIZE_NS` | `32645` | Double-pointed vertical arrow for resizing (`IDC_SIZENS`) |
| `SIZE_NWSE` | `32642` | Double-pointed diagonal NW-SE arrow for resizing (`IDC_SIZENWSE`) |
| `SIZE_NESW` | `32643` | Double-pointed diagonal NESW arrow for resizing (`IDC_SIZENESW`) |
| `NO` | `32648` | Circle with a diagonal slash (`IDC_NO`) |
| `HELP` | `32651` | Help cursor with arrow and question mark (`IDC_HELP`) |
| `UP_ARROW` | `32516` | Vertical bar / split resize cursor (`IDC_UPARROW`) |

---

## 3. Low-Level Native JNI Bindings (NativeUser32Cursor)

Internal package-private engine dispatching directly to native fastcursor.dll and user32.dll.

- Automatically binds `SetCursor`, `GetCursor`, `LoadCursorW`, `ShowCursor`, and `SetClassLongPtrW` (with fallback to `SetClassLongW`).
- Maintains an internal concurrent lookup cache `CURSOR_CACHE` to avoid repeated `LoadCursorW` Win32 allocations.
