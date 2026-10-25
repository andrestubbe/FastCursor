#define UNICODE
#define _UNICODE
#include <windows.h>
#include <jni.h>

/**
 * FastCursor Native Win32 Engine
 * Pure JNI implementation for maximum compatibility (Java 21+ without preview flags).
 */
extern "C" {

JNIEXPORT jlong JNICALL Java_fastcursor_NativeUser32Cursor_loadCursorW(
    JNIEnv* env, jclass cls, jint idcValue) {
    HCURSOR hCursor = LoadCursorW(NULL, MAKEINTRESOURCEW(idcValue));
    return (jlong)hCursor;
}

JNIEXPORT jlong JNICALL Java_fastcursor_NativeUser32Cursor_setCursor(
    JNIEnv* env, jclass cls, jlong hCursor) {
    HCURSOR old = SetCursor((HCURSOR)hCursor);
    return (jlong)old;
}

JNIEXPORT jlong JNICALL Java_fastcursor_NativeUser32Cursor_getCursor(
    JNIEnv* env, jclass cls) {
    HCURSOR h = GetCursor();
    return (jlong)h;
}

JNIEXPORT jint JNICALL Java_fastcursor_NativeUser32Cursor_showCursor(
    JNIEnv* env, jclass cls, jboolean show) {
    return (jint)ShowCursor(show ? TRUE : FALSE);
}

JNIEXPORT jlong JNICALL Java_fastcursor_NativeUser32Cursor_setWindowCursor(
    JNIEnv* env, jclass cls, jlong hwnd, jlong hCursor) {
    return (jlong)SetClassLongPtrW((HWND)hwnd, -12 /* GCLP_HCURSOR */, (LONG_PTR)hCursor);
}

JNIEXPORT jintArray JNICALL Java_fastcursor_FastCursor_nativeCaptureCursor(
    JNIEnv* env, jclass cls, jintArray metaOut) {

    CURSORINFO ci = { sizeof(CURSORINFO) };
    if (!GetCursorInfo(&ci)) return nullptr;

    if (!(ci.flags & CURSOR_SHOWING)) return nullptr;
    if (!ci.hCursor) return nullptr;

    ICONINFO ii = { 0 };
    if (!GetIconInfo(ci.hCursor, &ii)) return nullptr;

    int cw = GetSystemMetrics(SM_CXCURSOR);
    int ch = GetSystemMetrics(SM_CYCURSOR);
    if (cw <= 0) cw = 32;
    if (ch <= 0) ch = 32;

    HDC hdcScreen = GetDC(NULL);
    HDC hdcMem = CreateCompatibleDC(hdcScreen);

    BITMAPINFO bmi = { 0 };
    bmi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
    bmi.bmiHeader.biWidth = cw;
    bmi.bmiHeader.biHeight = -ch; // Top-down
    bmi.bmiHeader.biPlanes = 1;
    bmi.bmiHeader.biBitCount = 32;
    bmi.bmiHeader.biCompression = BI_RGB;

    void* pBits = nullptr;
    HBITMAP hbm = CreateDIBSection(hdcMem, &bmi, DIB_RGB_COLORS, &pBits, NULL, 0);
    HBITMAP hbmOld = (HBITMAP)SelectObject(hdcMem, hbm);

    memset(pBits, 0, cw * ch * 4);
    DrawIconEx(hdcMem, 0, 0, ci.hCursor, cw, ch, 0, NULL, DI_NORMAL);

    jint meta[6];
    meta[0] = ci.ptScreenPos.x;
    meta[1] = ci.ptScreenPos.y;
    meta[2] = ii.xHotspot;
    meta[3] = ii.yHotspot;
    meta[4] = cw;
    meta[5] = ch;

    env->SetIntArrayRegion(metaOut, 0, 6, meta);

    int pixelCount = cw * ch;
    jintArray result = env->NewIntArray(pixelCount);
    if (result != nullptr) {
        env->SetIntArrayRegion(result, 0, pixelCount, (const jint*)pBits);
    }

    SelectObject(hdcMem, hbmOld);
    DeleteObject(hbm);
    DeleteDC(hdcMem);
    ReleaseDC(NULL, hdcScreen);

    if (ii.hbmMask) DeleteObject(ii.hbmMask);
    if (ii.hbmColor) DeleteObject(ii.hbmColor);

    return result;
}

}
