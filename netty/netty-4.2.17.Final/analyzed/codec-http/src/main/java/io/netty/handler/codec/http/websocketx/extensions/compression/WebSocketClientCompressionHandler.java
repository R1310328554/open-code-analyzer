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

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;

/**
 * WebSocket 客户端压缩扩展处理器：预注册 permessage-deflate 与 x-deflate-frame 握手器。
 * <p>继承 {@link WebSocketClientExtensionHandler}，开箱即用常见压缩扩展；
 * 用法参见 {@code io.netty.example.http.websocketx.client.WebSocketClient}。
 */
@ChannelHandler.Sharable
public final class WebSocketClientCompressionHandler extends WebSocketClientExtensionHandler {

    /**
     * @deprecated Use {@link WebSocketClientCompressionHandler#WebSocketClientCompressionHandler(int)}
     */
    @Deprecated
    public static final WebSocketClientCompressionHandler INSTANCE = new WebSocketClientCompressionHandler();

    private WebSocketClientCompressionHandler() {
        this(0);
    }

    /**
     * 构造客户端压缩处理器，注册 permessage-deflate 与两种 deflate-frame 模式。
     * @param maxAllocation
     *            Maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     */
    public WebSocketClientCompressionHandler(int maxAllocation) {
        super(new PerMessageDeflateClientExtensionHandshaker(maxAllocation),
                new DeflateFrameClientExtensionHandshaker(false, maxAllocation),
                new DeflateFrameClientExtensionHandshaker(true, maxAllocation));
    }

}
