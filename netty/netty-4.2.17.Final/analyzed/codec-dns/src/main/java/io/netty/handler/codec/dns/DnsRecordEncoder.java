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
 * 将 {@link DnsRecord} 编码为 DNS 二进制格式。
 *
 * @see DatagramDnsQueryEncoder
 */
public interface DnsRecordEncoder {

    /** 默认编码器实例。 */
    DnsRecordEncoder DEFAULT = new DefaultDnsRecordEncoder();

    /**
     * 编码 {@link DnsQuestion}。
     *
     * @param out 写入编码后问题段的输出缓冲区
     */
    void encodeQuestion(DnsQuestion question, ByteBuf out) throws Exception;

    /**
     * 编码 {@link DnsRecord}。
     *
     * @param out 写入编码后资源记录的输出缓冲区
     */
    void encodeRecord(DnsRecord record, ByteBuf out) throws Exception;
}
