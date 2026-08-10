/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http2;

import io.netty.channel.ChannelHandlerContext;

/**
 * Server Push 合法性校验扩展点：按 RFC 7540 §8.2 判定 promised 请求是否可接受。
 * <p>解码器在收到 {@code PUSH_PROMISE} 时调用，任一校验失败则拒绝推送。
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-8.2">[RFC 7540], Section 8.2</a>.
 */
public interface Http2PromisedRequestVerifier {
    /**
     * 判断 {@code headers} 对于当前 {@code ctx} 所代表的 authority 是否权威（host/scheme 匹配）。
     * @param ctx The context on which the {@code headers} where received on.
     * @param headers The headers to be verified.
     * @return {@code true} if the {@code ctx} is authoritative for the {@code headers}, {@code false} otherwise.
     * @see
     * <a href="https://tools.ietf.org/html/rfc7540#section-10.1">[RFC 7540], Section 10.1</a>.
     */
    boolean isAuthoritative(ChannelHandlerContext ctx, Http2Headers headers);

    /**
     * 判断推送请求是否可缓存（RFC 7231 §4.2.3）；不可缓存的请求不应被 push。
     * @param headers The headers for a push request.
     * @return {@code true} if the request associated with {@code headers} is known to be cacheable,
     * {@code false} otherwise.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.2.3">[RFC 7231], Section 4.2.3</a>.
     */
    boolean isCacheable(Http2Headers headers);

    /**
     * 判断推送请求是否安全（无副作用，RFC 7231 §4.2.1）；仅 GET 等安全方法适合 push。
     * @param headers The headers for a push request.
     * @return {@code true} if the request associated with {@code headers} is known to be safe,
     * {@code false} otherwise.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.2.1">[RFC 7231], Section 4.2.1</a>.
     */
    boolean isSafe(Http2Headers headers);

    /**
     * 默认实现：对所有校验均返回 {@code true}，等同于关闭 push 合法性过滤。
     */
    Http2PromisedRequestVerifier ALWAYS_VERIFY = new Http2PromisedRequestVerifier() {
        @Override
        public boolean isAuthoritative(ChannelHandlerContext ctx, Http2Headers headers) {
            return true;
        }

        @Override
        public boolean isCacheable(Http2Headers headers) {
            return true;
        }

        @Override
        public boolean isSafe(Http2Headers headers) {
            return true;
        }
    };
}
