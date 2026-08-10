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
package com.sun.nio.sctp;

import java.net.SocketAddress;

/**
 * SCTP 消息元数据：流号、PPID、有序/完整标志及目标地址等。
 * <p>出站由 {@link #createOutgoing} 构造；入站由 {@link SctpChannel#receive} 填充。 与 {@link java.nio.ByteBuffer} 负载一并传递给 {@link SctpChannel#send}。</p>
 */
public abstract class MessageInfo {
    /** 非 SCTP 平台 stub 探测 */
    static {
        UnsupportedOperatingSystemException.raise();
    }

    /** 构造出站消息描述（关联、对端地址、流 ID） */
    public static MessageInfo createOutgoing(Association association, SocketAddress address, int streamNumber) {
        return null;
    }

    /** 消息关联的对端或目标 {@link SocketAddress} */
    public abstract SocketAddress address();
    /**  SCTP 流编号（0 .. maxOutboundStreams-1） */
    public abstract int streamNumber();
    /** 链式设置流编号 */
    public abstract MessageInfo streamNumber(int streamNumber);
    /** 返回载荷协议标识 PPID */
    public abstract int payloadProtocolID();
    /** 链式设置 PPID（区分上层协议，如 WebRTC/DataChannel） */
    public abstract MessageInfo payloadProtocolID(int ppid);
    /** 是否为分片消息的最后一片 */
    public abstract boolean isComplete();
    /** 是否以无序（UNORDERED）方式发送 */
    public abstract boolean isUnordered();
    /** 链式设置无序标志 */
    public abstract MessageInfo unordered(boolean b);

}
