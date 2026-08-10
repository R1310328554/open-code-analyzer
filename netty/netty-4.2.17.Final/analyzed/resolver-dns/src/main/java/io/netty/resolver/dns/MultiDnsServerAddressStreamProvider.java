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

import java.util.List;

/**
 * 组合多个 {@link DnsServerAddressStreamProvider}，按顺序查询直至首个非 {@code null} 结果。
 */
public final class MultiDnsServerAddressStreamProvider implements DnsServerAddressStreamProvider {
    private final DnsServerAddressStreamProvider[] providers;

    /**
     * 使用给定 provider 列表构造实例；按列表顺序依次查询。
     * @param providers The providers to use for DNS resolution. They will be queried in order.
     */
    public MultiDnsServerAddressStreamProvider(List<DnsServerAddressStreamProvider> providers) {
        this.providers = providers.toArray(new DnsServerAddressStreamProvider[0]);
    }

    /**
     * 使用给定 provider 数组构造实例；按数组顺序依次查询。
     * @param providers The providers to use for DNS resolution. They will be queried in order.
     */
    public MultiDnsServerAddressStreamProvider(DnsServerAddressStreamProvider... providers) {
        this.providers = providers.clone();
    }

    @Override
    public DnsServerAddressStream nameServerAddressStream(String hostname) {
        // 依次委托各 provider，返回第一个非 null 的地址流
        for (DnsServerAddressStreamProvider provider : providers) {
            DnsServerAddressStream stream = provider.nameServerAddressStream(hostname);
            if (stream != null) {
                return stream;
            }
        }
        return null;
    }
}
