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
 * 客户端 WebSocket 扩展握手器：生成请求参数并解析服务端应答。
 */
public interface WebSocketClientExtensionHandshaker {

    /**
     * 返回提交给服务端的扩展配置。
     *
     * @return the desired extension configuration.
     */
    WebSocketExtensionData newRequestData();

    /**
     * 根据服务端应答完成扩展握手；应答应为对客户端请求的确认。
     *
     * @param extensionData
     *          the extension configuration sent by the server.
     * @return an initialized extension if handshake phase succeed or null if failed.
     */
    WebSocketClientExtension handshakeExtension(WebSocketExtensionData extensionData);

}
