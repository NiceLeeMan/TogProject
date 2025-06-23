package org.example;


import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.win32.WinNT;

// 기존 Kernel32 바인딩을 상속해서, 누락된 메서드만 추가
public interface MyKernel32 extends com.sun.jna.platform.win32.Kernel32 {
    MyKernel32 INSTANCE = Native.load(
            "kernel32", MyKernel32.class, W32APIOptions.DEFAULT_OPTIONS);

    // Win32 API: BOOL GetProcessHandleCount(HANDLE hProcess, PDWORD lpdwHandleCount);
    boolean GetProcessHandleCount(WinNT.HANDLE hProcess, IntByReference lpdwHandleCount);
}
