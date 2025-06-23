package org.example;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Properties;

// JNA 윈도우 핸들 수 조회용
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
// Unix FD 조회용
import com.sun.management.UnixOperatingSystemMXBean;

public class FileDescriptorDemo {

    /**
     * 현재 프로세스의 열린 핸들(FD) 수를 반환.
     * Windows면 GetProcessHandleCount,
     * 그 외 OS면 UnixOperatingSystemMXBean을 사용.
     */
    private static long getOpenFdCount() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // MyKernel32.INSTANCE 사용
            MyKernel32 kernel32   = MyKernel32.INSTANCE;
            WinNT.HANDLE hProcess = kernel32.GetCurrentProcess();
            IntByReference handleCount = new IntByReference();
            kernel32.GetProcessHandleCount(hProcess, handleCount);
            return handleCount.getValue();
        }
        // Unix 계열은 기존 방식 유지…
        UnixOperatingSystemMXBean osBean =
                ManagementFactory.getPlatformMXBean(UnixOperatingSystemMXBean.class);
        return osBean.getOpenFileDescriptorCount();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Before load: open FDs = " + getOpenFdCount());

        // 1) Without try-with-resources
        InputStream is1 = FileDescriptorDemo.class
                .getClassLoader()
                .getResourceAsStream("config/db.properties");
        if (is1 != null) {
            new Properties().load(is1);
            // 여기서 close()를 호출하지 않으면…
        }
        // GC로 스트림이 언제 닫힐지 기대
        System.gc();
        Thread.sleep(500);
        System.out.println("After load without close: open FDs = " + getOpenFdCount());

        // 2) With try-with-resources
        try (InputStream is2 = FileDescriptorDemo.class
                .getClassLoader()
                .getResourceAsStream("config/db.properties")) {
            if (is2 != null) {
                new Properties().load(is2);
            }
        }
        System.gc();
        Thread.sleep(500);
        System.out.println("After load with try-with-resources: open FDs = " + getOpenFdCount());
    }
}
