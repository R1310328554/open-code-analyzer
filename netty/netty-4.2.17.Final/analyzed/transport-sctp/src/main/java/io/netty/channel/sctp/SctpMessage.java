/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.sctp;

import com.sun.nio.sctp.MessageInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.util.internal.ObjectUtil;

/**
 * Representation of SCTP Data Chunk
 * <p>封装 SCTP 数据块：PPID、流 ID、无序标志与 {@link ByteBuf} 负载； 入站可附带 JDK {@link MessageInfo}。</p>
 */
public final class SctpMessage extends DefaultByteBufHolder {
    /** SCTP 流编号（0 .. maxOutboundStreams-1） */
    private final int streamIdentifier;
    /** 载荷协议标识 PPID，区分上层协议 */
    private final int protocolIdentifier;
    /** 是否以无序（U 标志）方式发送 */
    private final boolean unordered;

    /** 入站消息的 JDK 元数据；出站为 {@code null} */
    private final MessageInfo msgInfo;

    /**
     * Essential data that is being carried within SCTP Data Chunk
     * @param protocolIdentifier of payload
     * @param streamIdentifier that you want to send the payload
     * @param payloadBuffer channel buffer
     * <p>构造有序出站消息（PPID + 流 ID + 负载）。</p>
     */
    public SctpMessage(int protocolIdentifier, int streamIdentifier, ByteBuf payloadBuffer) {
        this(protocolIdentifier, streamIdentifier, false, payloadBuffer);
    }

    /**
     * Essential data that is being carried within SCTP Data Chunk
     * @param protocolIdentifier of payload
     * @param streamIdentifier that you want to send the payload
     * @param unordered if {@literal true}, the SCTP Data Chunk will be sent with the U (unordered) flag set.
     * @param payloadBuffer channel buffer
     * <p>可指定无序标志的出站构造。</p>
     */
    public SctpMessage(int protocolIdentifier, int streamIdentifier, boolean unordered, ByteBuf payloadBuffer) {
        super(payloadBuffer);
        this.protocolIdentifier = protocolIdentifier;
        this.streamIdentifier = streamIdentifier;
        this.unordered = unordered;
        msgInfo = null;
    }

    /**
     * Essential data that is being carried within SCTP Data Chunk
     * @param msgInfo       the {@link MessageInfo}
     * @param payloadBuffer channel buffer
     * <p>由 {@link MessageInfo} 填充流/PPID/无序标志的入站构造。</p>
     */
    public SctpMessage(MessageInfo msgInfo, ByteBuf payloadBuffer) {
        super(payloadBuffer);
        this.msgInfo = ObjectUtil.checkNotNull(msgInfo, "msgInfo");
        this.streamIdentifier = msgInfo.streamNumber();
        this.protocolIdentifier = msgInfo.payloadProtocolID();
        this.unordered = msgInfo.isUnordered();
    }

    /**
     * Return the stream-identifier
     * <p>返回 SCTP 流 ID。</p>
     */
    public int streamIdentifier() {
        return streamIdentifier;
    }

    /**
     * Return the protocol-identifier
     * <p>返回 PPID。</p>
     */
    public int protocolIdentifier() {
        return protocolIdentifier;
    }

    /**
     * return the unordered flag
     * <p>是否无序发送。</p>
     */
    public boolean isUnordered() {
        return unordered;
    }

    /**
     * Return the {@link MessageInfo} for inbound messages or {@code null} for
     * outbound messages.
     * <p>入站附带 JDK 元数据，出站为 {@code null}。</p>
     */
    public MessageInfo messageInfo() {
        return msgInfo;
    }

    /**
     * Return {@code true} if this message is complete.
     * <p>分片消息是否已收齐；出站恒为 {@code true}。</p>
     */
    public boolean isComplete() {
        if (msgInfo != null) {
            return msgInfo.isComplete();
        }  else {
            // 出站 SCTP 消息视为完整单块
            return true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SctpMessage sctpFrame = (SctpMessage) o;

        if (protocolIdentifier != sctpFrame.protocolIdentifier) {
            return false;
        }

        if (streamIdentifier != sctpFrame.streamIdentifier) {
            return false;
        }

        if (unordered != sctpFrame.unordered) {
            return false;
        }

        return content().equals(sctpFrame.content());
    }

    @Override
    public int hashCode() {
        int result = streamIdentifier;
        result = 31 * result + protocolIdentifier;
        // 1231/1237 与 Boolean#hashCode() 文档约定一致
        result = 31 * result + (unordered ? 1231 : 1237);
        result = 31 * result + content().hashCode();
        return result;
    }

    @Override
    public SctpMessage copy() {
        return (SctpMessage) super.copy();
    }

    @Override
    public SctpMessage duplicate() {
        return (SctpMessage) super.duplicate();
    }

    @Override
    public SctpMessage retainedDuplicate() {
        return (SctpMessage) super.retainedDuplicate();
    }

    @Override
    public SctpMessage replace(ByteBuf content) {
        if (msgInfo == null) {
            return new SctpMessage(protocolIdentifier, streamIdentifier, unordered, content);
        } else {
            return new SctpMessage(msgInfo, content);
        }
    }

    @Override
    public SctpMessage retain() {
        super.retain();
        return this;
    }

    @Override
    public SctpMessage retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public SctpMessage touch() {
        super.touch();
        return this;
    }

    @Override
    public SctpMessage touch(Object hint) {
        super.touch(hint);
        return this;
    }

    @Override
    public String toString() {
        return "SctpFrame{" +
               "streamIdentifier=" + streamIdentifier + ", protocolIdentifier=" + protocolIdentifier +
               ", unordered=" + unordered +
               ", data=" + contentToString() + '}';
    }
}
