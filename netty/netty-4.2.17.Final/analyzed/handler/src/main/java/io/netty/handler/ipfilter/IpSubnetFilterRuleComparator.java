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

import java.net.InetSocketAddress;
import java.util.Comparator;

/**
 * This comparator is only used for searching.
 *
 * <p>供 {@link java.util.Arrays#binarySearch} 在子网规则数组中查找远端地址时使用。</p>
 */
final class IpSubnetFilterRuleComparator implements Comparator<Object> {

    /** 单例比较器实例。 */
    static final IpSubnetFilterRuleComparator INSTANCE = new IpSubnetFilterRuleComparator();

    private IpSubnetFilterRuleComparator() {
        // Prevent outside initialization
        // 禁止外部实例化
    }

    @Override
    public int compare(Object o1, Object o2) {
        // o1 为 IpSubnetFilterRule，o2 为待查找的 InetSocketAddress
        return ((IpSubnetFilterRule) o1).compareTo((InetSocketAddress) o2);
    }
}
