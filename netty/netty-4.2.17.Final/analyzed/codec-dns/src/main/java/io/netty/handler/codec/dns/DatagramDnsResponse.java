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

import io.netty.channel.AddressedEnvelope;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * 基于 UDP/IP 的 {@link DnsResponse} 实现，携带发送方与接收方地址。
 */
public class DatagramDnsResponse extends DefaultDnsResponse
        implements AddressedEnvelope<DatagramDnsResponse, InetSocketAddress> {

    private final InetSocketAddress sender;
    private final InetSocketAddress recipient;

    /**
     * 使用 {@link DnsOpCode#QUERY} 与 {@link DnsResponseCode#NOERROR} 创建实例。
     *
     * @param sender 发送方地址
     * @param recipient 接收方地址
     * @param id DNS 响应 {@code ID}
     */
    public DatagramDnsResponse(InetSocketAddress sender, InetSocketAddress recipient, int id) {
        this(sender, recipient, id, DnsOpCode.QUERY, DnsResponseCode.NOERROR);
    }

    /**
     * 使用 {@link DnsResponseCode#NOERROR} 响应码创建实例。
     *
     * @param sender 发送方地址
     * @param recipient 接收方地址
     * @param id DNS 响应 {@code ID}
     * @param opCode DNS 响应操作码
     */
    public DatagramDnsResponse(InetSocketAddress sender, InetSocketAddress recipient, int id, DnsOpCode opCode) {
        this(sender, recipient, id, opCode, DnsResponseCode.NOERROR);
    }

    /**
     * 创建完整配置的实例。
     *
     * @param sender 发送方地址
     * @param recipient 接收方地址
     * @param id DNS 响应 {@code ID}
     * @param opCode DNS 响应操作码
     * @param responseCode DNS 响应 {@code RCODE}
     */
    public DatagramDnsResponse(
            InetSocketAddress sender, InetSocketAddress recipient,
            int id, DnsOpCode opCode, DnsResponseCode responseCode) {
        super(id, opCode, responseCode);

        if (recipient == null && sender == null) {
            throw new NullPointerException("recipient and sender");
        }

        this.sender = sender;
        this.recipient = recipient;
    }

    @Override
    public DatagramDnsResponse content() {
        return this;
    }

    @Override
    public InetSocketAddress sender() {
        return sender;
    }

    @Override
    public InetSocketAddress recipient() {
        return recipient;
    }

    @Override
    public DatagramDnsResponse setAuthoritativeAnswer(boolean authoritativeAnswer) {
        return (DatagramDnsResponse) super.setAuthoritativeAnswer(authoritativeAnswer);
    }

    @Override
    public DatagramDnsResponse setTruncated(boolean truncated) {
        return (DatagramDnsResponse) super.setTruncated(truncated);
    }

    @Override
    public DatagramDnsResponse setRecursionAvailable(boolean recursionAvailable) {
        return (DatagramDnsResponse) super.setRecursionAvailable(recursionAvailable);
    }

    @Override
    public DatagramDnsResponse setCode(DnsResponseCode code) {
        return (DatagramDnsResponse) super.setCode(code);
    }

    @Override
    public DatagramDnsResponse setId(int id) {
        return (DatagramDnsResponse) super.setId(id);
    }

    @Override
    public DatagramDnsResponse setOpCode(DnsOpCode opCode) {
        return (DatagramDnsResponse) super.setOpCode(opCode);
    }

    @Override
    public DatagramDnsResponse setRecursionDesired(boolean recursionDesired) {
        return (DatagramDnsResponse) super.setRecursionDesired(recursionDesired);
    }

    @Override
    public DatagramDnsResponse setZ(int z) {
        return (DatagramDnsResponse) super.setZ(z);
    }

    @Override
    public DatagramDnsResponse setRecord(DnsSection section, DnsRecord record) {
        return (DatagramDnsResponse) super.setRecord(section, record);
    }

    @Override
    public DatagramDnsResponse addRecord(DnsSection section, DnsRecord record) {
        return (DatagramDnsResponse) super.addRecord(section, record);
    }

    @Override
    public DatagramDnsResponse addRecord(DnsSection section, int index, DnsRecord record) {
        return (DatagramDnsResponse) super.addRecord(section, index, record);
    }

    @Override
    public DatagramDnsResponse clear(DnsSection section) {
        return (DatagramDnsResponse) super.clear(section);
    }

    @Override
    public DatagramDnsResponse clear() {
        return (DatagramDnsResponse) super.clear();
    }

    @Override
    public DatagramDnsResponse touch() {
        return (DatagramDnsResponse) super.touch();
    }

    @Override
    public DatagramDnsResponse touch(Object hint) {
        return (DatagramDnsResponse) super.touch(hint);
    }

    @Override
    public DatagramDnsResponse retain() {
        return (DatagramDnsResponse) super.retain();
    }

    @Override
    public DatagramDnsResponse retain(int increment) {
        return (DatagramDnsResponse) super.retain(increment);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!super.equals(obj)) {
            return false;
        }

        if (!(obj instanceof AddressedEnvelope)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final AddressedEnvelope<?, SocketAddress> that = (AddressedEnvelope<?, SocketAddress>) obj;
        if (sender() == null) {
            if (that.sender() != null) {
                return false;
            }
        } else if (!sender().equals(that.sender())) {
            return false;
        }

        if (recipient() == null) {
            return that.recipient() == null;
        } else {
            return recipient().equals(that.recipient());
        }
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        if (sender() != null) {
            hashCode = hashCode * 31 + sender().hashCode();
        }
        if (recipient() != null) {
            hashCode = hashCode * 31 + recipient().hashCode();
        }
        return hashCode;
    }
}
