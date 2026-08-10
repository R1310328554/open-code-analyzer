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
package io.netty.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.SocketChannelConfig;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

/**
 * A set of configuration properties of a {@link Channel}.
 * <p>
 * Please down-cast to more specific configuration type such as
 * {@link SocketChannelConfig} or use {@link #setOptions(Map)} to set the
 * transport-specific properties:
 * <pre>
 * {@link Channel} ch = ...;
 * {@link SocketChannelConfig} cfg = <strong>({@link SocketChannelConfig}) ch.getConfig();</strong>
 * cfg.setTcpNoDelay(false);
 * </pre>
 *
 * <h3>Option map</h3>
 *
 * An option map property is a dynamic write-only property which allows
 * the configuration of a {@link Channel} without down-casting its associated
 * {@link ChannelConfig}.  To update an option map, please call {@link #setOptions(Map)}.
 * <p>
 * All {@link ChannelConfig} has the following options:
 *
 * <table border="1" cellspacing="0" cellpadding="6">
 * <tr>
 * <th>Name</th><th>Associated setter method</th>
 * </tr><tr>
 * <td>{@link ChannelOption#CONNECT_TIMEOUT_MILLIS}</td><td>{@link #setConnectTimeoutMillis(int)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#WRITE_SPIN_COUNT}</td><td>{@link #setWriteSpinCount(int)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#WRITE_BUFFER_WATER_MARK}</td><td>{@link #setWriteBufferWaterMark(WriteBufferWaterMark)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#ALLOCATOR}</td><td>{@link #setAllocator(ByteBufAllocator)}</td>
 * </tr><tr>
 * <td>{@link ChannelOption#AUTO_READ}</td><td>{@link #setAutoRead(boolean)}</td>
 * </tr>
 * </table>
 * <p>
 * More options are available in the sub-types of {@link ChannelConfig}.  For
 * example, you can configure the parameters which are specific to a TCP/IP
 * socket as explained in {@link SocketChannelConfig}.
 * <p>{@link Channel} 的可配置属性集合：连接超时、写缓冲水位、分配器、自动读等。
 * 通用选项可通过 {@link #setOptions(Map)} 设置；传输专有选项需向下转型为
 * {@link SocketChannelConfig} 等子接口。</p>
 */
public interface ChannelConfig {

    /**
     * Return all set {@link ChannelOption}'s.
     * <p>返回当前已设置的所有 {@link ChannelOption} 及其值。</p>
          */
    Map<ChannelOption<?>, Object> getOptions();

    /**
     * Sets the configuration properties from the specified {@link Map}.
     * <p>从 {@link Map} 批量设置配置项；未知选项被忽略。</p>
          */
    boolean setOptions(Map<ChannelOption<?>, ?> options);

    /**
     * Return the value of the given {@link ChannelOption}
     * <p>读取指定 {@link ChannelOption} 的当前值，未设置时返回 {@code null}。</p>
          */
    <T> T getOption(ChannelOption<T> option);

    /**
     * Sets a configuration property with the specified name and value.
     * To override this method properly, you must call the super class:
     * <pre>
     * public boolean setOption(ChannelOption&lt;T&gt; option, T value) {
     *     if (super.setOption(option, value)) {
     *         return true;
     *     }
     *
     *     if (option.equals(additionalOption)) {
     *         ....
     *         return true;
     *     }
     *
     *     return false;
     * }
     * </pre>
     *
     * <p>设置单个 {@link ChannelOption}；子类实现须先调用 {@code super.setOption}。</p>
     *
     * @return {@code true} if and only if the property has been set
     */
    <T> boolean setOption(ChannelOption<T> option, T value);

    /**
     * Returns the connect timeout of the channel in milliseconds.  If the
     * {@link Channel} does not support connect operation, this property is not
     * used at all, and therefore will be ignored.
     *
     * <p>返回连接超时（毫秒）；不支持 connect 的通道忽略此配置。</p>
     *
     * @return the connect timeout in milliseconds.  {@code 0} if disabled.
     */
    int getConnectTimeoutMillis();

    /**
     * Sets the connect timeout of the channel in milliseconds.  If the
     * {@link Channel} does not support connect operation, this property is not
     * used at all, and therefore will be ignored.
     *
     * <p>设置连接超时（毫秒）；{@code 0} 表示禁用。</p>
     *
     * @param connectTimeoutMillis the connect timeout in milliseconds.
     *                             {@code 0} to disable.
     */
    ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    /**
     * @deprecated Use {@link MaxMessagesRecvByteBufAllocator} and
     * {@link MaxMessagesRecvByteBufAllocator#maxMessagesPerRead()}.
     * <p>
     * Returns the maximum number of messages to read per read loop.
     * a {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object) channelRead()} event.
     * If this value is greater than 1, an event loop might attempt to read multiple times to procure multiple messages.
     * <p>每次读循环最多触发的 {@code channelRead} 次数；已废弃，请用 {@link MaxMessagesRecvByteBufAllocator}。</p>
     */
    @Deprecated
    int getMaxMessagesPerRead();

    /**
     * @deprecated Use {@link MaxMessagesRecvByteBufAllocator} and
     * {@link MaxMessagesRecvByteBufAllocator#maxMessagesPerRead(int)}.
     * <p>
     * Sets the maximum number of messages to read per read loop.
     * If this value is greater than 1, an event loop might attempt to read multiple times to procure multiple messages.
     * <p>设置每次读循环的消息上限；已废弃。</p>
     */
    @Deprecated
    ChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    /**
     * Returns the maximum loop count for a write operation until
     * {@link WritableByteChannel#write(ByteBuffer)} returns a non-zero value.
     * It is similar to what a spin lock is used for in concurrency programming.
     * It improves memory utilization and write throughput depending on
     * the platform that JVM runs on.  The default value is {@code 16}.
     * <p>返回写自旋次数上限：在底层 {@link WritableByteChannel#write} 返回非零前最多循环次数。</p>
          */
    int getWriteSpinCount();

    /**
     * Sets the maximum loop count for a write operation until
     * {@link WritableByteChannel#write(ByteBuffer)} returns a non-zero value.
     * It is similar to what a spin lock is used for in concurrency programming.
     * It improves memory utilization and write throughput depending on
     * the platform that JVM runs on.  The default value is {@code 16}.
     *
     * <p>设置写自旋次数；过小降低吞吐，过大可能占用 CPU。</p>
     *
     * @throws IllegalArgumentException
     *         if the specified value is {@code 0} or less than {@code 0}
     */
    ChannelConfig setWriteSpinCount(int writeSpinCount);

    /**
     * Returns {@link ByteBufAllocator} which is used for the channel
     * to allocate buffers.
     * <p>返回通道分配 {@link ByteBuf} 时使用的 {@link ByteBufAllocator}。</p>
          */
    ByteBufAllocator getAllocator();

    /**
     * Set the {@link ByteBufAllocator} which is used for the channel
     * to allocate buffers.
     * <p>设置通道的 {@link ByteBufAllocator}。</p>
          */
    ChannelConfig setAllocator(ByteBufAllocator allocator);

    /**
     * Returns {@link RecvByteBufAllocator} which is used for the channel to allocate receive buffers.
     * <p>返回接收缓冲区分配策略 {@link RecvByteBufAllocator}。</p>
          */
    <T extends RecvByteBufAllocator> T getRecvByteBufAllocator();

    /**
     * Set the {@link RecvByteBufAllocator} which is used for the channel to allocate receive buffers.
     * <p>设置接收缓冲区分配策略。</p>
          */
    ChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    /**
     * Returns {@code true} if and only if {@link ChannelHandlerContext#read()} will be invoked automatically so that
     * a user application doesn't need to call it at all. The default value is {@code true}.
     * <p>为 {@code true} 时事件循环在可读时自动调度读，用户无需手动 {@link ChannelHandlerContext#read()}。</p>
          */
    boolean isAutoRead();

    /**
     * Sets if {@link ChannelHandlerContext#read()} will be invoked automatically so that a user application doesn't
     * need to call it at all. The default value is {@code true}.
     * <p>设置是否自动读；关闭后须显式调用 {@link ChannelHandlerContext#read()} 才能继续接收。</p>
          */
    ChannelConfig setAutoRead(boolean autoRead);

    /**
     * Returns {@code true} if and only if the {@link Channel} will be closed automatically and immediately on
     * write failure. The default is {@code true}.
     * <p>为 {@code true} 时写失败会立即关闭通道。</p>
          */
    boolean isAutoClose();

    /**
     * Sets whether the {@link Channel} should be closed automatically and immediately on write failure.
     * The default is {@code true}.
     * <p>设置写失败时是否自动关闭通道。</p>
          */
    ChannelConfig setAutoClose(boolean autoClose);

    /**
     * Returns the high water mark of the write buffer.  If the number of bytes
     * queued in the write buffer exceeds this value, {@link Channel#isWritable()}
     * will start to return {@code false}.
     * <p>返回写缓冲高水位：排队字节超过此值时 {@link Channel#isWritable()} 变为 {@code false}。</p>
          */
    int getWriteBufferHighWaterMark();

    /**
     * <p>
     * Sets the high water mark of the write buffer.  If the number of bytes
     * queued in the write buffer exceeds this value, {@link Channel#isWritable()}
     * will start to return {@code false}.
     * <p>设置写缓冲高水位。</p>
          */
    ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    /**
     * Returns the low water mark of the write buffer.  Once the number of bytes
     * queued in the write buffer exceeded the
     * {@linkplain #setWriteBufferHighWaterMark(int) high water mark} and then
     * dropped down below this value, {@link Channel#isWritable()} will start to return
     * {@code true} again.
     * <p>返回写缓冲低水位：曾超过高水位后降至其下时 {@link Channel#isWritable()} 恢复 {@code true}。</p>
          */
    int getWriteBufferLowWaterMark();

    /**
     * <p>
     * Sets the low water mark of the write buffer.  Once the number of bytes
     * queued in the write buffer exceeded the
     * {@linkplain #setWriteBufferHighWaterMark(int) high water mark} and then
     * dropped down below this value, {@link Channel#isWritable()} will start to return
     * {@code true} again.
     * <p>设置写缓冲低水位，须小于高水位。</p>
          */
    ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    /**
     * Returns {@link MessageSizeEstimator} which is used for the channel
     * to detect the size of a message.
     * <p>返回用于估算出站消息大小的 {@link MessageSizeEstimator}。</p>
          */
    MessageSizeEstimator getMessageSizeEstimator();

    /**
     * Set the {@link MessageSizeEstimator} which is used for the channel
     * to detect the size of a message.
     * <p>设置消息大小估算器，影响 flush 进度与流量控制。</p>
          */
    ChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);

    /**
     * Returns the {@link WriteBufferWaterMark} which is used for setting the high and low
     * water mark of the write buffer.
     * <p>返回写缓冲高低水位封装对象。</p>
          */
    WriteBufferWaterMark getWriteBufferWaterMark();

    /**
     * Set the {@link WriteBufferWaterMark} which is used for setting the high and low
     * water mark of the write buffer.
     * <p>一次性设置写缓冲高低水位。</p>
          */
    ChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark);
}
