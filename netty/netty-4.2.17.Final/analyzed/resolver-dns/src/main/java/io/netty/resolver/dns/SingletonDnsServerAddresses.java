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

/**
 * 仅包含一个 DNS 服务器地址的 {@link DnsServerAddresses} 实现。
 * <p>{@link #stream()} 每次 {@link DnsServerAddressStream#next()} 均返回同一地址。</p>
 */
final class SingletonDnsServerAddresses extends DnsServerAddresses {

    /** 唯一的 DNS 服务器地址。 */
    private final InetSocketAddress address;

    /** 始终返回 {@link #address} 的单元素流。 */
    private final DnsServerAddressStream stream = new DnsServerAddressStream() {
        @Override
        public InetSocketAddress next() {
            return address;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public DnsServerAddressStream duplicate() {
            return this;
        }

        @Override
        public String toString() {
            return SingletonDnsServerAddresses.this.toString();
        }
    };

    SingletonDnsServerAddresses(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public DnsServerAddressStream stream() {
        return stream;
    }

    @Override
    public String toString() {
        return "singleton(" + address + ")";
    }
}
