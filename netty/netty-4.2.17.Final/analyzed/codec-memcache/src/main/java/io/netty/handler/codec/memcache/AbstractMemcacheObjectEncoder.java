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
package io.netty.handler.codec.memcache;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.FileRegion;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * A general purpose {@link AbstractMemcacheObjectEncoder} that encodes {@link MemcacheMessage}s.
 * <p/>
 * <p>Note that this class is designed to be extended, especially because both the binary and ascii protocol
 * require different treatment of their messages. Since the content chunk writing is the same for both, the encoder
 * abstracts this right away.</p>
 *
 * <p>Memcache 出站编码模板：子类实现 {@link #encodeMessage} 写协议头，本类统一处理后续 body 分片。
 * 通过 {@code expectingMoreContent} 状态机保证「消息头 → 若干 content → 下一消息头」的严格顺序。</p>
 */
@UnstableApi
public abstract class AbstractMemcacheObjectEncoder<M extends MemcacheMessage> extends MessageToMessageEncoder<Object> {

    /** 上一帧是否已写消息头、仍在等待后续 content 分片。 */
    private boolean expectingMoreContent;

    public AbstractMemcacheObjectEncoder() {
        super(Object.class);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        if (msg instanceof MemcacheMessage) {
            if (expectingMoreContent) {
                // 上一消息的 body 尚未结束，不能再写新消息头
                throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
            }

            @SuppressWarnings({ "unchecked", "CastConflictsWithInstanceof" })
            final M m = (M) msg;
            out.add(encodeMessage(ctx, m));
        }

        if (msg instanceof MemcacheContent || msg instanceof ByteBuf || msg instanceof FileRegion) {
            int contentLength = contentLength(msg);
            if (contentLength > 0) {
                out.add(encodeAndRetain(msg));
            } else {
                // 零长度分片仍占位，保持分片边界
                out.add(Unpooled.EMPTY_BUFFER);
            }

            expectingMoreContent = !(msg instanceof LastMemcacheContent);
        }
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return msg instanceof MemcacheObject || msg instanceof ByteBuf || msg instanceof FileRegion;
    }

    /**
     * Take the given {@link MemcacheMessage} and encode it into a writable {@link ByteBuf}.
     *
     * @param ctx the channel handler context.
     * @param msg the message to encode.
     * @return the {@link ByteBuf} representation of the message.
     *
     * <p>子类将协议头（及 key/extras 等）写入可写 {@link ByteBuf}；body 由本类按分片单独追加。</p>
     */
    protected abstract ByteBuf encodeMessage(ChannelHandlerContext ctx, M msg);

    /**
     * Determine the content length of the given object.
     *
     * @param msg the object to determine the length of.
     * @return the determined content length.
     */
    private static int contentLength(Object msg) {
        if (msg instanceof MemcacheContent) {
            return ((MemcacheContent) msg).content().readableBytes();
        }
        if (msg instanceof ByteBuf) {
            return ((ByteBuf) msg).readableBytes();
        }
        if (msg instanceof FileRegion) {
            return (int) ((FileRegion) msg).count();
        }
        throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
    }

    /**
     * Encode the content, depending on the object type.
     *
     * @param msg the object to encode.
     * @return the encoded object.
     *
     * <p>retain 后交给 pipeline 写出，避免编码阶段意外释放引用。</p>
     */
    private static Object encodeAndRetain(Object msg) {
        if (msg instanceof ByteBuf) {
            return ((ByteBuf) msg).retain();
        }
        if (msg instanceof MemcacheContent) {
            return ((MemcacheContent) msg).content().retain();
        }
        if (msg instanceof FileRegion) {
            return ((FileRegion) msg).retain();
        }
        throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
    }

}
