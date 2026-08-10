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
 * 按流类型校验帧类型是否合法；{@link Http3FrameCodec} 解码首帧前调用。
 */
@FunctionalInterface
interface Http3FrameTypeValidator {

    /** 不做任何校验，用于 QPACK 专用流等场景。 */
    Http3FrameTypeValidator NO_VALIDATION = (type, first) -> { };

    /**
     * @param type  帧类型整数值
     * @param first 是否为该 QUIC 流上读到的第一帧
     */
    void validate(long type, boolean first) throws Http3Exception;
}
