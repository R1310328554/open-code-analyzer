package com.taobao.arthas.common;

import java.lang.management.ManagementFactory;

/**
 * 当前 JVM 进程 ID 与主类名探测，供 attach、日志与诊断使用。
 *
 * @author hengyunabc 2019-02-16
 */
public class PidUtils {
    /** 进程 ID 字符串，解析失败时为 "-1" */
    private static String PID = "-1";
    private static long pid = -1;

    /** {@code sun.java.command} 中第一个 token（主类名） */
    private static String MAIN_CLASS = "";

    static {
        // https://stackoverflow.com/a/7690178
        try {
            String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            int index = jvmName.indexOf('@');

            if (index > 0) {
                PID = Long.toString(Long.parseLong(jvmName.substring(0, index)));
                pid = Long.parseLong(PID);
            }
        } catch (Throwable e) {
            // ignore
        }

        try {
            String command = System.getProperty("sun.java.command", "");
            // sun.java.command 含主类名及参数，只取第一个 token 作为主类
            int spaceIndex = command.indexOf(' ');
            MAIN_CLASS = spaceIndex != -1 ? command.substring(0, spaceIndex) : command;
        } catch (Throwable e) {
            // ignore
        }

    }

    private PidUtils() {
    }

    /** 当前进程 ID 字符串 */
    public static String currentPid() {
        return PID;
    }

    /** 当前进程 ID（long），失败时为 -1 */
    public static long currentLongPid() {
        return pid;
    }

    /** 启动主类全限定名 */
    public static String mainClass() {
        return MAIN_CLASS;
    }
}
