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
 * DNS 响应报文接口，QR 位为 1 表示服务端返回的应答。
 * <p>
 * 在 {@link DnsMessage} 基础上扩展 AA/TC/RA 标志与 {@link DnsResponseCode}。
 */
public interface DnsResponse extends DnsMessage {

    /**
     * 响应服务器是否对该域名具有权威（{@code AA} 标志）。
     */
    boolean isAuthoritativeAnswer();

    /**
     * 设置权威应答（{@code AA}）标志。
     *
     * @param authoritativeAnswer 是否为权威应答
     */
    DnsResponse setAuthoritativeAnswer(boolean authoritativeAnswer);

    /**
     * 响应是否被截断（{@code TC} 标志，UDP 超过 512 字节时常见）。
     */
    boolean isTruncated();

    /**
     * 设置截断（{@code TC}）标志。
     *
     * @param truncated 是否截断
     */
    DnsResponse setTruncated(boolean truncated);

    /**
     * 服务器是否支持递归查询（{@code RA} 标志）。
     */
    boolean isRecursionAvailable();

    /**
     * 设置递归可用（{@code RA}）标志。
     *
     * @param recursionAvailable 是否支持递归
     */
    DnsResponse setRecursionAvailable(boolean recursionAvailable);

    /** 返回 4 位响应码 {@code RCODE}。 */
    DnsResponseCode code();

    /**
     * 设置响应码。
     *
     * @param code 响应码
     */
    DnsResponse setCode(DnsResponseCode code);

    @Override
    DnsResponse setId(int id);

    @Override
    DnsResponse setOpCode(DnsOpCode opCode);

    @Override
    DnsResponse setRecursionDesired(boolean recursionDesired);

    @Override
    DnsResponse setZ(int z);

    @Override
    DnsResponse setRecord(DnsSection section, DnsRecord record);

    @Override
    DnsResponse addRecord(DnsSection section, DnsRecord record);

    @Override
    DnsResponse addRecord(DnsSection section, int index, DnsRecord record);

    @Override
    DnsResponse clear(DnsSection section);

    @Override
    DnsResponse clear();

    @Override
    DnsResponse touch();

    @Override
    DnsResponse touch(Object hint);

    @Override
    DnsResponse retain();

    @Override
    DnsResponse retain(int increment);
}
