/*
 * Copyright 2012 The Netty Project
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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 心跳 Ping 帧（opcode 0x9），对端应以 {@link PongWebSocketFrame} 回应。
 */
public class PingWebSocketFrame extends WebSocketFrame {

    /**
     * 创建空 Ping 帧（FIN 强制为 true）。
     */
    public PingWebSocketFrame() {
        super(true, 0, Unpooled.buffer(0));
    }

    /**
     * 以可选应用数据创建 Ping 帧。
     *
     * @param binaryData
     *            the content of the frame.
     */
    public PingWebSocketFrame(ByteBuf binaryData) {
        super(binaryData);
    }

    /**
     * 创建 Ping 帧并指定 FIN 与 RSV（RFC 6455 中 Ping 必须为完整帧）。
     *
     * @param finalFragment
     *            flag indicating if this frame is the final fragment
     * @param rsv
     *            reserved bits used for protocol extensions
     * @param binaryData
     *            the content of the frame.
     */
    public PingWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
        super(finalFragment, rsv, binaryData);
    }

    @Override
    public PingWebSocketFrame copy() {
        return (PingWebSocketFrame) super.copy();
    }

    @Override
    public PingWebSocketFrame duplicate() {
        return (PingWebSocketFrame) super.duplicate();
    }

    @Override
    public PingWebSocketFrame retainedDuplicate() {
        return (PingWebSocketFrame) super.retainedDuplicate();
    }

    @Override
    public PingWebSocketFrame replace(ByteBuf content) {
        return new PingWebSocketFrame(isFinalFragment(), rsv(), content);
    }

    @Override
    public PingWebSocketFrame retain() {
        super.retain();
        return this;
    }

    @Override
    public PingWebSocketFrame retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public PingWebSocketFrame touch() {
        super.touch();
        return this;
    }

    @Override
    public PingWebSocketFrame touch(Object hint) {
        super.touch(hint);
        return this;
    }
}
