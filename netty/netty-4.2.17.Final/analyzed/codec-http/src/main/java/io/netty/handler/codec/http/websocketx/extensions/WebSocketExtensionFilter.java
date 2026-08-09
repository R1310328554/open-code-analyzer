/*
 * Copyright 2019 The Netty Project
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

import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * WebSocket 扩展帧过滤器：按 RFC 约定决定是否跳过某扩展对当前帧的处理。
 * <p>permessage-deflate 等扩展在握手后可配置为仅压缩特定帧类型或大小；
 * 编码器/解码器在 {@link #mustSkip} 返回 true 时不介入该帧。
 */
public interface WebSocketExtensionFilter {

    /** 永不过滤：对所有帧均应用扩展编解码 */

    WebSocketExtensionFilter NEVER_SKIP = new WebSocketExtensionFilter() {
        @Override
        public boolean mustSkip(WebSocketFrame frame) {
            return false;
        }
    };

    /** 始终跳过：扩展编解码器不处理任何帧 */

    WebSocketExtensionFilter ALWAYS_SKIP = new WebSocketExtensionFilter() {
        @Override
        public boolean mustSkip(WebSocketFrame frame) {
            return true;
        }
    };

    /**
     * 判断当前帧是否应跳过扩展处理。
     *
     * @param frame 待处理的 WebSocket 帧
     * @return 为 true 时编/解码器忽略该帧
     */
    boolean mustSkip(WebSocketFrame frame);

}
