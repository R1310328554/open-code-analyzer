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

/**
 * See <a href="https://tools.ietf.org/html/draft-ietf-quic-http-32#section-7.2.5">PUSH_PROMISE</a>.
 * <p>服务端在请求/推送流上向客户端承诺一条 server push：携带 Push ID 与 push 请求的
 * 伪头部/字段；客户端随后可在对应单向 push 流上接收完整响应。
 */
public interface Http3PushPromiseFrame extends Http3RequestStreamFrame {

    @Override
    default long type() {
        return Http3CodecUtils.HTTP3_PUSH_PROMISE_FRAME_TYPE;
    }

    /**
     * Returns the push id.
     * <p>与后续 {@link Http3PushStreamServerInitializer} 写入流前缀中的 Push ID 一致。
     *
     * @return the id.
     */
    long id();

    /**
     * Returns the carried headers.
     * <p>描述被 push 资源的请求行等价信息（{@code :method}、{@code :path} 等）。
     *
     * @return the headers.
     */
    Http3Headers headers();
}
