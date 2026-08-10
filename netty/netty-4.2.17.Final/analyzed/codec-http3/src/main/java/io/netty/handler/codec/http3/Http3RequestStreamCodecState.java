/*
 * Copyright 2021 The Netty Project
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
package io.netty.handler.codec.http3;

/**
 * State of encoding or decoding for a stream following the <a
 * href="https://quicwg.org/base-drafts/draft-ietf-quic-http.html#name-http-message-exchanges">
 * HTTP message exchange semantics</a>
 * <p>跟踪单条请求/响应流上 HEADERS/DATA 的交换阶段，供 QPACK「流依赖」与
 * {@link Http3RequestStreamValidationUtils} 判断头部块是否完整、流是否已终止。
 */
interface Http3RequestStreamCodecState {
    /**
     * An implementation of {@link Http3RequestStreamCodecState} that managed no state.
     * <p>占位实现：始终返回「未开始/未收最终头/未终止」，用于不需要 QPACK 流状态的一侧（如 push 流出站）。
     */
    Http3RequestStreamCodecState NO_STATE = new Http3RequestStreamCodecState() {
        @Override
        public boolean started() {
            return false;
        }

        @Override
        public boolean receivedFinalHeaders() {
            return false;
        }

        @Override
        public boolean terminated() {
            return false;
        }
    };

    /**
     * If any {@link Http3HeadersFrame} or {@link Http3DataFrame} has been received/sent on this stream.
     * <p>任一头或数据帧出现即视为消息交换已开始。
     *
     * @return {@code true} if any {@link Http3HeadersFrame} or {@link Http3DataFrame} has been received/sent on this
     * stream.
     */
    boolean started();

    /**
     * If a final {@link Http3HeadersFrame} has been received/sent before {@link Http3DataFrame} starts.
     * <p>「最终」指非 1xx 信息性响应头；收到后 QPACK 可释放对该流的阻塞引用。
     *
     * @return {@code true} if a final {@link Http3HeadersFrame} has been received/sent before {@link Http3DataFrame}
     * starts
     */
    boolean receivedFinalHeaders();

    /**
     * If no more frames are expected on this stream.
     * <p>通常为已发送/接收 trailer 头块，或整段消息无 trailer 且 DATA 已结束。
     *
     * @return {@code true} if no more frames are expected on this stream.
     */
    boolean terminated();
}
