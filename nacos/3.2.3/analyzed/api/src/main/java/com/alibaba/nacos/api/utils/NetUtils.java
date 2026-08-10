/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.utils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 网络地址解析工具类。
 *
 * <p>用于探测本机可用 IP/主机名，支持通过系统属性覆盖默认行为。</p>
 *
 * @author xuanyin.zy
 */
public class NetUtils {
    
    /** 客户端显式指定本地 IP 的系统属性键。 */
    private static final String CLIENT_LOCAL_IP_PROPERTY = "com.alibaba.nacos.client.local.ip";
    
    /** 是否优先返回主机名而非 IP 地址的系统属性键。 */
    private static final String CLIENT_LOCAL_PREFER_HOSTNAME_PROPERTY =
        "com.alibaba.nacos.client.local.preferHostname";
    
    /** 控制优先 IPv4/IPv6 的 JVM 系统属性键。 */
    private static final String LEGAL_LOCAL_IP_PROPERTY = "java.net.preferIPv6Addresses";
    
    /** 地址解析失败时的默认返回值。 */
    private static final String DEFAULT_SOLVE_FAILED_RETURN = "resolve_failed";
    
    /** 缓存的本地 IP/主机名，避免重复探测。 */
    private static String localIp;
    
    /**
     * 获取本机 IP 或主机名。
     *
     * <p>优先读取 {@link #CLIENT_LOCAL_IP_PROPERTY} 指定值；否则自动探测首个非回环网卡地址。</p>
     *
     * @return 本地 IP、主机名或 {@link #DEFAULT_SOLVE_FAILED_RETURN}
     */
    public static String localIp() {
        if (!StringUtils.isEmpty(localIp)) {
            return localIp;
        }
        if (System.getProperties().containsKey(CLIENT_LOCAL_IP_PROPERTY)) {
            return localIp = System.getProperty(CLIENT_LOCAL_IP_PROPERTY, getAddress());
        }
        localIp = getAddress();
        return localIp;
    }
    
    /** 探测并返回本机地址字符串（IP 或主机名）。 */
    private static String getAddress() {
        InetAddress inetAddress = findFirstNonLoopbackAddress();
        if (inetAddress == null) {
            return DEFAULT_SOLVE_FAILED_RETURN;
        }
        
        boolean preferHost =
            Boolean.parseBoolean(System.getProperty(CLIENT_LOCAL_PREFER_HOSTNAME_PROPERTY));
        return preferHost ? inetAddress.getHostName() : inetAddress.getHostAddress();
    }
    
    /** 遍历网卡，返回首个符合条件的非回环 {@link InetAddress}。 */
    private static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;
        
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics
                .hasMoreElements();) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else {
                        continue;
                    }
                    
                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs
                        .hasMoreElements();) {
                        InetAddress address = addrs.nextElement();
                        boolean isLegalIpVersion =
                            Boolean.parseBoolean(System.getProperty(LEGAL_LOCAL_IP_PROPERTY))
                                ? address instanceof Inet6Address : address instanceof Inet4Address;
                        if (isLegalIpVersion && !address.isLoopbackAddress()) {
                            result = address;
                        }
                    }
                    
                }
            }
        } catch (Exception ignore) {
        }
        
        if (result != null) {
            return result;
        }
        
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException ignore) {
        }
        
        return null;
        
    }
}
