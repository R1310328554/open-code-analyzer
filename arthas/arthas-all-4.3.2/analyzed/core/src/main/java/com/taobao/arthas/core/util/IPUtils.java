package com.taobao.arthas.core.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 本机 IP 与操作系统类型检测工具。
 *
 * @author weipeng2k 2015-01-30 15:06:47
 */
public class IPUtils {

    private static final String WINDOWS = "windows";
    private static final String OS_NAME = "os.name";

    /**
     * 判断当前操作系统是否为 Windows。
     *
     * @return true 表示 Windows
     */
    public static boolean isWindowsOS() {
        String osName = System.getProperty(OS_NAME);
        return osName.toLowerCase().contains(WINDOWS);
    }

    /**
     * 获取本机 IP 地址，自动区分 Windows 与 Linux/Unix 策略。
     * <p>
     * Windows 直接使用 {@link InetAddress#getLocalHost()}；非 Windows 若 localHost
     * 为回环地址则遍历网卡，选取 site-local 且非 loopback 的 IPv4 地址。
     *
     * @return 本机 IP 字符串，无法解析时返回 null
     */
    public static String getLocalIP() {
        InetAddress ip = null;
        try {
            if (isWindowsOS()) {
                ip = InetAddress.getLocalHost();
            } else {
                // 非 Windows：若 getLocalHost 不是回环则直接使用，否则扫描网卡
                if (!InetAddress.getLocalHost().isLoopbackAddress()) {
                    ip = InetAddress.getLocalHost();
                } else {
                    boolean bFindIP = false;
                    Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
                    while (netInterfaces.hasMoreElements()) {
                        if (bFindIP) {
                            break;
                        }
                        NetworkInterface ni = netInterfaces.nextElement();
                        // 遍历该网卡下所有 IP
                        Enumeration<InetAddress> ips = ni.getInetAddresses();
                        while (ips.hasMoreElements()) {
                            ip = ips.nextElement();
                            // 127.x 为回环；含 ':' 为 IPv6，此处只取 IPv4 site-local
                            if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                                    && !ip.getHostAddress().contains(":")) {
                                bFindIP = true;
                                break;
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
        }

        return ip == null ? null : ip.getHostAddress();
    }


    /**
     * 判断 IP 字符串是否仅由 0、点号或冒号组成（如 0.0.0.0、::）。
     *
     * @param ipStr IP 字符串
     * @return true 表示“全零”占位地址
     */
    public static boolean isAllZeroIP(String ipStr) {
        if (ipStr == null || ipStr.isEmpty()) {
            return false;
        }
        char[] charArray = ipStr.toCharArray();

        for (char c : charArray) {
            if (c != '0' && c != '.' && c != ':') {
                return false;
            }
        }

        return true;
    }
}
