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
 * {@link Http3PushPromiseFrame} 的默认实现：服务端在请求流上预告即将推送的资源。
 * <p>包含 push ID 与预推送资源的伪头部/常规头部，后续由独立 push 流承载响应体。
 */
public final class DefaultHttp3PushPromiseFrame implements Http3PushPromiseFrame {

    /** 本次 server push 的唯一标识，客户端可用 {@link Http3CancelPushFrame} 撤销。 */
    private final long id;
    /** 预推送响应的头部（:method、:path、:authority 等）。 */
    private final Http3Headers headers;

    public DefaultHttp3PushPromiseFrame(long id) {
        this(id, new DefaultHttp3Headers());
    }

    public DefaultHttp3PushPromiseFrame(long id, Http3Headers headers) {
        this.id = ObjectUtil.checkPositiveOrZero(id, "id");
        this.headers = ObjectUtil.checkNotNull(headers, "headers");
    }

    @Override
    public long id() {
        return id;
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
        DefaultHttp3PushPromiseFrame that = (DefaultHttp3PushPromiseFrame) o;
        return id == that.id &&
                Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, headers);
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "(id=" + id() + ", headers=" + headers() + ')';
    }
}
