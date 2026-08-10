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

/**
 * 可能与特定流关联的帧（如 DATA、HEADERS、RST_STREAM），区别于纯连接级帧（如 SETTINGS、PING）。
 * <p>若语义作用于整个连接，{@link #stream()} 返回 {@code null}；若绑定流，则
 * {@link Http2FrameStream#id()} 必须大于 0。
 */
public interface Http2StreamFrame extends Http2Frame {

    /**
     * 绑定或更新本帧所属的 {@link Http2FrameStream}。
     */
    Http2StreamFrame stream(Http2FrameStream stream);

    /**
     * 返回关联的 {@link Http2FrameStream}；尚未绑定流时返回 {@code null}。
     */
    Http2FrameStream stream();
}
