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

import java.net.InetAddress;

/**
 * 可缓存的 DNS 查询结果条目。
 * <p>成功时 {@link #address()} 非空；失败时 {@link #cause()} 描述原因且地址为 null。</p>
 */
public interface DnsCacheEntry {
    /**
     * 获取已解析的 IP 地址。
     * <p>
     * This may be null if the resolution failed, and in that case {@link #cause()} will describe the failure.
     * @return the resolved address.
     */
    InetAddress address();

    /**
     * 若 DNS 查询失败，返回失败原因。
     * @return the rational for why the DNS query failed, or {@code null} if the query hasn't failed.
     */
    Throwable cause();
}
