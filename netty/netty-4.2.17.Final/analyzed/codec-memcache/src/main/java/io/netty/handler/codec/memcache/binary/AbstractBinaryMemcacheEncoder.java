/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.memcache.binary;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.memcache.AbstractMemcacheObjectEncoder;
import io.netty.util.internal.UnstableApi;

/**
 * A {@link MessageToByteEncoder} that encodes binary memcache messages into bytes.
 *
 * <p>二进制 Memcache 编码：将 24 字节头、extras、key 写入单个 {@link ByteBuf}；
 * value 仍由父类 {@link AbstractMemcacheObjectEncoder} 按 {@link io.netty.handler.codec.memcache.MemcacheContent} 分片追加。</p>
 */
@UnstableApi
public abstract class AbstractBinaryMemcacheEncoder<M extends BinaryMemcacheMessage>
    extends AbstractMemcacheObjectEncoder<M> {

    /**
     * Every binary memcache message has at least a 24 bytes header.
     */
    private static final int MINIMUM_HEADER_SIZE = 24;

    @Override
    protected ByteBuf encodeMessage(ChannelHandlerContext ctx, M msg) {
        ByteBuf buf = ctx.alloc().buffer(MINIMUM_HEADER_SIZE + msg.extrasLength()
            + msg.keyLength());

        encodeHeader(buf, msg);
        encodeExtras(buf, msg.extras());
        encodeKey(buf, msg.key());

        return buf;
    }

    /**
     * Encode the extras.
     *
     * @param buf    the {@link ByteBuf} to write into.
     * @param extras the extras to encode.
     *
     * <p>extras 可选；null 或不可读时跳过。</p>
     */
    private static void encodeExtras(ByteBuf buf, ByteBuf extras) {
        if (extras == null || !extras.isReadable()) {
            return;
        }

        buf.writeBytes(extras);
    }

    /**
     * Encode the key.
     *
     * @param buf the {@link ByteBuf} to write into.
     * @param key the key to encode.
     */
    private static void encodeKey(ByteBuf buf, ByteBuf key) {
        if (key == null || !key.isReadable()) {
            return;
        }

        buf.writeBytes(key);
    }

    /**
     * Encode the header.
     * <p/>
     * This methods needs to be implemented by a sub class because the header is different
     * for both requests and responses.
     *
     * @param buf the {@link ByteBuf} to write into.
     * @param msg the message to encode.
     *
     * <p>子类写入 magic、opcode、body length、opaque、CAS 等；必须与 {@link AbstractBinaryMemcacheDecoder#decodeHeader} 对称。</p>
     */
    protected abstract void encodeHeader(ByteBuf buf, M msg);

}
