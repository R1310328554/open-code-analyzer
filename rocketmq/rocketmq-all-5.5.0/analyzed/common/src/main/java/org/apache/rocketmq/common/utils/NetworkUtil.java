/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 网络与本地地址探测工具：平台识别、Selector 优化、IPv4/IPv6 格式化等。
 */
public class NetworkUtil {
    public static final String OS_NAME = System.getProperty("os.name");

    private static final Logger log = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);
    private static boolean isLinuxPlatform = false;
    private static boolean isWindowsPlatform = false;

    static {
        if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
            isLinuxPlatform = true;
        }

        if (OS_NAME != null && OS_NAME.toLowerCase().contains("windows")) {
            isWindowsPlatform = true;
        }
    }

    public static boolean isWindowsPlatform() {
        return isWindowsPlatform;
    }

    /** Linux 下优先使用 EPoll Selector，失败则回退默认实现。 */
    public static Selector openSelector() throws IOException {
        Selector result = null;

        if (isLinuxPlatform()) {
            try {
                final Class<?> providerClazz = Class.forName("sun.nio.ch.EPollSelectorProvider");
                try {
                    final Method method = providerClazz.getMethod("provider");
                    final SelectorProvider selectorProvider = (SelectorProvider) method.invoke(null);
                    if (selectorProvider != null) {
                        result = selectorProvider.openSelector();
                    }
                } catch (final Exception e) {
                    log.warn("Open ePoll Selector for linux platform exception", e);
                }
            } catch (final Exception e) {
                // 忽略，改用默认 Selector
            }
        }

        if (result == null) {
            result = Selector.open();
        }

        return result;
    }

    public static boolean isLinuxPlatform() {
        return isLinuxPlatform;
    }

    /** 枚举本机可用 InetAddress（排除网桥、虚拟、点对点及未 up 的网卡）。 */
    public static List<InetAddress> getLocalInetAddressList() throws SocketException {
        Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
        List<InetAddress> inetAddressList = new ArrayList<>();
        // 遍历网卡，收集非 bridge、非虚拟、非 PPP 且处于 up 状态的地址
        while (enumeration.hasMoreElements()) {
            final NetworkInterface nif = enumeration.nextElement();
            if (isBridge(nif) || nif.isVirtual() || nif.isPointToPoint() || !nif.isUp()) {
                continue;
            }
            InetAddressValidator validator = InetAddressValidator.getInstance();
            final Enumeration<InetAddress> en = nif.getInetAddresses();
            while (en.hasMoreElements()) {
                final InetAddress address = en.nextElement();
                if (address instanceof Inet4Address) {
                    byte[] ipByte = address.getAddress();
                    if (ipByte.length == 4) {
                        if (validator.isValidInet4Address(UtilAll.ipToIPv4Str(ipByte))) {
                            inetAddressList.add(address);
                        }
                    }
                } else if (address instanceof Inet6Address) {
                    byte[] ipByte = address.getAddress();
                    if (ipByte.length == 16) {
                        if (validator.isValidInet6Address(UtilAll.ipToIPv6Str(ipByte))) {
                            inetAddressList.add(address);
                        }
                    }
                }
            }
        }
        return inetAddressList;
    }

    /** 选取本机对外可用地址：优先 IPv4 公网，否则内网或 IPv6，最后回退 localhost。 */
    public static InetAddress getLocalInetAddress() {
        try {
            ArrayList<InetAddress> ipv4Result = new ArrayList<>();
            ArrayList<InetAddress> ipv6Result = new ArrayList<>();
            List<InetAddress> localInetAddressList = getLocalInetAddressList();
            for (InetAddress inetAddress : localInetAddressList) {
                // 跳过回环地址
                if (inetAddress.isLoopbackAddress()) {
                    continue;
                }
                if (inetAddress instanceof Inet6Address) {
                    ipv6Result.add(inetAddress);
                } else {
                    ipv4Result.add(inetAddress);
                }
            }
            // 优先 IPv4，且优先非公网/内网划分中的外网地址
            if (!ipv4Result.isEmpty()) {
                for (InetAddress ip : ipv4Result) {
                    if (UtilAll.isInternalIP(ip.getAddress())) {
                        continue;
                    }
                    return ip;
                }
                return ipv4Result.get(ipv4Result.size() - 1);
            } else if (!ipv6Result.isEmpty()) {
                for (InetAddress ip : ipv6Result) {
                    if (UtilAll.isInternalV6IP(ip)) {
                        continue;
                    }
                    return ip;
                }
                return ipv6Result.get(0);
            }
            // 未找到合适地址时回退到 localhost
            return InetAddress.getLocalHost();
        } catch (Exception e) {
            log.error("Failed to obtain local address", e);
        }

        return null;
    }

    /** 返回规范化后的本机 IP 字符串（IPv6 带方括号）。 */
    public static String getLocalAddress() {
        InetAddress localHost = getLocalInetAddress();
        return normalizeHostAddress(localHost);
    }

    /** IPv6 地址输出为 [addr] 形式，IPv4 直接返回 host 字符串。 */
    public static String normalizeHostAddress(final InetAddress localHost) {
        if (localHost instanceof Inet6Address) {
            return "[" + localHost.getHostAddress() + "]";
        } else {
            return localHost.getHostAddress();
        }
    }

    /** 去掉 IPv6 地址外层的方括号，非 bracket 形式原样返回。 */
    public static String denormalizeHostAddress(final String bracketedAddress) {
        if (bracketedAddress == null) {
            return null;
        }

        if (bracketedAddress.startsWith("[") && bracketedAddress.endsWith("]")) {
            return bracketedAddress.substring(1, bracketedAddress.length() - 1);
        } else {
            return bracketedAddress;
        }
    }

    /** 将 "host:port" 字符串解析为 {@link InetSocketAddress}。 */
    public static SocketAddress string2SocketAddress(final String addr) {
        int split = addr.lastIndexOf(":");
        String host = addr.substring(0, split);
        String port = addr.substring(split + 1);
        return new InetSocketAddress(host, Integer.parseInt(port));
    }

    /** 将 SocketAddress 格式化为 host:port 字符串。 */
    public static String socketAddress2String(final SocketAddress addr) {
        StringBuilder sb = new StringBuilder();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) addr;
        sb.append(inetSocketAddress.getAddress().getHostAddress());
        sb.append(":");
        sb.append(inetSocketAddress.getPort());
        return sb.toString();
    }

    /** 先解析再格式化，统一 host:port 表示。 */
    public static String convert2IpString(final String addr) {
        return socketAddress2String(string2SocketAddress(addr));
    }

    /** Linux 下通过 /sys/class/net/.../bridge 判断网卡是否为 bridge。 */
    private static boolean isBridge(NetworkInterface networkInterface) {
        try {
            if (isLinuxPlatform()) {
                String interfaceName = networkInterface.getName();
                File file = new File("/sys/class/net/" + interfaceName + "/bridge");
                return file.exists();
            }
        } catch (SecurityException e) {
            // 忽略权限异常
        }
        return false;
    }

    // 校验常见 IPv6 写法，例如：
    // 带 scope：2001:0db8:85a3:0000:0000:8a2e:0370:7334%eth0
    // 带方括号：[2001:0db8:85a3:0000:0000:8a2e:0370:7334]
    public static boolean validCommonInet6Address(String ipOrCidr) {
        String  ipWithoutBracketed = denormalizeHostAddress(ipOrCidr);
        if (ipWithoutBracketed != null && ipWithoutBracketed.length() != 0) {
            InetAddressValidator validator = InetAddressValidator.getInstance();
            if (validator.isValidInet6Address(ipWithoutBracketed.split("%")[0])) {
                return true;
            }
        }
        return false;
    }
}
