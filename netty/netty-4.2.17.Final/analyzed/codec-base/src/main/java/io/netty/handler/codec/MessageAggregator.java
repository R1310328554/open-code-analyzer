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
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * 将一系列消息聚合为单一完整消息的抽象 {@link ChannelHandler}。
 * <p>
 * 'A series of messages' is composed of the following:
 * <ul>
 * <li>a single start message which optionally contains the first part of the content, and</li>
 * <li>1 or more content messages.</li>
 * </ul>
 * The content of the aggregated message will be the merged content of the start message and its following content
 * messages. If this aggregator encounters a content message where {@link #isLastContentMessage(ByteBufHolder)}
 * return {@code true} for, the aggregator will finish the aggregation and produce the aggregated message and expect
 * another start message.
 * </p>
 *
  * @param <I> the type that covers both start message and content message
  * @param <S> the type of the start message
  * @param <C> the type of the content message (must be a subtype of {@link ByteBufHolder})
  * @param <O> the type of the aggregated message (must be a subtype of {@code S} and {@link ByteBufHolder})
 */
public abstract class MessageAggregator<I, S, C extends ByteBufHolder, O extends ByteBufHolder>
        extends MessageToMessageDecoder<I> {

    /** 累积 {@link CompositeByteBuf} 默认最大组件数。 */
    /** 累积 {@link CompositeByteBuf} 默认最大组件数。 */
    private static final int DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS = 1024;

    /** 聚合内容最大字节数。 */
    /** 聚合内容最大字节数。 */
    private final int maxContentLength;
    /** 正在聚合的当前消息。 */
    /** 正在聚合的当前消息。 */
    private O currentMessage;
    /** 是否正在处理超大消息。 */
    /** 是否正在处理超大消息。 */
    private boolean handlingOversizedMessage;

    private int maxCumulationBufferComponents = DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS;
    private ChannelHandlerContext ctx;
    private ChannelFutureListener continueResponseWriteListener;

    /** 是否处于聚合过程中。 */
    /** 是否处于聚合过程中。 */
    private boolean aggregating;
    private boolean handleIncompleteAggregateDuringClose = true;

    /**
     * 创建新实例。
     *
     * @param maxContentLength
     *        聚合内容的最大字节数；超出时调用 {@link #handleOversizedMessage(ChannelHandlerContext, Object)}。
     */
    protected MessageAggregator(int maxContentLength) {
        validateMaxContentLength(maxContentLength);
        this.maxContentLength = maxContentLength;
    }

    protected MessageAggregator(int maxContentLength, Class<? extends I> inboundMessageType) {
        super(inboundMessageType);
        validateMaxContentLength(maxContentLength);
        this.maxContentLength = maxContentLength;
    }

    private static void validateMaxContentLength(int maxContentLength) {
        checkPositiveOrZero(maxContentLength, "maxContentLength");
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        // 无需单独匹配 last/full 类型，它们属于 first/middle 的子集
        if (!super.acceptInboundMessage(msg)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        I in = (I) msg;

        if (isAggregated(in)) {
            return false;
        }

        // NOTE: It's tempting to make this check only if aggregating is false. There are however
        // side conditions in decode(...) in respect to large messages.
        if (isStartMessage(in)) {
            return true;
        } else {
            return aggregating && isContentMessage(in);
        }
    }

    /**
     * 当且仅当指定消息为起始消息时返回 {@code true}。 Typically, this method is
     * implemented as a single {@code return} statement with {@code instanceof}:
     * <pre>
     * return msg instanceof MyStartMessage;
     * </pre>
     */
    protected abstract boolean isStartMessage(I msg) throws Exception;

    /**
     * 当且仅当指定消息为内容分片时返回 {@code true}。 Typically, this method is
     * implemented as a single {@code return} statement with {@code instanceof}:
     * <pre>
     * return msg instanceof MyContentMessage;
     * </pre>
     */
    protected abstract boolean isContentMessage(I msg) throws Exception;

    /**
     * 当且仅当指定消息为最后一个内容分片时返回 {@code true}。 Typically, this method is
     * implemented as a single {@code return} statement with {@code instanceof}:
     * <pre>
     * return msg instanceof MyLastContentMessage;
     * </pre>
     * or with {@code instanceof} and boolean field check:
     * <pre>
     * return msg instanceof MyContentMessage && msg.isLastFragment();
     * </pre>
     */
    protected abstract boolean isLastContentMessage(C msg) throws Exception;

    /**
     * 当且仅当指定消息已完整聚合时返回 {@code true}。  If this method returns
     * {@code true}, this handler will simply forward the message to the next handler as-is.
     */
    protected abstract boolean isAggregated(I msg) throws Exception;

    /**
     * 返回聚合消息允许的最大字节数。
     */
    public final int maxContentLength() {
        return maxContentLength;
    }

    /**
     * Returns the maximum number of components in the cumulation buffer.  If the number of
     * the components in the cumulation buffer exceeds this value, the components of the
     * cumulation buffer are consolidated into a single component, involving memory copies.
     * The default value of this property is {@value #DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS}.
     */
    public final int maxCumulationBufferComponents() {
        return maxCumulationBufferComponents;
    }

    /**
     * Sets the maximum number of components in the cumulation buffer.  If the number of
     * the components in the cumulation buffer exceeds this value, the components of the
     * cumulation buffer are consolidated into a single component, involving memory copies.
     * The default value of this property is {@value #DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS}
     * and its minimum allowed value is {@code 2}.
     */
    public final void setMaxCumulationBufferComponents(int maxCumulationBufferComponents) {
        if (maxCumulationBufferComponents < 2) {
            throw new IllegalArgumentException(
                    "maxCumulationBufferComponents: " + maxCumulationBufferComponents +
                    " (expected: >= 2)");
        }

        if (ctx == null) {
            this.maxCumulationBufferComponents = maxCumulationBufferComponents;
        } else {
            throw new IllegalStateException(
                    "decoder properties cannot be changed once the decoder is added to a pipeline.");
        }
    }

    /**
     * @deprecated 未来版本将移除此方法。
     */
    @Deprecated
    public final boolean isHandlingOversizedMessage() {
        return handlingOversizedMessage;
    }

    protected final ChannelHandlerContext ctx() {
        if (ctx == null) {
            throw new IllegalStateException("not added to a pipeline yet");
        }
        return ctx;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, I msg, List<Object> out) throws Exception {
        if (isStartMessage(msg)) {
            aggregating = true;
            handlingOversizedMessage = false;
            if (currentMessage != null) {
                currentMessage.release();
                currentMessage = null;
                throw new MessageAggregationException();
            }

            @SuppressWarnings("unchecked")
            S m = (S) msg;

            // 必要时发送 continue 响应（如 HTTP Expect: 100-continue）
            // Check before content length. Failing an expectation may result in a different response being sent.
            Object continueResponse = newContinueResponse(m, maxContentLength, ctx.pipeline());
            if (continueResponse != null) {
                // 缓存写完成监听器以便复用
                ChannelFutureListener listener = continueResponseWriteListener;
                if (listener == null) {
                    continueResponseWriteListener = listener = future -> {
                        if (!future.isSuccess()) {
                            ctx.fireExceptionCaught(future.cause());
                        }
                    };
                }

                // 必须在写入前调用，否则引用计数可能无效
                boolean closeAfterWrite = closeAfterContinueResponse(continueResponse);
                handlingOversizedMessage = ignoreContentAfterContinueResponse(continueResponse);

                final ChannelFuture future = ctx.writeAndFlush(continueResponse).addListener(listener);

                if (closeAfterWrite) {
                    handleIncompleteAggregateDuringClose = false;
                    future.addListener(ChannelFutureListener.CLOSE);
                    return;
                }
                if (handlingOversizedMessage) {
                    return;
                }
            } else if (isContentLengthInvalid(m, maxContentLength)) {
                // 若已声明 Content-Length 且过大，提前走超大消息处理
                invokeHandleOversizedMessage(ctx, m);
                return;
            }

            if (m instanceof DecoderResultProvider && !((DecoderResultProvider) m).decoderResult().isSuccess()) {
                O aggregated;
                if (m instanceof ByteBufHolder) {
                    aggregated = beginAggregation(m, ((ByteBufHolder) m).content().retain());
                } else {
                    aggregated = beginAggregation(m, EMPTY_BUFFER);
                }
                finishAggregation0(aggregated);
                out.add(aggregated);
                return;
            }

            // 流式消息：初始化累积缓冲区，等待后续分片
            CompositeByteBuf content = ctx.alloc().compositeBuffer(maxCumulationBufferComponents);
            if (m instanceof ByteBufHolder) {
                appendPartialContent(content, ((ByteBufHolder) m).content());
            }
            currentMessage = beginAggregation(m, content);
        } else if (isContentMessage(msg)) {
            if (currentMessage == null) {
                // 可能已抛出 TooLongFrameException，但仍可丢弃数据直至下一消息
                // until the begging of the next request/response.
                return;
            }

            // 将收到的分片合并到当前消息内容
            CompositeByteBuf content = (CompositeByteBuf) currentMessage.content();

            @SuppressWarnings("unchecked")
            final C m = (C) msg;
            // 处理超大消息
            if (content.readableBytes() > maxContentLength - m.content().readableBytes()) {
                // By convention, full message type extends first message type.
                @SuppressWarnings("unchecked")
                S s = (S) currentMessage;
                invokeHandleOversizedMessage(ctx, s);
                return;
            }

            // 追加分片内容
            appendPartialContent(content, m.content());

            // 允许子类合并附加信息（如 trailing headers）
            aggregate(currentMessage, m);

            final boolean last;
            if (m instanceof DecoderResultProvider) {
                DecoderResult decoderResult = ((DecoderResultProvider) m).decoderResult();
                if (!decoderResult.isSuccess()) {
                    if (currentMessage instanceof DecoderResultProvider) {
                        ((DecoderResultProvider) currentMessage).setDecoderResult(
                                DecoderResult.failure(decoderResult.cause()));
                    }
                    last = true;
                } else {
                    last = isLastContentMessage(m);
                }
            } else {
                last = isLastContentMessage(m);
            }

            if (last) {
                finishAggregation0(currentMessage);

                // 聚合完成，输出完整消息
                out.add(currentMessage);
                currentMessage = null;
            }
        } else {
            throw new MessageAggregationException();
        }
    }

    private static void appendPartialContent(CompositeByteBuf content, ByteBuf partialContent) {
        if (partialContent.isReadable()) {
            content.addComponent(true, partialContent.retain());
        }
    }

    /**
     * 判断起始消息是否已知内容长度且超过 {@code maxContentLength}。
      * @param start 可能携带内容长度的起始消息。
      * @param maxContentLength 允许的最大内容长度。
     * @return {@code true} if the message {@code start}'s content length is known, and if it greater than
     * {@code maxContentLength}. {@code false} otherwise.
     */
    protected abstract boolean isContentLengthInvalid(S start, int maxContentLength) throws Exception;

    /**
     * 必要时返回起始消息对应的 continue 响应（如 HTTP 100-continue）。 For example, this method is
     * useful to handle an HTTP 100-continue header.
     *
     * @return continue 响应；无需发送时为 {@code null}
     */
    protected abstract Object newContinueResponse(S start, int maxContentLength, ChannelPipeline pipeline)
            throws Exception;

    /**
     * 判断写入 {@link #newContinueResponse(Object, int, ChannelPipeline)} 结果后是否应关闭通道。
      * @param msg The return value from {@link #newContinueResponse(Object, int, ChannelPipeline)}.
     * @return {@code true} if the channel should be closed after the result of
     * {@link #newContinueResponse(Object, int, ChannelPipeline)} is written. {@code false} otherwise.
     */
    protected abstract boolean closeAfterContinueResponse(Object msg) throws Exception;

    /**
     * 判断是否忽略当前请求/响应的后续对象；下次 {@link #isContentMessage(Object)} 为真时停止忽略。
     *
      * @param msg The return value from {@link #newContinueResponse(Object, int, ChannelPipeline)}.
     * @return {@code true} if all objects for the current request/response should be ignored or not.
     * {@code false} otherwise.
     */
    protected abstract boolean ignoreContentAfterContinueResponse(Object msg) throws Exception;

    /**
     * 由起始消息与内容缓冲区创建新的聚合消息。  If the start
     * message implements {@link ByteBufHolder}, its content is appended to the specified {@code content}.
     * This aggregator will continue to append the received content to the specified {@code content}.
     */
    protected abstract O beginAggregation(S start, ByteBuf content) throws Exception;

    /**
     * 将内容分片的附加信息合并到聚合消息（内容字节已追加）。
     * Note that the content of the specified content message has been appended to the content of the specified
     * aggregated message already, so that you don't need to.  Use this method to transfer the additional information
     * that the content message provides to {@code aggregated}.
     */
    protected void aggregate(O aggregated, C content) throws Exception { }

    private void finishAggregation0(O aggregated) throws Exception {
        aggregating = false;
        finishAggregation(aggregated);
    }

    /**
     * 聚合消息即将传递给下游 handler 时调用。
     */
    protected void finishAggregation(O aggregated) throws Exception { }

    private void invokeHandleOversizedMessage(ChannelHandlerContext ctx, S oversized) throws Exception {
        handlingOversizedMessage = true;
        currentMessage = null;
        handleIncompleteAggregateDuringClose = false;
        try {
            handleOversizedMessage(ctx, oversized);
        } finally {
            // Release the message in case it is a full one.
            ReferenceCountUtil.release(oversized);
        }
    }

    /**
     * 入站请求超出最大内容长度时调用。  The default behvaior is to trigger an
     * {@code exceptionCaught()} event with a {@link TooLongFrameException}.
     *
      * @param ctx  {@link ChannelHandlerContext}
      * @param oversized 截至当前的累积消息，类型为 {@code S} 或 {@code O}
     */
    protected void handleOversizedMessage(ChannelHandlerContext ctx, S oversized) throws Exception {
        ctx.fireExceptionCaught(
                new TooLongFrameException("content length exceeded " + maxContentLength() + " bytes."));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 可能需要持续 read 直至消息完全聚合
        //
        // See https://github.com/netty/netty/issues/6583
        if (currentMessage != null && !ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (aggregating && handleIncompleteAggregateDuringClose) {
            ctx.fireExceptionCaught(
                    new PrematureChannelClosureException("Channel closed while still aggregating message"));
        }
        try {
            // 释放可能残留的 currentMessage
            super.channelInactive(ctx);
        } finally {
            releaseCurrentMessage();
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        try {
            super.handlerRemoved(ctx);
        } finally {
            // release current message if it is not null as it may be a left-over as there is not much more we can do in
            // this case
            releaseCurrentMessage();
        }
    }

    protected final void releaseCurrentMessage() {
        if (currentMessage != null) {
            currentMessage.release();
            currentMessage = null;
        }
        handlingOversizedMessage = false;
        aggregating = false;
    }
}
