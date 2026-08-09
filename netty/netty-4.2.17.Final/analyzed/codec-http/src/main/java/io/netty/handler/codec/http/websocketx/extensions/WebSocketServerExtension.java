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
 * 服务端 WebSocket 扩展实例，在握手协商成功后创建。
 * <p>扩展 {@link WebSocketExtension}，额外提供向客户端回写确认配置的
 * {@link #newReponseData()} 及编解码器工厂方法。
 */
public interface WebSocketServerExtension extends WebSocketExtension {

    /**
     * 返回写入 101 响应 {@code Sec-WebSocket-Extensions} 的确认配置。
     *
     * @return 已协商的扩展参数
     */
    // TODO: JDK 8 迁移后重命名为 newResponseData() 并将旧名标为 @Deprecated default 方法
    WebSocketExtensionData newReponseData();

}
