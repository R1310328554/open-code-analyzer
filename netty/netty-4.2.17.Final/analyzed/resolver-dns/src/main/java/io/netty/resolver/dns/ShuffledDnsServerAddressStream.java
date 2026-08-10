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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机打乱顺序后循环返回 DNS 服务器地址的 {@link DnsServerAddressStream} 实现。
 * <p>每轮遍历结束后重新洗牌，降低对固定顺序的依赖。</p>
 */
final class ShuffledDnsServerAddressStream implements DnsServerAddressStream {

    /** 可修改的地址列表，用于就地洗牌。 */
    private final List<InetSocketAddress> addresses;
    /** 当前轮次中的读取下标。 */
    private int i;

    /**
     * 创建新实例。
     * @param addresses 地址列表不会被克隆；调用方应已克隆或保证不再修改其内容
     */
    ShuffledDnsServerAddressStream(List<InetSocketAddress> addresses) {
        this.addresses = addresses;

        shuffle();
    }

    private ShuffledDnsServerAddressStream(List<InetSocketAddress> addresses, int startIdx) {
        this.addresses = addresses;
        i = startIdx;
    }

    /** 使用线程本地随机源打乱地址顺序。 */
    private void shuffle() {
        Collections.shuffle(addresses, ThreadLocalRandom.current());
    }

    @Override
    public InetSocketAddress next() {
        int i = this.i;
        InetSocketAddress next = addresses.get(i);
        if (++ i < addresses.size()) {
            this.i = i;
        } else {
            this.i = 0;
            shuffle();
        }
        return next;
    }

    @Override
    public int size() {
        return addresses.size();
    }

    @Override
    public ShuffledDnsServerAddressStream duplicate() {
        return new ShuffledDnsServerAddressStream(addresses, i);
    }

    @Override
    public String toString() {
        return SequentialDnsServerAddressStream.toString("shuffled", i, addresses);
    }
}
