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

import io.netty.buffer.ByteBuf;

/**
 * 将 DNS 记录从二进制格式解码为 Java 对象。
 *
 * @see DatagramDnsResponseDecoder
 */
public interface DnsRecordDecoder {

    /** 默认解码器实例。 */
    DnsRecordDecoder DEFAULT = new DefaultDnsRecordDecoder();

    /**
     * 将 DNS 问题段解码为 {@link DnsQuestion}。
     *
     * @param in 读指针处含一条 DNS 问题的输入缓冲区
     */
    DnsQuestion decodeQuestion(ByteBuf in) throws Exception;

    /**
     * 将 DNS 资源记录解码为 {@link DnsRecord} 子类型。
     *
     * @param in 读指针处含一条 DNS 记录的输入缓冲区
     *
     * @return 解码后的记录；输入数据不足时返回 {@code null}
     */
    <T extends DnsRecord> T decodeRecord(ByteBuf in) throws Exception;
}
