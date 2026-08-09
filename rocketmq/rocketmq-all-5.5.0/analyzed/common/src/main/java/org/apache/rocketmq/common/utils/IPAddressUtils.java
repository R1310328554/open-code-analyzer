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

import java.math.BigInteger;
import java.net.InetAddress;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * IP 与 CIDR 校验工具，支持 IPv4/IPv6 及网段包含判断。
 */
public class IPAddressUtils {

    /** CIDR 表示法中网络前缀与掩码长度的分隔符。 */
    private static final String SLASH = "/";

    private static final InetAddressValidator VALIDATOR = InetAddressValidator.getInstance();

    /** 判断字符串是否为合法 IP 或 CIDR 表示。 */
    public static boolean isValidIPOrCidr(String ipOrCidr) {
        return isValidIp(ipOrCidr) || isValidCidr(ipOrCidr);
    }

    /** 判断是否为合法 IP 地址（IPv4 或 IPv6）。 */
    public static boolean isValidIp(String ip) {
        return VALIDATOR.isValid(ip);
    }

    /** 判断是否为合法 IPv4 地址。 */
    public static boolean isValidIPv4(String ip) {
        return VALIDATOR.isValidInet4Address(ip);
    }

    /** 判断是否为合法 IPv6 地址。 */
    public static boolean isValidIPv6(String ip) {
        return VALIDATOR.isValidInet6Address(ip);
    }

    /** 判断是否为合法 CIDR（IPv4 或 IPv6 网段）。 */
    public static boolean isValidCidr(String cidr) {
        return isValidIPv4Cidr(cidr) || isValidIPv6Cidr(cidr);
    }

    /** 校验 IPv4 CIDR，前缀长度须在 0～32 之间。 */
    public static boolean isValidIPv4Cidr(String cidr) {
        try {
            String[] parts = cidr.split(SLASH);
            if (parts.length != 2) {
                return false;
            }
            InetAddress ip = InetAddress.getByName(parts[0]);
            if (ip.getAddress().length != 4) {
                return false;
            }
            int prefix = Integer.parseInt(parts[1]);
            return prefix >= 0 && prefix <= 32;
        } catch (Exception e) {
            return false;
        }
    }

    /** 校验 IPv6 CIDR，前缀长度须在 0～128 之间。 */
    public static boolean isValidIPv6Cidr(String cidr) {
        try {
            String[] parts = cidr.split(SLASH);
            if (parts.length != 2) {
                return false;
            }
            InetAddress ip = InetAddress.getByName(parts[0]);
            if (ip.getAddress().length != 16) {
                return false;
            }
            int prefix = Integer.parseInt(parts[1]);
            return prefix >= 0 && prefix <= 128;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断 IP 是否落在给定 CIDR 网段内；若 cidr 无掩码则做精确字符串匹配。
     *
     * @param ip   待检测 IP
     * @param cidr IP 或 CIDR 字符串
     */
    public static boolean isIPInRange(String ip, String cidr) {
        try {
            String[] parts = cidr.split(SLASH);
            if (parts.length == 1) {
                return StringUtils.equals(ip, cidr);
            }
            if (parts.length != 2) {
                return false;
            }
            InetAddress cidrIp = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            BigInteger cidrIpBigInt = new BigInteger(1, cidrIp.getAddress());
            BigInteger ipBigInt = new BigInteger(1, InetAddress.getByName(ip).getAddress());

            BigInteger mask = BigInteger.valueOf(-1).shiftLeft(cidrIp.getAddress().length * 8 - prefixLength);
            BigInteger cidrIpLower = cidrIpBigInt.and(mask);
            BigInteger cidrIpUpper = cidrIpLower.add(mask.not());

            return ipBigInt.compareTo(cidrIpLower) >= 0 && ipBigInt.compareTo(cidrIpUpper) <= 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
