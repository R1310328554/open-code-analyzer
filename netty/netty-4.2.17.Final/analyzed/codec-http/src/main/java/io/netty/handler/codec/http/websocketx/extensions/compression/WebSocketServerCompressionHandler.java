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
package io.netty.handler.codec.http.websocketx.extensions.compression;

import io.netty.handler.codec.http.websocketx.extensions.WebSocketServerExtensionHandler;

/**
 * WebSocket 服务端压缩扩展处理器：预注册 permessage-deflate 与 x-deflate-frame 握手器。
 * <p>继承 {@link WebSocketServerExtensionHandler}，开箱即用常见压缩扩展；
 * 用法参见 {@code io.netty.example.http.websocketx.html5.WebSocketServer}。
 */
public class WebSocketServerCompressionHandler extends WebSocketServerExtensionHandler {

    /**
     * 默认配置（已弃用，请使用带 maxAllocation 的构造器）。
     *
     * @deprecated
     *            Use {@link WebSocketServerCompressionHandler#WebSocketServerCompressionHandler(int)}.
     */
    @Deprecated
    public WebSocketServerCompressionHandler() {
        this(0);
    }

    /**
     * 构造服务端压缩处理器，注册 permessage-deflate 与 deflate-frame 扩展。
     *
     * @param maxAllocation
     *            Maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     */
    public WebSocketServerCompressionHandler(int maxAllocation) {
        super(new PerMessageDeflateServerExtensionHandshaker(maxAllocation),
                new DeflateFrameServerExtensionHandshaker(
                        DeflateFrameServerExtensionHandshaker.DEFAULT_COMPRESSION_LEVEL, maxAllocation));
    }

}
