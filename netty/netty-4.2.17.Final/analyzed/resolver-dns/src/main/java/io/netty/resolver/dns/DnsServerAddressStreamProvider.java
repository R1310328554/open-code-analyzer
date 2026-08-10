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

/**
 * 为指定主机名选择 {@link DnsServerAddressStream} 的扩展点。
 * <p>
 * 可用于映射 <a href="https://linux.die.net/man/5/resolver">/etc/resolv.conf</a> 与
 * <a href="https://developer.apple.com/legacy/library/documentation/Darwin/Reference/ManPages/man5/resolver.5.html">
 * /etc/resolver</a> 等系统配置。
 */
public interface DnsServerAddressStreamProvider {
    /**
     * 返回解析 {@code hostname} 时应使用的 nameserver 地址流。
     * @param hostname The hostname for which to lookup the DNS server addressed to use.
     *                 If this is the final {@link DnsServerAddressStreamProvider} to be queried then generally empty
     *                 string or {@code '.'} correspond to the default {@link DnsServerAddressStream}.
     * @return The {@link DnsServerAddressStream} which should be used to resolve {@code hostname}.
     */
    DnsServerAddressStream nameServerAddressStream(String hostname);
}
