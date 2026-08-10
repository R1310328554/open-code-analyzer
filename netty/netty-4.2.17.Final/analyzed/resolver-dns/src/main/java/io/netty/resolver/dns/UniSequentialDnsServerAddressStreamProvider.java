/*
 * Copyright 2017 The Netty Project
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

import io.netty.util.internal.ObjectUtil;

/**
 * 由单一 {@link DnsServerAddresses} 支撑的 {@link DnsServerAddressStreamProvider} 抽象基类。
 * <p>忽略 hostname，对所有查询返回同一地址流策略。</p>
 */
abstract class UniSequentialDnsServerAddressStreamProvider implements DnsServerAddressStreamProvider {
    /** 固定的 DNS 服务器地址集合策略。 */
    private final DnsServerAddresses addresses;

    UniSequentialDnsServerAddressStreamProvider(DnsServerAddresses addresses) {
        this.addresses = ObjectUtil.checkNotNull(addresses, "addresses");
    }

    @Override
    public final DnsServerAddressStream nameServerAddressStream(String hostname) {
        return addresses.stream();
    }
}
