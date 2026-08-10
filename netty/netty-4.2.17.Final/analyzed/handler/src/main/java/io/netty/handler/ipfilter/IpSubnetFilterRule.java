/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.ipfilter;

import io.netty.util.NetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SocketUtils;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Use this class to create rules for {@link RuleBasedIpFilter} that group IP addresses into subnets.
 * Supports both, IPv4 and IPv6.
 *
 * <p>将 IP 地址与 CIDR 前缀封装为 {@link IpFilterRule}，支持 IPv4/IPv6，
 * 供 {@link IpSubnetFilter} 排序与二分查找使用。</p>
 */
public final class IpSubnetFilterRule implements IpFilterRule, Comparable<IpSubnetFilterRule> {

    /** 实际执行匹配的 IPv4 或 IPv6 子网规则。 */
    private final IpFilterRule filterRule;
    /** 规则对应的 IP 字符串（用于比较与重叠检测）。 */
    private final String ipAddress;

    /**
     * Create a new {@link IpSubnetFilterRule} instance
     *
     * @param ipAddressWithCidr IP Address with CIDR notation, e.g. (192.168.0.0/16) or (2001:db8::/32)
     * @param ruleType {@link IpFilterRuleType} to use
     *
     * <p>解析 {@code 地址/前缀} 形式字符串创建规则。</p>
     */
    public IpSubnetFilterRule(String ipAddressWithCidr, IpFilterRuleType ruleType) {
        try {
            String[] ipAndCidr = ipAddressWithCidr.split("/");
            if (ipAndCidr.length != 2) {
                throw new IllegalArgumentException("ipAddressWithCidr: " + ipAddressWithCidr +
                        " (expected: \"<ip-address>/<mask-size>\")");
            }

            ipAddress = ipAndCidr[0];
            int cidrPrefix = Integer.parseInt(ipAndCidr[1]);
            filterRule = selectFilterRule(SocketUtils.addressByName(ipAddress), cidrPrefix, ruleType);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("ipAddressWithCidr", e);
        }
    }

    /**
     * Create a new {@link IpSubnetFilterRule} instance
     *
     * @param ipAddress IP Address as {@link String}
     * @param cidrPrefix CIDR Prefix
     * @param ruleType {@link IpFilterRuleType} to use
     */
    public IpSubnetFilterRule(String ipAddress, int cidrPrefix, IpFilterRuleType ruleType) {
        try {
            this.ipAddress = ipAddress;
            filterRule = selectFilterRule(SocketUtils.addressByName(ipAddress), cidrPrefix, ruleType);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("ipAddress", e);
        }
    }

    /**
     * Create a new {@link IpSubnetFilterRule} instance
     *
     * @param ipAddress IP Address as {@link InetAddress}
     * @param cidrPrefix CIDR Prefix
     * @param ruleType {@link IpFilterRuleType} to use
     */
    public IpSubnetFilterRule(InetAddress ipAddress, int cidrPrefix, IpFilterRuleType ruleType) {
        this.ipAddress = ipAddress.getHostAddress();
        filterRule = selectFilterRule(ipAddress, cidrPrefix, ruleType);
    }

    /** 按地址族选择 IPv4 或 IPv6 内部规则实现。 */
    private static IpFilterRule selectFilterRule(InetAddress ipAddress, int cidrPrefix, IpFilterRuleType ruleType) {
        ObjectUtil.checkNotNull(ipAddress, "ipAddress");
        ObjectUtil.checkNotNull(ruleType, "ruleType");

        if (ipAddress instanceof Inet4Address) {
            return new Ip4SubnetFilterRule((Inet4Address) ipAddress, cidrPrefix, ruleType);
        } else if (ipAddress instanceof Inet6Address) {
            return new Ip6SubnetFilterRule((Inet6Address) ipAddress, cidrPrefix, ruleType);
        } else {
            throw new IllegalArgumentException("Only IPv4 and IPv6 addresses are supported");
        }
    }

    @Override
    public boolean matches(InetSocketAddress remoteAddress) {
        return filterRule.matches(remoteAddress);
    }

    @Override
    public IpFilterRuleType ruleType() {
        return filterRule.ruleType();
    }

    /**
     * Get IP Address of this rule
     *
     * <p>返回规则网段的 IP 字符串。</p>
     */
    String getIpAddress() {
        return ipAddress;
    }

    /**
     * {@link Ip4SubnetFilterRule} or {@link Ip6SubnetFilterRule}
     *
     * <p>返回内部 IPv4/IPv6 子网匹配实现。</p>
     */
    IpFilterRule getFilterRule() {
        return filterRule;
    }

    /** 按网络地址排序，供 {@link IpSubnetFilter} 使用。 */
    @Override
    public int compareTo(IpSubnetFilterRule ipSubnetFilterRule) {
        if (filterRule instanceof Ip4SubnetFilterRule) {
            return compareInt(((Ip4SubnetFilterRule) filterRule).networkAddress,
                    ((Ip4SubnetFilterRule) ipSubnetFilterRule.filterRule).networkAddress);
        } else {
            return ((Ip6SubnetFilterRule) filterRule).networkAddress
                    .compareTo(((Ip6SubnetFilterRule) ipSubnetFilterRule.filterRule).networkAddress);
        }
    }

    /**
     * It'll compare IP address with {@link Ip4SubnetFilterRule#networkAddress} or
     * {@link Ip6SubnetFilterRule#networkAddress}.
     *
     * @param inetSocketAddress {@link InetSocketAddress} to match
     * @return 0 if IP Address match else difference index.
     *
     * <p>将远端地址映射到子网网络地址后与规则网络地址比较，供二分查找使用。</p>
     */
    int compareTo(InetSocketAddress inetSocketAddress) {
        if (filterRule instanceof Ip4SubnetFilterRule) {
            Ip4SubnetFilterRule ip4SubnetFilterRule = (Ip4SubnetFilterRule) filterRule;
            return compareInt(ip4SubnetFilterRule.networkAddress, NetUtil.ipv4AddressToInt((Inet4Address)
                    inetSocketAddress.getAddress()) & ip4SubnetFilterRule.subnetMask);
        } else {
            Ip6SubnetFilterRule ip6SubnetFilterRule = (Ip6SubnetFilterRule) filterRule;
            return ip6SubnetFilterRule.networkAddress
                    .compareTo(Ip6SubnetFilterRule.ipToInt((Inet6Address) inetSocketAddress.getAddress())
                            .and(ip6SubnetFilterRule.subnetMask));
        }
    }

    /**
     * Equivalent to {@link Integer#compare(int, int)}
     *
     * <p>三值比较，等价于 {@link Integer#compare}。</p>
     */
    private static int compareInt(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /** IPv4 CIDR 子网匹配实现。 */
    static final class Ip4SubnetFilterRule implements IpFilterRule {

        /** 子网网络地址（主机位清零）。 */
        private final int networkAddress;
        /** 子网掩码。 */
        private final int subnetMask;
        /** 匹配时的动作。 */
        private final IpFilterRuleType ruleType;

        private Ip4SubnetFilterRule(Inet4Address ipAddress, int cidrPrefix, IpFilterRuleType ruleType) {
            if (cidrPrefix < 0 || cidrPrefix > 32) {
                throw new IllegalArgumentException(String.format("IPv4 requires the subnet prefix to be in range of "
                        + "[0,32]. The prefix was: %d", cidrPrefix));
            }

            subnetMask = prefixToSubnetMask(cidrPrefix);
            networkAddress = NetUtil.ipv4AddressToInt(ipAddress) & subnetMask;
            this.ruleType = ruleType;
        }

        @Override
        public boolean matches(InetSocketAddress remoteAddress) {
            final InetAddress inetAddress = remoteAddress.getAddress();
            if (inetAddress instanceof Inet4Address) {
                int ipAddress = NetUtil.ipv4AddressToInt((Inet4Address) inetAddress);
                return (ipAddress & subnetMask) == networkAddress;
            }
            return false;
        }

        @Override
        public IpFilterRuleType ruleType() {
            return ruleType;
        }

        /** 将 CIDR 前缀转换为 32 位子网掩码；前缀 0 需在 long 上移位避免 int 移位陷阱。 */
        private static int prefixToSubnetMask(int cidrPrefix) {
            /*
             * Perform the shift on a long and downcast it to int afterwards.
             * This is necessary to handle a cidrPrefix of zero correctly.
             * The left shift operator on an int only uses the five least
             * significant bits of the right-hand operand. Thus -1 << 32 evaluates
             * to -1 instead of 0. The left shift operator applied on a long
             * uses the six least significant bits.
             *
             * Also see https://github.com/netty/netty/issues/2767
             *
             * 在 long 上左移再转 int，正确支持 /0；见 netty#2767。
             */
            return (int) (-1L << 32 - cidrPrefix);
        }
    }

    /** IPv6 CIDR 子网匹配实现。 */
    static final class Ip6SubnetFilterRule implements IpFilterRule {

        private static final BigInteger MINUS_ONE = BigInteger.valueOf(-1);

        /** 子网网络地址。 */
        private final BigInteger networkAddress;
        /** 子网掩码。 */
        private final BigInteger subnetMask;
        /** 匹配时的动作。 */
        private final IpFilterRuleType ruleType;

        private Ip6SubnetFilterRule(Inet6Address ipAddress, int cidrPrefix, IpFilterRuleType ruleType) {
            if (cidrPrefix < 0 || cidrPrefix > 128) {
                throw new IllegalArgumentException(String.format("IPv6 requires the subnet prefix to be in range of "
                        + "[0,128]. The prefix was: %d", cidrPrefix));
            }

            subnetMask = prefixToSubnetMask(cidrPrefix);
            networkAddress = ipToInt(ipAddress).and(subnetMask);
            this.ruleType = ruleType;
        }

        @Override
        public boolean matches(InetSocketAddress remoteAddress) {
            final InetAddress inetAddress = remoteAddress.getAddress();
            if (inetAddress instanceof Inet6Address) {
                BigInteger ipAddress = ipToInt((Inet6Address) inetAddress);
                return ipAddress.and(subnetMask).equals(subnetMask) || ipAddress.and(subnetMask).equals(networkAddress);
            }
            return false;
        }

        @Override
        public IpFilterRuleType ruleType() {
            return ruleType;
        }

        /** 将 16 字节 IPv6 地址转为正数 BigInteger。 */
        private static BigInteger ipToInt(Inet6Address ipAddress) {
            byte[] octets = ipAddress.getAddress();
            assert octets.length == 16;

            return new BigInteger(1, octets);
        }

        /** CIDR 前缀转 128 位 IPv6 掩码。 */
        private static BigInteger prefixToSubnetMask(int cidrPrefix) {
            return MINUS_ONE.shiftLeft(128 - cidrPrefix);
        }
    }
}
