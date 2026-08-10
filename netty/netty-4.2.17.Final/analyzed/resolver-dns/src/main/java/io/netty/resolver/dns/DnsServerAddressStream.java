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
 * 无限循环的 DNS 服务器地址流。
 * <p>每次解析可从中依次取出 nameserver 地址，耗尽后按实现策略重复或终止。</p>
 */
public interface DnsServerAddressStream {
    /**
     * 从流中取出下一个 DNS 服务器地址。
     */
    InetSocketAddress next();

    /**
     * 返回在重复或终止前，{@link #next()} 能返回的不同元素个数。
     * @return the number of times {@link #next()} will return a distinct element before repeating or terminating.
     */
    int size();

    /**
     * 复制本对象，副本可独立通过 {@link #next()} 迭代。
     * <p>
     * 不使用 {@link #clone()}，因部分实现可能满足 {@code x.duplicate() == x}。
     * @return A duplicate of this object.
     */
    DnsServerAddressStream duplicate();
}
