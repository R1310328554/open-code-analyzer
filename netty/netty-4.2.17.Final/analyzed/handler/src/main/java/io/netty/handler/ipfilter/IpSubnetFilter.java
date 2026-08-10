/*
 * Copyright 2020 The Netty Project
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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ObjectUtil;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * This class allows one to filter new {@link Channel}s based on the
 * {@link IpSubnetFilter}s passed to its constructor. If no rules are provided, all connections
 * will be accepted since {@code acceptIfNotFound} is {@code true} by default.
 * </p>
 *
 * <p>
 * If you would like to explicitly take action on rejected {@link Channel}s, you should override
 * {@link AbstractRemoteAddressFilter#channelRejected(ChannelHandlerContext, SocketAddress)}.
 * </p>
 *
 * <p>
 *     Few Points to keep in mind:
 *     <ol>
 *         <li> Since {@link IpSubnetFilter} uses Binary search algorithm, it's a good
 *         idea to insert IP addresses in incremental order. </li>
 *         <li> Remove any over-lapping CIDR.  </li>
 *     </ol>
 * </p>
 *
 * <p>基于 CIDR 子网规则的高性能 IP 过滤器：IPv4/IPv6 分表、排序后二分查找。
 * 建议按网络地址递增添加规则并去除重叠子网；默认未命中时接受连接。</p>
 */
@Sharable
public class IpSubnetFilter extends AbstractRemoteAddressFilter<InetSocketAddress> {

    /** 未匹配任何子网时是否接受连接。 */
    private final boolean acceptIfNotFound;
    /** 已排序去重后的 IPv4 子网规则；无规则时为 null。 */
    private final IpSubnetFilterRule[] ipv4Rules;
    /** 已排序去重后的 IPv6 子网规则；无规则时为 null。 */
    private final IpSubnetFilterRule[] ipv6Rules;
    /** IPv4 规则是否全部为 ACCEPT 或全部为 REJECT；混合类型时为 null。 */
    private final IpFilterRuleType ipFilterRuleTypeIPv4;
    /** IPv6 规则是否全部为 ACCEPT 或全部为 REJECT；混合类型时为 null。 */
    private final IpFilterRuleType ipFilterRuleTypeIPv6;

    /**
     * <p> Create new {@link IpSubnetFilter} Instance with specified {@link IpSubnetFilterRule} as array. </p>
     * <p> {@code acceptIfNotFound} is set to {@code true}. </p>
     *
     * @param rules {@link IpSubnetFilterRule} as an array
     *
     * <p>未命中时默认接受。</p>
     */
    public IpSubnetFilter(IpSubnetFilterRule... rules) {
        this(true, Arrays.asList(ObjectUtil.checkNotNull(rules, "rules")));
    }

    /**
     * <p> Create new {@link IpSubnetFilter} Instance with specified {@link IpSubnetFilterRule} as array
     * and specify if we'll accept a connection if we don't find it in the rule(s). </p>
     *
     * @param acceptIfNotFound {@code true} if we'll accept connection if not found in rule(s).
     * @param rules            {@link IpSubnetFilterRule} as an array
     */
    public IpSubnetFilter(boolean acceptIfNotFound, IpSubnetFilterRule... rules) {
        this(acceptIfNotFound, Arrays.asList(ObjectUtil.checkNotNull(rules, "rules")));
    }

    /**
     * <p> Create new {@link IpSubnetFilter} Instance with specified {@link IpSubnetFilterRule} as {@link List}. </p>
     * <p> {@code acceptIfNotFound} is set to {@code true}. </p>
     *
     * @param rules {@link IpSubnetFilterRule} as a {@link List}
     */
    public IpSubnetFilter(List<IpSubnetFilterRule> rules) {
        this(true, rules);
    }

    /**
     * <p> Create new {@link IpSubnetFilter} Instance with specified {@link IpSubnetFilterRule} as {@link List}
     * and specify if we'll accept a connection if we don't find it in the rule(s). </p>
     *
     * @param acceptIfNotFound {@code true} if we'll accept connection if not found in rule(s).
     * @param rules            {@link IpSubnetFilterRule} as a {@link List}
     *
     * <p>按 IP 版本分组、统计规则类型、排序并去除被父网段覆盖的子规则。</p>
     */
    public IpSubnetFilter(boolean acceptIfNotFound, List<IpSubnetFilterRule> rules) {
        ObjectUtil.checkNotNull(rules, "rules");
        this.acceptIfNotFound = acceptIfNotFound;

        int numAcceptIPv4 = 0;
        int numRejectIPv4 = 0;
        int numAcceptIPv6 = 0;
        int numRejectIPv6 = 0;

        List<IpSubnetFilterRule> unsortedIPv4Rules = new ArrayList<IpSubnetFilterRule>();
        List<IpSubnetFilterRule> unsortedIPv6Rules = new ArrayList<IpSubnetFilterRule>();

        // Iterate over rules and check for `null` rule.
        // 遍历规则并按 IPv4/IPv6 分组，同时统计 ACCEPT/REJECT 数量
        for (IpSubnetFilterRule ipSubnetFilterRule : rules) {
            ObjectUtil.checkNotNull(ipSubnetFilterRule, "rule");

            if (ipSubnetFilterRule.getFilterRule() instanceof IpSubnetFilterRule.Ip4SubnetFilterRule) {
                unsortedIPv4Rules.add(ipSubnetFilterRule);

                if (ipSubnetFilterRule.ruleType() == IpFilterRuleType.ACCEPT) {
                    numAcceptIPv4++;
                } else {
                    numRejectIPv4++;
                }
            } else {
                unsortedIPv6Rules.add(ipSubnetFilterRule);

                if (ipSubnetFilterRule.ruleType() == IpFilterRuleType.ACCEPT) {
                    numAcceptIPv6++;
                } else {
                    numRejectIPv6++;
                }
            }
        }

        /*
         * If Number of ACCEPT rule is 0 and number of REJECT rules is more than 0,
         * then all rules are of "REJECT" type.
         *
         * In this case, we'll set `ipFilterRuleTypeIPv4` to `IpFilterRuleType.REJECT`.
         *
         * If Number of ACCEPT rules are more than 0 and number of REJECT rules is 0,
         * then all rules are of "ACCEPT" type.
         *
         * In this case, we'll set `ipFilterRuleTypeIPv4` to `IpFilterRuleType.ACCEPT`.
         *
         * 若规则类型单一，可用统一类型快速判定；混合 ACCEPT/REJECT 时置 null，逐条读取 ruleType。
         */
        if (numAcceptIPv4 == 0 && numRejectIPv4 > 0) {
            ipFilterRuleTypeIPv4 = IpFilterRuleType.REJECT;
        } else if (numAcceptIPv4 > 0 && numRejectIPv4 == 0) {
            ipFilterRuleTypeIPv4 = IpFilterRuleType.ACCEPT;
        } else {
            ipFilterRuleTypeIPv4 = null;
        }

        if (numAcceptIPv6 == 0 && numRejectIPv6 > 0) {
            ipFilterRuleTypeIPv6 = IpFilterRuleType.REJECT;
        } else if (numAcceptIPv6 > 0 && numRejectIPv6 == 0) {
            ipFilterRuleTypeIPv6 = IpFilterRuleType.ACCEPT;
        } else {
            ipFilterRuleTypeIPv6 = null;
        }

        this.ipv4Rules = unsortedIPv4Rules.isEmpty() ? null : sortAndFilter(unsortedIPv4Rules);
        this.ipv6Rules = unsortedIPv6Rules.isEmpty() ? null : sortAndFilter(unsortedIPv6Rules);
    }

    @Override
    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        if (ipv4Rules != null && remoteAddress.getAddress() instanceof Inet4Address) {
            int indexOf = Arrays.binarySearch(ipv4Rules, remoteAddress, IpSubnetFilterRuleComparator.INSTANCE);
            if (indexOf >= 0) {
                if (ipFilterRuleTypeIPv4 == null) {
                    return ipv4Rules[indexOf].ruleType() == IpFilterRuleType.ACCEPT;
                } else {
                    return ipFilterRuleTypeIPv4 == IpFilterRuleType.ACCEPT;
                }
            }
        } else if (ipv6Rules != null && remoteAddress.getAddress() instanceof Inet6Address) {
            int indexOf = Arrays.binarySearch(ipv6Rules, remoteAddress, IpSubnetFilterRuleComparator.INSTANCE);
            if (indexOf >= 0) {
                if (ipFilterRuleTypeIPv6 == null) {
                    return ipv6Rules[indexOf].ruleType() == IpFilterRuleType.ACCEPT;
                } else {
                    return ipFilterRuleTypeIPv6 == IpFilterRuleType.ACCEPT;
                }
            }
        }

        return acceptIfNotFound;
    }

    /**
     * <ol>
     *     <li> Sort the list </li>
     *     <li> Remove over-lapping subnet </li>
     *     <li> Sort the list again </li>
     * </ol>
     *
     * <p>排序后剔除被更大子网完全覆盖的规则，返回数组供二分查找。</p>
     */
    @SuppressWarnings("ZeroLengthArrayAllocation")
    private static IpSubnetFilterRule[] sortAndFilter(List<IpSubnetFilterRule> rules) {
        Collections.sort(rules);
        Iterator<IpSubnetFilterRule> iterator = rules.iterator();
        List<IpSubnetFilterRule> toKeep = new ArrayList<IpSubnetFilterRule>();

        IpSubnetFilterRule parentRule = iterator.hasNext() ? iterator.next() : null;
        if (parentRule != null) {
            toKeep.add(parentRule);
        }

        while (iterator.hasNext()) {

            // Grab a potential child rule.
            // 若当前规则已被 parentRule 网段包含，则丢弃（重叠子网）
            IpSubnetFilterRule childRule = iterator.next();

            // If parentRule matches childRule, then there's no need to keep the child rule.
            // Otherwise, the rules are distinct and we need both.
            if (!parentRule.matches(new InetSocketAddress(childRule.getIpAddress(), 1))) {
                toKeep.add(childRule);
                // Then we'll keep the child rule around as the parent for the next round.
                parentRule = childRule;
            }
        }

        return toKeep.toArray(new IpSubnetFilterRule[0]);
    }
}
