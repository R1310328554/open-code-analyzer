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

package io.netty.handler.codec.http2;

import io.netty.handler.codec.http2.Http2Stream.State;

/**
 * HTTP/2 连接中的单条流抽象，供 {@link Http2FrameCodec} 向上层暴露流标识与状态。
 * <p>与底层 {@link Http2Stream} 不同，此接口面向 frame 级 API 用户，不暴露流控细节。
 */
public interface Http2FrameStream {
    /**
     * 返回流标识符。
     *
     * <p>Use {@link Http2CodecUtil#isStreamIdValid(int)} to check if the stream has already been assigned an
     * identifier.
     */
    int id();

    /**
     * 返回流当前生命周期状态（IDLE、OPEN、HALF_CLOSED 等）。
     */
    State state();
}
