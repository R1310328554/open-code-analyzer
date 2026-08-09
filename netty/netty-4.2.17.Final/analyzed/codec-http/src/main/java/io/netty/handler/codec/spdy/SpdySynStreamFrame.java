/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.spdy;

/**
 * SPDY SYN_STREAM 帧：在连接上创建新流并携带请求/推送头块。
 * <p>除 {@link SpdyHeadersFrame} 的 stream ID 与头域外，还包含关联流、优先级与单向标志，
 * 用于多路复用下的流调度与 server push 语义。
 */
public interface SpdySynStreamFrame extends SpdyHeadersFrame {

    /**
     * 返回关联流 ID（Associated-To-Stream-ID）。
     * <p>值为 0 表示独立新流；非零时本流与指定流关联（如 server push）。
     */
    int associatedStreamId();

    /**
     * 设置关联流 ID，不可为负数。
     */
    SpdySynStreamFrame setAssociatedStreamId(int associatedStreamId);

    /**
     * 返回流优先级，范围 0（最高）到 7（最低）。
     */
    byte priority();

    /**
     * 设置流优先级，必须在 0–7 之间（含端点）。
     */
    SpdySynStreamFrame setPriority(byte priority);

    /**
     * 若创建后流对接收方呈 half-closed（仅发送方可继续写 DATA），返回 {@code true}。
     * <p>用于 server push 等单向场景。
     */
    boolean isUnidirectional();

    /**
     * 设置新流是否对接收方 half-closed。
     */
    SpdySynStreamFrame setUnidirectional(boolean unidirectional);

    @Override
    SpdySynStreamFrame setStreamId(int streamID);

    @Override
    SpdySynStreamFrame setLast(boolean last);

    @Override
    SpdySynStreamFrame setInvalid();
}
