/*
 * Copyright 2020 The Netty Project
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

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.Objects;

/**
 * HTTP/3 HEADERS 帧的默认实现，封装经 QPACK 编解码后的头部块。
 */
public final class DefaultHttp3HeadersFrame implements Http3HeadersFrame {

    /** QPACK 解压后的完整头部集合（含伪头部 :method、:path 等）。 */
    private final Http3Headers headers;

    /** 使用空的 {@link DefaultHttp3Headers} 构造，便于逐步填充。 */
    public DefaultHttp3HeadersFrame() {
        this(new DefaultHttp3Headers());
    }

    public DefaultHttp3HeadersFrame(Http3Headers headers) {
        this.headers = ObjectUtil.checkNotNull(headers, "headers");
    }

    @Override
    public Http3Headers headers() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultHttp3HeadersFrame that = (DefaultHttp3HeadersFrame) o;
        return Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers);
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "(headers=" + headers() + ')';
    }
}
