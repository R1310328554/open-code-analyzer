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
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * 流式 {@link ChannelInboundHandlerAdapter}：将入站 {@link ByteBuf} 累积并解码为消息对象。
 * <p>
 * For example here is an implementation which reads all readable bytes from
 * the input {@link ByteBuf} and create a new {@link ByteBuf}.
 *
 * <pre>
 *     public class SquareDecoder extends {@link ByteToMessageDecoder} {
 *         {@code @Override}
 *         public void decode({@link ChannelHandlerContext} ctx, {@link ByteBuf} in, List&lt;Object&gt; out)
 *                 throws {@link Exception} {
 *             out.add(in.readBytes(in.readableBytes()));
 *         }
 *     }
 * </pre>
 *
 * <h3>帧边界检测</h3>
 * <p>
 * 帧切分通常由管道前端的
 * {@link DelimiterBasedFrameDecoder}, {@link FixedLengthFrameDecoder}, {@link LengthFieldBasedFrameDecoder},
 * or {@link LineBasedFrameDecoder}.
 * <p>
 * 若自定义帧解码，须确保缓冲中有完整帧再消费：
 * one with {@link ByteToMessageDecoder}. Ensure there are enough bytes in the buffer for a
 * complete frame by checking {@link ByteBuf#readableBytes()}. If there are not enough bytes
 * for a complete frame, return without modifying the reader index to allow more bytes to arrive.
 * <p>
 * 不移动 readerIndex 时可使用 {@link ByteBuf#getInt(int)} 等窥视方法；
 * One <strong>MUST</strong> use the reader index when using methods like {@link ByteBuf#getInt(int)}.
 * For example calling <tt>in.getInt(0)</tt> is assuming the frame starts at the beginning of the buffer, which
 * is not always the case. Use <tt>in.getInt(in.readerIndex())</tt> instead.
 * <h3>注意事项</h3>
 * <p>
 * 子类<strong>不得</strong>标注 {@link @Sharable}。
 * <p>
 * {@link ByteBuf#readBytes(int)} 返回的缓冲须 release 或加入 {@code out}，否则泄漏；
 * 可用 {@link ByteBuf#readSlice(int)} 等派生视图。
 */
public abstract class ByteToMessageDecoder extends ChannelInboundHandlerAdapter {

    /** 通过内存拷贝将多个 {@link ByteBuf} 合并为一个。 */
    public static final Cumulator MERGE_CUMULATOR = new Cumulator() {
        @Override
        public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
            if (cumulation == in) {
                // in 与 cumulation 同一对象时被双重 retain，释放一次
                in.release();
                return cumulation;
            }
            if (!cumulation.isReadable() && in.isContiguous()) {
                // cumulation 空且 in 连续则直接使用 in
                cumulation.release();
                return in;
            }
            try {
                final int required = in.readableBytes();
                if (required > cumulation.maxWritableBytes() ||
                    required > cumulation.maxFastWritableBytes() && cumulation.refCnt() > 1 ||
                    cumulation.isReadOnly()) {
                    // 无法原地扩展或缓冲被共享（refCnt>1）时替换 cumulation
                    return expandCumulation(alloc, cumulation, in);
                }
                cumulation.writeBytes(in, in.readerIndex(), required);
                in.readerIndex(in.writerIndex());
                return cumulation;
            } finally {
                // writeBytes 异常时仍须 release in
                in.release();
            }
        }
    };

    /** 将 {@link ByteBuf} 追加为 {@link CompositeByteBuf} 组件，尽量零拷贝；索引开销可能高于 {@link #MERGE_CUMULATOR}。 */
    public static final Cumulator COMPOSITE_CUMULATOR = new Cumulator() {
        @Override
        public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
            if (cumulation == in) {
                // when the in buffer is the same as the cumulation it is doubly retained, release it once
                in.release();
                return cumulation;
            }
            if (!cumulation.isReadable()) {
                cumulation.release();
                return in;
            }
            CompositeByteBuf composite = null;
            try {
                if (cumulation instanceof CompositeByteBuf && cumulation.refCnt() == 1) {
                    composite = (CompositeByteBuf) cumulation;
                    // Writer index must equal capacity if we are going to "write"
                    // new components to the end
                    if (composite.writerIndex() != composite.capacity()) {
                        composite.capacity(composite.writerIndex());
                    }
                } else {
                    composite = alloc.compositeBuffer(Integer.MAX_VALUE).addFlattenedComponents(true, cumulation);
                }
                composite.addFlattenedComponents(true, in);
                in = null;
                return composite;
            } finally {
                if (in != null) {
                    // We must release if the ownership was not transferred as otherwise it may produce a leak
                    in.release();
                    // Also release any new buffer allocated if we're not returning it
                    if (composite != null && composite != cumulation) {
                        composite.release();
                    }
                }
            }
        }
    };

    private static final byte STATE_INIT = 0;
    private static final byte STATE_CALLING_CHILD_DECODE = 1;
    private static final byte STATE_HANDLER_REMOVED_PENDING = 2;

    // 可重入 channelRead 时的入站消息队列
    private Queue<Object> inputMessages;
    ByteBuf cumulation;
    private Cumulator cumulator = MERGE_CUMULATOR;
    private boolean singleDecode;
    private boolean first;

    /** 在 {@link ChannelConfig#isAutoRead()} 为 {@code false} 时标记是否需主动 {@link ChannelHandlerContext#read()}。 */
    private boolean firedChannelRead;

    private boolean selfFiredChannelRead;

    /** 解码状态位：{@link #STATE_INIT}、{@link #STATE_CALLING_CHILD_DECODE}、{@link #STATE_HANDLER_REMOVED_PENDING}。 */
    private byte decodeState = STATE_INIT;
    private int discardAfterReads = 16;
    private int numReads;

    protected ByteToMessageDecoder() {
        ensureNotSharable();
    }

    /** 每次 {@link #channelRead} 仅解码一条消息（协议升级场景）；默认 {@code false} 以保性能。 */
    public void setSingleDecode(boolean singleDecode) {
        this.singleDecode = singleDecode;
    }

    /** 是否启用单次解码模式；默认 {@code false}。 */
    public boolean isSingleDecode() {
        return singleDecode;
    }

    /** 设置累积入站 {@link ByteBuf} 的 {@link Cumulator} 策略。 */
    public void setCumulator(Cumulator cumulator) {
        this.cumulator = ObjectUtil.checkNotNull(cumulator, "cumulator");
    }

    /** 每累积多少次读操作后调用 {@link ByteBuf#discardSomeReadBytes()} 释放已读内存，默认 {@code 16}。 */
    public void setDiscardAfterReads(int discardAfterReads) {
        checkPositive(discardAfterReads, "discardAfterReads");
        this.discardAfterReads = discardAfterReads;
    }

    /** 内部累积缓冲当前可读字节数；一般解码逻辑无需依赖。 */
    protected int actualReadableBytes() {
        return internalBuffer().readableBytes();
    }

    /** 返回内部累积 {@link ByteBuf}；通常不应直接操作。 */
    protected ByteBuf internalBuffer() {
        if (cumulation != null) {
            return cumulation;
        } else {
            return Unpooled.EMPTY_BUFFER;
        }
    }

    @Override
    public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (decodeState == STATE_CALLING_CHILD_DECODE) {
            decodeState = STATE_HANDLER_REMOVED_PENDING;
            return;
        }
        ByteBuf buf = cumulation;
        if (buf != null) {
            // 立即置 null，避免后续方法误用
            cumulation = null;
            numReads = 0;
            int readable = buf.readableBytes();
            if (readable > 0) {
                ctx.fireChannelRead(buf);
                ctx.fireChannelReadComplete();
            } else {
                buf.release();
            }
        }
        handlerRemoved0(ctx);
    }

    /** Handler 已从 pipeline 移除后的清理钩子。 */
    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception { }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object input) throws Exception {
        if (decodeState == STATE_INIT) {
            do {
                if (input instanceof ByteBuf) {
                    selfFiredChannelRead = true;
                    CodecOutputList out = CodecOutputList.newInstance();
                    try {
                        first = cumulation == null;
                        cumulation = cumulator.cumulate(ctx.alloc(),
                                first ? EMPTY_BUFFER : cumulation, (ByteBuf) input);
                        callDecode(ctx, cumulation, out);
                    } catch (DecoderException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new DecoderException(e);
                    } finally {
                        try {
                            if (cumulation != null && !cumulation.isReadable()) {
                                numReads = 0;
                                try {
                                    cumulation.release();
                                } catch (IllegalReferenceCountException e) {
                                    //noinspection ThrowFromFinallyBlock
                                    throw new IllegalReferenceCountException(
                                            getClass().getSimpleName() +
                                                    "#decode() might have released its input buffer, " +
                                                    "or passed it down the pipeline without a retain() call, " +
                                                    "which is not allowed.", e);
                                }
                                cumulation = null;
                            } else if (++numReads >= discardAfterReads) {
                                // 定期 discard 已读字节降低 OOME 风险；见 #4275
                                numReads = 0;
                                discardSomeReadBytes();
                            }

                            int size = out.size();
                            firedChannelRead |= out.insertSinceRecycled();
                            fireChannelRead(ctx, out, size);
                        } finally {
                            out.recycle();
                        }
                    }
                } else {
                    ctx.fireChannelRead(input);
                }
            } while (inputMessages != null && (input = inputMessages.poll()) != null);
        } else {
            // 可重入：入队后由外层 channelRead 处理
            if (inputMessages == null) {
                inputMessages = new ArrayDeque<>(2);
            }
            inputMessages.offer(input);
        }
    }

    /** 将 {@code out} 前 {@code numElements} 个元素 fireChannelRead 下游。 */
    static void fireChannelRead(ChannelHandlerContext ctx, List<Object> msgs, int numElements) {
        if (msgs instanceof CodecOutputList) {
            fireChannelRead(ctx, (CodecOutputList) msgs, numElements);
        } else {
            for (int i = 0; i < numElements; i++) {
                ctx.fireChannelRead(msgs.get(i));
            }
        }
    }

    /** {@link CodecOutputList} 专用版本，使用 {@link CodecOutputList#getUnsafe(int)}。 */
    static void fireChannelRead(ChannelHandlerContext ctx, CodecOutputList msgs, int numElements) {
        for (int i = 0; i < numElements; i ++) {
            ctx.fireChannelRead(msgs.getUnsafe(i));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        numReads = 0;
        discardSomeReadBytes();
        if (selfFiredChannelRead && !firedChannelRead && !ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
        firedChannelRead = false;
        selfFiredChannelRead = false;
        ctx.fireChannelReadComplete();
    }

    protected final void discardSomeReadBytes() {
        if (cumulation != null && !first && cumulation.refCnt() == 1) {
            // refCnt==1 时丢弃已读字节腾空间；共享引用时不可 discard
            //
            // See:
            // - https://github.com/netty/netty/issues/2327
            // - https://github.com/netty/netty/issues/1764
            cumulation.discardSomeReadBytes();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelInputClosed(ctx, true);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof ChannelInputShutdownEvent) {
            // 输入关闭时须触发 decodeLast 收尾
            channelInputClosed(ctx, false);
        }
        super.userEventTriggered(ctx, evt);
    }

    private void channelInputClosed(ChannelHandlerContext ctx, boolean callChannelInactive) {
        CodecOutputList out = CodecOutputList.newInstance();
        try {
            channelInputClosed(ctx, out);
        } catch (DecoderException e) {
            throw e;
        } catch (Exception e) {
            throw new DecoderException(e);
        } finally {
            try {
                if (cumulation != null) {
                    cumulation.release();
                    cumulation = null;
                }
                int size = out.size();
                fireChannelRead(ctx, out, size);
                if (size > 0) {
                    // Something was read, call fireChannelReadComplete()
                    ctx.fireChannelReadComplete();
                }
                if (callChannelInactive) {
                    ctx.fireChannelInactive();
                }
            } finally {
                // 始终 recycle 输出列表
                out.recycle();
            }
        }
    }

    /** 通道输入关闭（inactive 或 {@link ChannelInputShutdownEvent}）时的解码收尾。 */
    void channelInputClosed(ChannelHandlerContext ctx, List<Object> out) throws Exception {
        if (cumulation != null) {
            callDecode(ctx, cumulation, out);
            // handler 已被移除则不再 decodeLast
            if (!ctx.isRemoved()) {
                // cumulation 被 decode 释放时用 EMPTY_BUFFER；见 #10802
                ByteBuf buffer = cumulation == null ? Unpooled.EMPTY_BUFFER : cumulation;
                decodeLast(ctx, buffer, out);
            }
        } else {
            decodeLast(ctx, Unpooled.EMPTY_BUFFER, out);
        }
    }

    /**
     * 循环调用 {@link #decode} 直至无法继续或 handler 被移除。
     * @param ctx 上下文
     * @param in 累积输入缓冲
     * @param out 解码产出列表
     */
    protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            while (in.isReadable()) {
                final int outSize = out.size();

                if (outSize > 0) {
                    fireChannelRead(ctx, out, outSize);
                    out.clear();

                    // handler 已移除则停止解码
                    //
                    // See:
                    // - https://github.com/netty/netty/issues/4635
                    if (ctx.isRemoved()) {
                        break;
                    }
                }

                int oldInputLength = in.readableBytes();
                decodeRemovalReentryProtection(ctx, in, out);

                // 同上，decode 后再次检查
                //
                // See https://github.com/netty/netty/issues/1664
                if (ctx.isRemoved()) {
                    break;
                }

                if (out.isEmpty()) {
                    if (oldInputLength == in.readableBytes()) {
                        break;
                    } else {
                        continue;
                    }
                }

                if (oldInputLength == in.readableBytes()) {
                    throw new DecoderException(
                            StringUtil.simpleClassName(getClass()) +
                                    ".decode() did not read anything but decoded a message.");
                }

                if (isSingleDecode()) {
                    break;
                }
            }
        } catch (DecoderException e) {
            throw e;
        } catch (Exception cause) {
            throw new DecoderException(cause);
        }
    }

    /**
     * 子类实现：从 {@code in} 解码并追加到 {@code out}；数据不足时勿移动 readerIndex。
     * @param ctx 上下文
     * @param in 输入缓冲
     * @param out 输出列表
     * @throws Exception 解码错误
     */
    protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;

    /**
     * Decode the from one {@link ByteBuf} to an other. This method will be called till either the input
     * {@link ByteBuf} has nothing to read when return from this method or till nothing was read from the input
     * {@link ByteBuf}.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in            the {@link ByteBuf} from which to read data
     * @param out           the {@link List} to which decoded messages should be added
     * @throws Exception    is thrown if an error occurs
     */
    final void decodeRemovalReentryProtection(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        decodeState = STATE_CALLING_CHILD_DECODE;
        try {
            decode(ctx, in, out);
        } finally {
            if (inputMessages == null || inputMessages.isEmpty()) {
                boolean removePending = decodeState == STATE_HANDLER_REMOVED_PENDING;
                decodeState = STATE_INIT;
                if (removePending) {
                    fireChannelRead(ctx, out, out.size());
                    out.clear();
                    handlerRemoved(ctx);
                }
            }
        }
    }

    /**
     * 通道 inactive 时最后一次解码；默认在有剩余字节时调用 {@link #decode}。
     */
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.isReadable()) {
            // 缓冲仍有可读字节时才 decode；见 #4386
            decodeRemovalReentryProtection(ctx, in, out);
        }
    }

    static ByteBuf expandCumulation(ByteBufAllocator alloc, ByteBuf oldCumulation, ByteBuf in) {
        int oldBytes = oldCumulation.readableBytes();
        int newBytes = in.readableBytes();
        int totalBytes = oldBytes + newBytes;
        ByteBuf newCumulation = alloc.buffer(alloc.calculateNewCapacity(totalBytes, Integer.MAX_VALUE));
        ByteBuf toRelease = newCumulation;
        try {
            // 直接 setBytes 避免 writeBytes 额外检查
            newCumulation.setBytes(0, oldCumulation, oldCumulation.readerIndex(), oldBytes)
                .setBytes(oldBytes, in, in.readerIndex(), newBytes)
                .writerIndex(totalBytes);
            in.readerIndex(in.writerIndex());
            toRelease = oldCumulation;
            return newCumulation;
        } finally {
            toRelease.release();
        }
    }

    /** 定义如何将新到达的 {@link ByteBuf} 与已有累积缓冲合并。 */
    public interface Cumulator {
        /**
         * 合并 {@code cumulation} 与 {@code in}，返回持有全部可读字节的 {@link ByteBuf}；
         * 完全消费的缓冲须 {@link ByteBuf#release()}。
         */
        ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in);
    }
}
