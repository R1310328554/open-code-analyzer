/*
 * Copyright 2015 The Netty Project
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

package io.netty.resolver.dns;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 轮转式 DNS 服务器地址集合：每次 {@link #stream()} 从不同起始下标顺序遍历。
 * <p>对应 resolv.conf 中 {@code options rotate} 的语义。</p>
 */
final class RotationalDnsServerAddresses extends DefaultDnsServerAddresses {

    private static final AtomicIntegerFieldUpdater<RotationalDnsServerAddresses> startIdxUpdater =
            AtomicIntegerFieldUpdater.newUpdater(RotationalDnsServerAddresses.class, "startIdx");

    /** 下次 stream 的起始下标，CAS 递增实现轮转。 */
    @SuppressWarnings("UnusedDeclaration")
    private volatile int startIdx;

    RotationalDnsServerAddresses(List<InetSocketAddress> addresses) {
        super("rotational", addresses);
    }

    @Override
    public DnsServerAddressStream stream() {
        for (;;) {
            int curStartIdx = startIdx;
            int nextStartIdx = curStartIdx + 1;
            if (nextStartIdx >= addresses.size()) {
                nextStartIdx = 0;
            }
            if (startIdxUpdater.compareAndSet(this, curStartIdx, nextStartIdx)) {
                return new SequentialDnsServerAddressStream(addresses, curStartIdx);
            }
        }
    }
}
