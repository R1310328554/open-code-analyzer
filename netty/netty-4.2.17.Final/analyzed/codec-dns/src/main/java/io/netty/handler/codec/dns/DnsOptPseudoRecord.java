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

/**
 * <a href="https://tools.ietf.org/html/rfc6891#section-6.1">OPT RR</a> 伪记录接口。
 * <p>
 * 用于 <a href="https://tools.ietf.org/html/rfc6891#section-6.1.3">EDNS(0)</a> 扩展机制，
 * 将扩展 RCODE、版本、标志及 UDP 载荷大小编码进 TTL 与类字段。
 */
public interface DnsOptPseudoRecord extends DnsRecord {

    /** 返回编码在 {@link DnsOptPseudoRecord#timeToLive()} 高 8 位中的扩展 RCODE。 */
    int extendedRcode();

    /** 返回编码在 {@link DnsOptPseudoRecord#timeToLive()} 中的 EDNS 版本号。 */
    int version();

    /** 返回编码在 {@link DnsOptPseudoRecord#timeToLive()} 低 16 位中的标志（含 DO 与 Z）。 */
    int flags();
}
