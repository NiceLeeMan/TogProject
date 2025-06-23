package org.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import com.sun.management.UnixOperatingSystemMXBean;
import com.sun.jna.platform.win32.WinNT;

/**
 * HandleLeakTest
 *
 * 1) 수동 open/close 전후 핸들 카운트
 * 2) try-with-resources 전후 핸들 카운트
 *
 * 이 프로그램을 실행하면
 *   - no-close 상태에서 +1이 찍히고
 *   - 수동 close 후 다시 원위치
 *   - try-with-resources 중간에 +1 → 블록 끝에 자동 close로 –1
 * 을 순차적으로 확인할 수 있습니다.
 */
public class HandleLeakTest {

    // (Windows) 커널32 GetProcessHandleCount 바인딩
    public interface MyKernel32 extends com.sun.jna.platform.win32.Kernel32 {
        MyKernel32 INSTANCE = Native.load(
                "kernel32", MyKernel32.class, W32APIOptions.DEFAULT_OPTIONS
        );
        boolean GetProcessHandleCount(WinNT.HANDLE hProcess, IntByReference lpdwHandleCount);
    }

    /**
     * 현재 프로세스의 열린 핸들(FD) 수를 반환.
     * Windows면 GetProcessHandleCount,
     * 그 외 OS면 UnixOperatingSystemMXBean을 사용.
     */
    private static long getOpenHandleCount() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                MyKernel32 k32 = MyKernel32.INSTANCE;
                WinNT.HANDLE hProc = k32.GetCurrentProcess();
                IntByReference cnt = new IntByReference();
                k32.GetProcessHandleCount(hProc, cnt);
                return cnt.getValue();
            } else {
                UnixOperatingSystemMXBean uos =
                        ManagementFactory.getPlatformMXBean(UnixOperatingSystemMXBean.class);
                return uos.getOpenFileDescriptorCount();
            }
        } catch (Throwable t) {
            return -1;
        }
    }

    public static void main(String[] args) throws Exception {
        // 실제 존재하는 파일 경로로 수정하세요.
        String path = "C:\\Users\\dlrlq\\OneDrive - 가톨릭대학교\\바탕 화면\\Tog\\TogProject\\src\\main\\resources\\config\\db.properties";

        System.out.println("=== Initial Handle Count ===");
        System.out.println("Handles = " + getOpenHandleCount());
        System.out.println();

        // 1) without close
        System.out.println("=== 1) Without Close ===");
        System.out.println("Before open:               " + getOpenHandleCount());
        FileInputStream fis1 = new FileInputStream(path);
        System.out.println(" After open (no-close):    " + getOpenHandleCount());
        // 수동 close
        fis1.close();
        System.out.println(" After manual close:       " + getOpenHandleCount());
        System.out.println();

        // 2) with try-with-resources
        System.out.println("=== 2) With try-with-resources ===");
        System.out.println("Before try-with-resources: " + getOpenHandleCount());
        try (FileInputStream fis2 = new FileInputStream(path)) {
            System.out.println(" During open:              " + getOpenHandleCount());
        }
        System.out.println(" After auto-close:         " + getOpenHandleCount());
    }
}
