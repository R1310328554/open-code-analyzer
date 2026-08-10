/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.stomp;

/**
 * An interface that defines a {@link StompFrame}'s command and headers.
 * <p>表示 STOMP 帧的「命令 + 头部」部分，通常由 {@link StompSubframeDecoder} 作为解码输出的第一个子帧。
 * 后续可能跟随若干 {@link StompContentSubframe} 承载正文。</p>
 *
 * @see StompCommand
 * @see StompHeaders
 */
public interface StompHeadersSubframe extends StompSubframe {
    /**
     * Returns command of this frame.
     * <p>返回本帧的命令字（如 CONNECT、SEND、MESSAGE 等）。</p>
     */
    StompCommand command();

    /**
     * Returns headers of this frame.
     * <p>返回本帧的头部多值映射，键值均为 {@link CharSequence}。</p>
     */
    StompHeaders headers();
}
