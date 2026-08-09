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
package io.netty.handler.codec.dns;

/**
 * {@link DnsMessage} 的 section（区段）枚举。
 * <p>
 * DNS 报文按 RFC 1035 分为问题、应答、权威、附加四个 section。
 */
public enum DnsSection {
    /** 问题段，包含 {@link DnsQuestion}。 */
    QUESTION,
    /** 应答段，包含应答 {@link DnsRecord}。 */
    ANSWER,
    /** 权威段，包含权威服务器 {@link DnsRecord}。 */
    AUTHORITY,
    /** 附加段，包含额外 {@link DnsRecord}（如 EDNS OPT）。 */
    ADDITIONAL
}
