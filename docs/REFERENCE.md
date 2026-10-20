# FastCursor API Reference Manual

`FastCursor` provides high-performance native Win32 cursor and pointer management for Java 21+ via Foreign Function & Memory (FFM) with zero native compilation and zero DLL overhead.

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

### `clip`
```java
public static boolean clip(int left, int top, int right, int bottom)
```
Confines mouse cursor movements to a specific rectangle on the primary desktop.
- **Parameters:**
  - `left` - Left screen pixel.
  - `top` - Top screen pixel.
  - `right` - Right screen pixel.
  - `bottom` - Bottom screen pixel.
- **Returns:** `true` if clipping succeeded, `false` otherwise.

### `unclip`
```java
public static boolean unclip()
```
Releases cursor confinement and restores free desktop movement across all screens.
- **Returns:** `true` if unclipped successfully, `false` otherwise.

---

## 2. System Cursor Types (`CursorType`)

```java
public enum CursorType
```

| Constant | IDC Value | Description |
|:---|:---|:---|
| `ARROW` | `32512` | Standard arrow cursor (`IDC_ARROW`) |
| `IBEAM` | `32513` | Text I-beam cursor for editable text (`IDC_IBEAM`) |
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

## 3. Low-Level FFM Bindings (`NativeUser32Cursor`)

Internal package-private engine managing `java.lang.foreign` Linker calls to `user32.dll`.

- Automatically binds `SetCursor`, `GetCursor`, `LoadCursorW`, `ShowCursor`, `ClipCursor`, and `SetClassLongPtrW` (with fallback to `SetClassLongW`).
- Maintains an internal concurrent lookup cache `CURSOR_CACHE` to avoid repeated `LoadCursorW` Win32 allocations.
