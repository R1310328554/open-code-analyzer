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
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.util.internal.StringUtil;

/**
 * 所有 WebSocket 帧的抽象基类，继承 {@link DefaultByteBufHolder} 持有载荷 {@link ByteBuf}。
 * <p>子类包括 {@link TextWebSocketFrame}、{@link BinaryWebSocketFrame}、控制帧等。
 */
public abstract class WebSocketFrame extends DefaultByteBufHolder {

    /** FIN 位：是否为消息最后一个分片（单帧消息首帧即 FIN） */
    private final boolean finalFragment;

    /** RSV1/2/3 扩展位（如 permessage-deflate） */
    private final int rsv;

    protected WebSocketFrame(ByteBuf binaryData) {
        this(true, 0, binaryData);
    }

    protected WebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
        super(binaryData);
        this.finalFragment = finalFragment;
        this.rsv = rsv;
    }

    /**
     * 是否为消息最后一个分片。
     */
    public boolean isFinalFragment() {
        return finalFragment;
    }

    /**
     * 协议扩展使用的 RSV 位。
     */
    public int rsv() {
        return rsv;
    }

    @Override
    public WebSocketFrame copy() {
        return (WebSocketFrame) super.copy();
    }

    @Override
    public WebSocketFrame duplicate() {
        return (WebSocketFrame) super.duplicate();
    }

    @Override
    public WebSocketFrame retainedDuplicate() {
        return (WebSocketFrame) super.retainedDuplicate();
    }

    @Override
    public abstract WebSocketFrame replace(ByteBuf content);

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "(data: " + contentToString() + ')';
    }

    @Override
    public WebSocketFrame retain() {
        super.retain();
        return this;
    }

    @Override
    public WebSocketFrame retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public WebSocketFrame touch() {
        super.touch();
        return this;
    }

    @Override
    public WebSocketFrame touch(Object hint) {
        super.touch(hint);
        return this;
    }
}
