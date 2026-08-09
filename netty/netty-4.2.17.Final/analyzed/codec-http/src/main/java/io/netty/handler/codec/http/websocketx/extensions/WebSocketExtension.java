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
 * WebSocket 扩展抽象：握手完成后创建，负责占用 RSV 位并提供帧变换编解码器。
 */
public interface WebSocketExtension {

    /** RSV1 位掩码（0x04）。 */
    int RSV1 = 0x04;
    /** RSV2 位掩码（0x02）。 */
    int RSV2 = 0x02;
    /** RSV3 位掩码（0x01）。 */
    int RSV3 = 0x01;

    /**
     * @return 本扩展占用的 RSV 位值，确保与其他扩展不冲突。
     */
    int rsv();

    /**
     * @return 创建扩展编码器。
     */
    WebSocketExtensionEncoder newExtensionEncoder();

    /**
     * @return 创建扩展解码器。
     */
    WebSocketExtensionDecoder newExtensionDecoder();

}
