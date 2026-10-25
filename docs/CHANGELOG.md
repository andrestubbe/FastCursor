# Changelog

All notable changes to `FastCursor` will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.2] - 2026-10-25

### Added
- **Native Cursor Capture & Blending**: Added Win32 native cursor capture engine (`fastcursor.dll` via `FastCore.loadLibrary`).
  - `FastCursor.captureCursor()`: Queries current cursor icon, hotspots, dimensions, and shape.
  - `FastCursor.blendCursor(int[] pixels, int screenWidth, int screenHeight, int offsetX, int offsetY, boolean captureHidden)`: Hardware-accelerated alpha blending of live cursor straight into screen frame buffers.
  - `FastCursor.isCaptureAvailable()`: Verification probe for native capture capabilities.
- **Unit & Integration Tests**: Added `CursorCaptureTest` validating shape extraction and alpha compositing.

### Changed
- **JNI Architecture Transition**: Replaced Java 21 preview FFM in `NativeUser32Cursor.java` with standard JNI downcalls, eliminating `--enable-preview` requirements for all downstream modules.
- **Clean Java 21 Compatibility**: Compiles cleanly with zero compiler preview flags.

## [0.1.1] - 2026-10-20

### Changed
- **Responsibility Isolation**: Delegated all screen and window clipping APIs (`clip`, `clipToWindow`, `unclip`) to `FastMouse`, focusing `FastCursor` entirely on cursor shapes, thread cursors, and window hover bindings.
- **Added `FastCursor.get()`**: Direct method to retrieve the active calling thread's Win32 `HCURSOR` native memory handle.
- **Documentation & Benchmarks**: Added full JMH microbenchmark suite (`run-benchmark.bat`) and standardized `Demo.java`.

## [0.1.0] - 2026-10-20

### Added
- **Native User32 FFM Bindings (`NativeUser32Cursor`)**: Direct, zero-DLL Java 21+ Project Panama bindings to Windows `user32.dll`.
  - Direct downcall method handles for `SetCursor`, `GetCursor`, `LoadCursorW`, `ShowCursor`, and `SetClassLongPtrW`.
  - Built-in concurrent handle caching to eliminate redundant system calls for identical IDC resource IDs.
- **Unified FastCursor API (`FastCursor`)**:
  - `FastCursor.set(CursorType)`: Immediately updates the current thread cursor to standard shapes.
  - `FastCursor.setForWindow(long hwnd, CursorType)`: Permanently binds standard cursor shapes to specific Win32 window handles (`HWND`) via class attribute modification.
  - `FastCursor.get()`: Returns the native `HCURSOR` memory handle of the calling thread.
  - `FastCursor.show()` / `FastCursor.hide()`: Controls visibility of the mouse cursor.
- **Cursor Type Enumeration (`CursorType`)**: Complete Win32 IDC resource mapping (`ARROW`, `IBEAM`, `WAIT`, `CROSS`, `HAND`, `SIZE_ALL`, `SIZE_WE`, `SIZE_NS`, `SIZE_NWSE`, `SIZE_NESW`, `NO`, `HELP`, `UP_ARROW`).
- **Comprehensive Unit Tests (`FastCursorTest`)**: Verifying enum values, handle retrieval, null-safety, and execution of Win32 calls.
- **JMH Microbenchmark Suite**: Profiling handle cache hits and active cursor switching throughput.
