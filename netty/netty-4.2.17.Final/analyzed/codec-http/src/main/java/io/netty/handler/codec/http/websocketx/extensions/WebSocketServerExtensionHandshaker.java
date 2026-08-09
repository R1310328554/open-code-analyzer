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
package io.netty.handler.codec.http.websocketx.extensions;


/**
 * 服务端 WebSocket 扩展握手器：根据服务端能力匹配客户端请求的扩展配置。
 * <p>由 {@link WebSocketServerExtensionHandler} 按注册顺序调用；无法支持时返回 null。
 */
public interface WebSocketServerExtensionHandshaker {

    /**
     * 基于客户端扩展配置尝试握手。
     *
     * @param extensionData 客户端 {@code Sec-WebSocket-Extensions} 中的一项
     * @return 协商成功则返回可用的 {@link WebSocketServerExtension}，否则 null
     */
    WebSocketServerExtension handshakeExtension(WebSocketExtensionData extensionData);

}
