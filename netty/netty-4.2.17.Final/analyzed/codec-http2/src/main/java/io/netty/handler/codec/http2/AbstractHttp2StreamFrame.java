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
 * {@link Http2StreamFrame} 的抽象基类，持有所属 {@link Http2FrameStream} 引用。
 * <p>具体帧类型（HEADERS、DATA 等）继承此类后只需关注自身 payload；
 * {@link #equals(Object)} / {@link #hashCode()} 以绑定的 stream 为语义键。
 */
public abstract class AbstractHttp2StreamFrame implements Http2StreamFrame {

    private Http2FrameStream stream;

    @Override
    public AbstractHttp2StreamFrame stream(Http2FrameStream stream) {
        this.stream = stream;
        return this;
    }

    @Override
    public Http2FrameStream stream() {
        return stream;
    }

    /**
     * 当 {@code o} 为 {@link Http2StreamFrame} 且二者绑定的 {@code stream} 相同时返回 {@code true}。
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Http2StreamFrame)) {
            return false;
        }
        Http2StreamFrame other = (Http2StreamFrame) o;
        return stream == other.stream() || stream != null && stream.equals(other.stream());
    }

    @Override
    public int hashCode() {
        Http2FrameStream stream = this.stream;
        // 须与 equals 一致；super.hashCode() 为 Object 身份哈希。
        if (stream == null) {
            return 0;
        }
        return stream.hashCode();
    }
}
