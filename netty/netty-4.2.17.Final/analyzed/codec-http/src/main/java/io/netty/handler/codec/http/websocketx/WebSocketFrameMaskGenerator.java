/*
 * Copyright 2025 The Netty Project
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
package io.netty.handler.codec.http.websocketx;

/**
 * 自定义客户端出站 {@link WebSocketFrame} 载荷掩码的 32 位 mask 生成策略。
 * <p>仅客户端发送的数据帧需要 mask（RFC 6455）；默认见 {@link RandomWebSocketFrameMaskGenerator}。
 */
public interface WebSocketFrameMaskGenerator {

    /**
     * 返回下一个用于 XOR 掩码载荷的 32 位整数。
     *
     * @return  mask.
     */
    int nextMask();
}
