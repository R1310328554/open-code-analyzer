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
package io.netty.handler.codec.compression;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Zlib/Deflate 压缩抽象基类，子类实现 JDK 或 JZlib 后端。
 */
public abstract class ZlibEncoder extends MessageToByteEncoder<ByteBuf> {

    protected ZlibEncoder() {
        super(ByteBuf.class, false);
    }

    /** @return 压缩流是否已结束。 */
    public abstract boolean isClosed();

    /**
     * 关闭编码器并完成压缩流；返回的 {@link ChannelFuture} 在操作完成时通知。
     */
    public abstract ChannelFuture close();

    /**
     * 关闭编码器并完成压缩流；给定 {@link ChannelFuture} 在完成时通知并作为返回值。
     */
    public abstract ChannelFuture close(ChannelPromise promise);

}
