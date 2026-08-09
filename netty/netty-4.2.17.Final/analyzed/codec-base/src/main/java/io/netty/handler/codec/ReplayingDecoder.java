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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.Signal;
import io.netty.util.internal.StringUtil;

import java.util.List;

/**
 * {@link ByteToMessageDecoder} 的特化变体：在阻塞 I/O 编程模型下实现“非阻塞式”解码体验。
 * <p>
 * The biggest difference between {@link ReplayingDecoder} and
 * {@link ByteToMessageDecoder} is that {@link ReplayingDecoder} allows you to
 * implement the {@code decode()} and {@code decodeLast()} methods just like
 * all required bytes were received already, rather than checking the
 * availability of the required bytes.  For example, the following
 * {@link ByteToMessageDecoder} implementation:
 * <pre>
 * public class IntegerHeaderFrameDecoder extends {@link ByteToMessageDecoder} {
 *
 *   {@code @Override}
 *   protected void decode({@link ChannelHandlerContext} ctx,
 *                           {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *
 *     if (buf.readableBytes() &lt; 4) {
 *        return;
 *     }
 *
 *     buf.markReaderIndex();
 *     int length = buf.readInt();
 *
 *     if (buf.readableBytes() &lt; length) {
 *        buf.resetReaderIndex();
 *        return;
 *     }
 *
 *     out.add(buf.readBytes(length));
 *   }
 * }
 * </pre>
 * is simplified like the following with {@link ReplayingDecoder}:
 * <pre>
 * public class IntegerHeaderFrameDecoder
 *      extends {@link ReplayingDecoder}&lt;{@link Void}&gt; {
 *
 *   protected void decode({@link ChannelHandlerContext} ctx,
 *                           {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *
 *     out.add(buf.readBytes(buf.readInt()));
 *   }
 * }
 * </pre>
 *
 * <h3>工作原理</h3>
 * <p>
 * {@link ReplayingDecoder} passes a specialized {@link ByteBuf}
 * implementation which throws an {@link Error} of certain type when there's not
 * enough data in the buffer.  In the {@code IntegerHeaderFrameDecoder} above,
 * you just assumed that there will be 4 or more bytes in the buffer when
 * you call {@code buf.readInt()}.  If there's really 4 bytes in the buffer,
 * it will return the integer header as you expected.  Otherwise, the
 * {@link Error} will be raised and the control will be returned to
 * {@link ReplayingDecoder}.  If {@link ReplayingDecoder} catches the
 * {@link Error}, then it will rewind the {@code readerIndex} of the buffer
 * back to the 'initial' position (i.e. the beginning of the buffer) and call
 * the {@code decode(..)} method again when more data is received into the
 * buffer.
 * <p>
 * Please note that {@link ReplayingDecoder} always throws the same cached
 * {@link Error} instance to avoid the overhead of creating a new {@link Error}
 * and filling its stack trace for every throw.
 *
 * <h3>使用限制</h3>
 * <p>
 * At the cost of the simplicity, {@link ReplayingDecoder} enforces you a few
 * limitations:
 * <ul>
 * <li>Some buffer operations are prohibited.</li>
 * <li>Performance can be worse if the network is slow and the message
 *     format is complicated unlike the example above.  In this case, your
 *     decoder might have to decode the same part of the message over and over
 *     again.</li>
 * <li>You must keep in mind that {@code decode(..)} method can be called many
 *     times to decode a single message.  For example, the following code will
 *     not work:
 * <pre> public class MyDecoder extends {@link ReplayingDecoder}&lt;{@link Void}&gt; {
 *
 *   private final Queue&lt;Integer&gt; values = new LinkedList&lt;Integer&gt;();
 *
 *   {@code @Override}
 *   public void decode(.., {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *
 *     // A message contains 2 integers.
 *     values.offer(buf.readInt());
 *     values.offer(buf.readInt());
 *
 *     // This assertion will fail intermittently since values.offer()
 *     // can be called more than two times!
 *     assert values.size() == 2;
 *     out.add(values.poll() + values.poll());
 *   }
 * }</pre>
 *      The correct implementation looks like the following, and you can also
 *      utilize the 'checkpoint' feature which is explained in detail in the
 *      next section.
 * <pre> public class MyDecoder extends {@link ReplayingDecoder}&lt;{@link Void}&gt; {
 *
 *   private final Queue&lt;Integer&gt; values = new LinkedList&lt;Integer&gt;();
 *
 *   {@code @Override}
 *   public void decode(.., {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *
 *     // Revert the state of the variable that might have been changed
 *     // since the last partial decode.
 *     values.clear();
 *
 *     // A message contains 2 integers.
 *     values.offer(buf.readInt());
 *     values.offer(buf.readInt());
 *
 *     // Now we know this assertion will never fail.
 *     assert values.size() == 2;
 *     out.add(values.poll() + values.poll());
 *   }
 * }</pre>
 *     </li>
 * </ul>
 *
 * <h3>性能优化</h3>
 * <p>
 * Fortunately, the performance of a complex decoder implementation can be
 * improved significantly with the {@code checkpoint()} method.  The
 * {@code checkpoint()} method updates the 'initial' position of the buffer so
 * that {@link ReplayingDecoder} rewinds the {@code readerIndex} of the buffer
 * to the last position where you called the {@code checkpoint()} method.
 *
 * <h4>Calling {@code checkpoint(T)} with an {@link Enum}</h4>
 * <p>
 * Although you can just use {@code checkpoint()} method and manage the state
 * of the decoder by yourself, the easiest way to manage the state of the
 * decoder is to create an {@link Enum} type which represents the current state
 * of the decoder and to call {@code checkpoint(T)} method whenever the state
 * changes.  You can have as many states as you want depending on the
 * complexity of the message you want to decode:
 *
 * <pre>
 * public enum MyDecoderState {
 *   READ_LENGTH,
 *   READ_CONTENT;
 * }
 *
 * public class IntegerHeaderFrameDecoder
 *      extends {@link ReplayingDecoder}&lt;<strong>MyDecoderState</strong>&gt; {
 *
 *   private int length;
 *
 *   public IntegerHeaderFrameDecoder() {
 *     // Set the initial state.
 *     <strong>super(MyDecoderState.READ_LENGTH);</strong>
 *   }
 *
 *   {@code @Override}
 *   protected void decode({@link ChannelHandlerContext} ctx,
 *                           {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *     switch (state()) {
 *     case READ_LENGTH:
 *       length = buf.readInt();
 *       <strong>checkpoint(MyDecoderState.READ_CONTENT);</strong>
 *     case READ_CONTENT:
 *       ByteBuf frame = buf.readBytes(length);
 *       <strong>checkpoint(MyDecoderState.READ_LENGTH);</strong>
 *       out.add(frame);
 *       break;
 *     default:
 *       throw new Error("Shouldn't reach here.");
 *     }
 *   }
 * }
 * </pre>
 *
 * <h4>Calling {@code checkpoint()} with no parameter</h4>
 * <p>
 * An alternative way to manage the decoder state is to manage it by yourself.
 * <pre>
 * public class IntegerHeaderFrameDecoder
 *      extends {@link ReplayingDecoder}&lt;<strong>{@link Void}</strong>&gt; {
 *
 *   <strong>private boolean readLength;</strong>
 *   private int length;
 *
 *   {@code @Override}
 *   protected void decode({@link ChannelHandlerContext} ctx,
 *                           {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *     if (!readLength) {
 *       length = buf.readInt();
 *       <strong>readLength = true;</strong>
 *       <strong>checkpoint();</strong>
 *     }
 *
 *     if (readLength) {
 *       ByteBuf frame = buf.readBytes(length);
 *       <strong>readLength = false;</strong>
 *       <strong>checkpoint();</strong>
 *       out.add(frame);
 *     }
 *   }
 * }
 * </pre>
 *
 * <h3>在 pipeline 中替换解码器</h3>
 * <p>
 * If you are going to write a protocol multiplexer, you will probably want to
 * replace a {@link ReplayingDecoder} (protocol detector) with another
 * {@link ReplayingDecoder}, {@link ByteToMessageDecoder} or {@link MessageToMessageDecoder}
 * (actual protocol decoder).
 * It is not possible to achieve this simply by calling
 * {@link ChannelPipeline#replace(ChannelHandler, String, ChannelHandler)}, but
 * some additional steps are required:
 * <pre>
 * public class FirstDecoder extends {@link ReplayingDecoder}&lt;{@link Void}&gt; {
 *
 *     {@code @Override}
 *     protected void decode({@link ChannelHandlerContext} ctx,
 *                             {@link ByteBuf} buf, List&lt;Object&gt; out) {
 *         ...
 *         // Decode the first message
 *         Object firstMessage = ...;
 *
 *         // Add the second decoder
 *         ctx.pipeline().addLast("second", new SecondDecoder());
 *
 *         if (buf.isReadable()) {
 *             // Hand off the remaining data to the second decoder
 *             out.add(firstMessage);
 *             out.add(buf.readBytes(<b>super.actualReadableBytes()</b>));
 *         } else {
 *             // Nothing to hand off
 *             out.add(firstMessage);
 *         }
 *         // Remove the first decoder (me)
 *         ctx.pipeline().remove(this);
 *     }
 * </pre>
 * @param <S> 解码器状态类型，通常为 {@link Enum}；不使用状态机时用 {@link Void}
 */
public abstract class ReplayingDecoder<S> extends ByteToMessageDecoder {

    /** 数据不足时由 {@link ReplayingDecoderByteBuf} 抛出的重放信号。 */
    static final Signal REPLAY = Signal.valueOf(ReplayingDecoder.class, "REPLAY");

    /** 包装累积缓冲的可重放 {@link ByteBuf}。 */
    private final ReplayingDecoderByteBuf replayable = new ReplayingDecoderByteBuf();
    /** 当前解码状态（可选状态机）。 */
    private S state;
    /** 累积缓冲 readerIndex 检查点；{@code -1} 表示未设置。 */
    private int checkpoint = -1;

    /** 创建无初始状态（{@code null}）的实例。 */
    protected ReplayingDecoder() {
        this(null);
    }

    /** 以指定初始状态创建实例。 */
    protected ReplayingDecoder(S initialState) {
        state = initialState;
    }

    /** 保存内部累积缓冲的 readerIndex 作为回退点。 */
    protected void checkpoint() {
        checkpoint = internalBuffer().readerIndex();
    }

    /** 保存 readerIndex 并更新解码器状态。 */
    protected void checkpoint(S state) {
        checkpoint();
        state(state);
    }

    /** @return 当前解码器状态 */
    protected S state() {
        return state;
    }

    /** @return 设置前的旧状态 */
    protected S state(S newState) {
        S oldState = state;
        state = newState;
        return oldState;
    }

    @Override
    final void channelInputClosed(ChannelHandlerContext ctx, List<Object> out) throws Exception {
        try {
            replayable.terminate();
            if (cumulation != null) {
                callDecode(ctx, internalBuffer(), out);
            } else {
                replayable.setCumulation(Unpooled.EMPTY_BUFFER);
            }
            decodeLast(ctx, replayable, out);
        } catch (Signal replay) {
            // 通道关闭时忽略 REPLAY 信号
            replay.expect(REPLAY);
        }
    }

    @Override
    protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        replayable.setCumulation(in);
        try {
            while (in.isReadable()) {
                int oldReaderIndex = checkpoint = in.readerIndex();
                int outSize = out.size();

                if (outSize > 0) {
                    fireChannelRead(ctx, out, outSize);
                    out.clear();

                    // handler 已移除则停止解码，避免操作已释放缓冲
                    //
                    // See:
                    // - https://github.com/netty/netty/issues/4635
                    if (ctx.isRemoved()) {
                        break;
                    }
                    outSize = 0;
                }

                S oldState = state;
                int oldInputLength = in.readableBytes();
                try {
                    decodeRemovalReentryProtection(ctx, replayable, out);

                    // Check if this handler was removed before continuing the loop.
                    // If it was removed, it is not safe to continue to operate on the buffer.
                    //
                    // See https://github.com/netty/netty/issues/1664
                    if (ctx.isRemoved()) {
                        break;
                    }

                    if (outSize == out.size()) {
                        if (oldInputLength == in.readableBytes() && oldState == state) {
                            throw new DecoderException(
                                    StringUtil.simpleClassName(getClass()) + ".decode() must consume the inbound " +
                                    "data or change its state if it did not decode anything.");
                        } else {
                            // Previous data has been discarded or caused state transition.
                            // Probably it is reading on.
                            continue;
                        }
                    }
                } catch (Signal replay) {
                    replay.expect(REPLAY);

                    // Check if this handler was removed before continuing the loop.
                    // If it was removed, it is not safe to continue to operate on the buffer.
                    //
                    // See https://github.com/netty/netty/issues/1664
                    if (ctx.isRemoved()) {
                        break;
                    }

                    // 回退到 checkpoint 等待更多入站数据后重试
                    int checkpoint = this.checkpoint;
                    if (checkpoint >= 0) {
                        in.readerIndex(checkpoint);
                    } else {
                        // cleanup 路径：缓冲已释放，无需恢复 readerIndex
                    }
                    break;
                }

                if (oldReaderIndex == in.readerIndex() && oldState == state) {
                    throw new DecoderException(
                           StringUtil.simpleClassName(getClass()) + ".decode() method must consume the inbound data " +
                           "or change its state if it decoded something.");
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
}
