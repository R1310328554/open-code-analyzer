/*
 * Copyright 2016 The Netty Project
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
package io.netty.handler.codec.dns;

import java.net.InetAddress;

/**
 * EDNS(0) 客户端子网（ECS）选项记录，定义见
 * <a href="https://tools.ietf.org/html/rfc7871#section-6">Client Subnet in DNS Queries</a>。
 * <p>
 * 携带客户端 IP 前缀，供权威服务器按地理位置返回更优解析结果。
 */
public interface DnsOptEcsRecord extends DnsOptPseudoRecord {

    /** 返回用于查找的 ADDRESS 最左侧有效位数（源前缀长度）。 */
    int sourcePrefixLength();

    /** 返回响应所覆盖的 ADDRESS 最左侧有效位数；查询中必须为 0。 */
    int scopePrefixLength();

    /** 返回 {@link InetAddress} 的字节表示（IPv4 为 4 字节，IPv6 为 16 字节）。 */
    byte[] address();
}
