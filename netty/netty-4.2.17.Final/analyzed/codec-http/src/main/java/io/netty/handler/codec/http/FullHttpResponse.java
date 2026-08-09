/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;

/**
 * 组合 {@link HttpResponse} 与 {@link FullHttpMessage}，表示一条<i>完整</i> HTTP 响应。
 * <p>
包含状态、头部、正文及 trailing headers。
 */
public interface FullHttpResponse extends HttpResponse, FullHttpMessage {
    @Override
    FullHttpResponse copy();

    @Override
    FullHttpResponse duplicate();

    @Override
    FullHttpResponse retainedDuplicate();

    @Override
    FullHttpResponse replace(ByteBuf content);

    @Override
    FullHttpResponse retain(int increment);

    @Override
    FullHttpResponse retain();

    @Override
    FullHttpResponse touch();

    @Override
    FullHttpResponse touch(Object hint);

    @Override
    FullHttpResponse setProtocolVersion(HttpVersion version);

    @Override
    FullHttpResponse setStatus(HttpResponseStatus status);
}
