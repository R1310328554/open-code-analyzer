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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeMap;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


/**
 * A nexus to a network socket or a component which is capable of I/O
 * operations such as read, write, connect, and bind.
 * <p>
 * A channel provides a user:
 * <ul>
 * <li>the current state of the channel (e.g. is it open? is it connected?),</li>
 * <li>the {@linkplain ChannelConfig configuration parameters} of the channel (e.g. receive buffer size),</li>
 * <li>the I/O operations that the channel supports (e.g. read, write, connect, and bind), and</li>
 * <li>the {@link ChannelPipeline} which handles all I/O events and requests
 *     associated with the channel.</li>
 * </ul>
 *
 * <h3>All I/O operations are asynchronous.</h3>
 * <p>
 * All I/O operations in Netty are asynchronous.  It means any I/O calls will
 * return immediately with no guarantee that the requested I/O operation has
 * been completed at the end of the call.  Instead, you will be returned with
 * a {@link ChannelFuture} instance which will notify you when the requested I/O
 * operation has succeeded, failed, or canceled.
 *
 * <h3>Channels are hierarchical</h3>
 * <p>
 * A {@link Channel} can have a {@linkplain #parent() parent} depending on
 * how it was created.  For instance, a {@link SocketChannel}, that was accepted
 * by {@link ServerSocketChannel}, will return the {@link ServerSocketChannel}
 * as its parent on {@link #parent()}.
 * <p>
 * The semantics of the hierarchical structure depends on the transport
 * implementation where the {@link Channel} belongs to.  For example, you could
 * write a new {@link Channel} implementation that creates the sub-channels that
 * share one socket connection, as <a href="http://beepcore.org/">BEEP</a> and
 * <a href="https://en.wikipedia.org/wiki/Secure_Shell">SSH</a> do.
 *
 * <h3>Downcast to access transport-specific operations</h3>
 * <p>
 * Some transports exposes additional operations that is specific to the
 * transport.  Down-cast the {@link Channel} to sub-type to invoke such
 * operations.  For example, with the old I/O datagram transport, multicast
 * join / leave operations are provided by {@link DatagramChannel}.
 *
 * <h3>Release resources</h3>
 * <p>
 * It is important to call {@link #close()} or {@link #close(ChannelPromise)} to release all
 * resources once you are done with the {@link Channel}. This ensures all resources are
 * released in a proper way, i.e. filehandles.
 * <p>通道是 Netty 对网络套接字或可 I/O 组件的抽象入口：提供状态查询、配置、
 * 异步 I/O 及 {@link ChannelPipeline}。所有 I/O 异步完成，通过 {@link ChannelFuture} 获知结果；
 * 存在父子层次结构；用完须 {@link #close()} 释放资源。</p>
 */
public interface Channel extends AttributeMap, ChannelOutboundInvoker, Comparable<Channel> {

    /**
     * Returns the globally unique identifier of this {@link Channel}.
     * <p>返回本通道的全局唯一标识 {@link ChannelId}。</p>
     */
    ChannelId id();

    /**
     * Return the {@link EventLoop} this {@link Channel} was registered to.
     * <p>返回本通道注册所在的 {@link EventLoop}。</p>
     */
    EventLoop eventLoop();

    /**
     * Returns the parent of this channel.
     *
     * @return the parent channel.
     *         {@code null} if this channel does not have a parent channel.
     */
    Channel parent();

    /**
     * Returns the configuration of this channel.
     * <p>返回本通道的 {@link ChannelConfig} 配置对象。</p>
     */
    ChannelConfig config();

    /**
     * Returns {@code true} if the {@link Channel} is open and may get active later
     * <p>通道已打开且尚未关闭，后续可能变为 active。</p>
     */
    boolean isOpen();

    /**
     * Returns {@code true} if the {@link Channel} is registered with an {@link EventLoop}.
     * <p>通道已注册到某个 {@link EventLoop}。</p>
     */
    boolean isRegistered();

    /**
     * Return {@code true} if the {@link Channel} is active and so connected.
     * <p>通道处于 active 状态（如 TCP 已连接或 UDP 已绑定可用）。</p>
     */
    boolean isActive();

    /**
     * Return the {@link ChannelMetadata} of the {@link Channel} which describe the nature of the {@link Channel}.
     * <p>返回描述通道特性（如有连接语义、最大消息数）的元数据。</p>
     */
    ChannelMetadata metadata();

    /**
     * Returns the local address where this channel is bound to.  The returned
     * {@link SocketAddress} is supposed to be down-cast into more concrete
     * type such as {@link InetSocketAddress} to retrieve the detailed
     * information.
     *
     * @return the local address of this channel.
     *         {@code null} if this channel is not bound.
     */
    SocketAddress localAddress();

    /**
     * Returns the remote address where this channel is connected to.  The
     * returned {@link SocketAddress} is supposed to be down-cast into more
     * concrete type such as {@link InetSocketAddress} to retrieve the detailed
     * information.
     *
     * @return the remote address of this channel.
     *         {@code null} if this channel is not connected.
     *         If this channel is not connected but it can receive messages
     *         from arbitrary remote addresses (e.g. {@link DatagramChannel},
     *         use {@link DatagramPacket#recipient()} to determine
     *         the origination of the received message as this method will
     *         return {@code null}.
     */
    SocketAddress remoteAddress();

    /**
     * Returns the {@link ChannelFuture} which will be notified when this
     * channel is closed.  This method always returns the same future instance.
     */
    ChannelFuture closeFuture();

    /**
     * Returns {@code true} if and only if the I/O thread will perform the
     * requested write operation immediately.  Any write requests made when
     * this method returns {@code false} are queued until the I/O thread is
     * ready to process the queued write requests.
     *
     * {@link WriteBufferWaterMark} can be used to configure on which condition
     * the write buffer would cause this channel to change writability.
     */
    default boolean isWritable() {
        ChannelOutboundBuffer buf = unsafe().outboundBuffer();
        return buf != null && buf.isWritable();
    }

    /**
     * Get how many bytes can be written until {@link #isWritable()} returns {@code false}.
     * This quantity will always be non-negative. If {@link #isWritable()} is {@code false} then 0.
     *
     * {@link WriteBufferWaterMark} can be used to define writability settings.
     */
    default long bytesBeforeUnwritable() {
        ChannelOutboundBuffer buf = unsafe().outboundBuffer();
        // isWritable() is currently assuming if there is no outboundBuffer then the channel is not writable.
        // We should be consistent with that here.
        return buf != null ? buf.bytesBeforeUnwritable() : 0;
    }

    /**
     * Get how many bytes must be drained from underlying buffers until {@link #isWritable()} returns {@code true}.
     * This quantity will always be non-negative. If {@link #isWritable()} is {@code true} then 0.
     *
     * {@link WriteBufferWaterMark} can be used to define writability settings.
     */
    default long bytesBeforeWritable() {
        ChannelOutboundBuffer buf = unsafe().outboundBuffer();
        // isWritable() is currently assuming if there is no outboundBuffer then the channel is not writable.
        // We should be consistent with that here.
        return buf != null ? buf.bytesBeforeWritable() : Long.MAX_VALUE;
    }

    /**
     * Returns an <em>internal-use-only</em> object that provides unsafe operations.
     * <p>返回仅供传输实现使用的 {@link Unsafe}，用户代码不应调用。</p>
     */
    Unsafe unsafe();

    /**
     * Return the assigned {@link ChannelPipeline}.
     * <p>返回本通道的 {@link ChannelPipeline} 处理器链。</p>
     */
    ChannelPipeline pipeline();

    /**
     * Return the assigned {@link ByteBufAllocator} which will be used to allocate {@link ByteBuf}s.
     * <p>返回分配 {@link ByteBuf} 的分配器，默认取自 {@link #config()}。</p>
     */
    default ByteBufAllocator alloc() {
        return config().getAllocator();
    }

    /**
     * Return the value of the given {@link ChannelOption}
     * <p>读取通道级 {@link ChannelOption} 配置。</p>
     */
    default <T> T getOption(ChannelOption<T> option) {
        return config().getOption(option);
    }

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
     * @return {@code true} if and only if the property has been set
     */
    default <T> boolean setOption(ChannelOption<T> option, T value) {
        return config().setOption(option, value);
    }

    /** 经管道触发读操作。 */
    @Override
    default Channel read() {
        pipeline().read();
        return this;
    }

    /** 经管道刷出待写数据。 */
    @Override
    default Channel flush() {
        pipeline().flush();
        return this;
    }

    /** 经管道写并 flush。 */
    @Override
    default ChannelFuture writeAndFlush(Object msg) {
        return pipeline().writeAndFlush(msg);
    }

    @Override
    default ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return pipeline().writeAndFlush(msg, promise);
    }

    @Override
    default ChannelFuture write(Object msg, ChannelPromise promise) {
        return pipeline().write(msg, promise);
    }

    /** 经管道写出消息。 */
    @Override
    default ChannelFuture write(Object msg) {
        return pipeline().write(msg);
    }

    @Override
    default ChannelFuture deregister(ChannelPromise promise) {
        return pipeline().deregister(promise);
    }

    @Override
    default ChannelFuture close(ChannelPromise promise) {
        return pipeline().close(promise);
    }

    @Override
    default ChannelFuture disconnect(ChannelPromise promise) {
        return pipeline().disconnect(promise);
    }

    @Override
    default ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return pipeline().connect(remoteAddress, localAddress, promise);
    }

    @Override
    default ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return pipeline().connect(remoteAddress, promise);
    }

    @Override
    default ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return pipeline().bind(localAddress, promise);
    }

    @Override
    default ChannelFuture deregister() {
        return pipeline().deregister();
    }

    /** 关闭通道。 */
    @Override
    default ChannelFuture close() {
        return pipeline().close();
    }

    @Override
    default ChannelFuture disconnect() {
        return pipeline().disconnect();
    }

    @Override
    default ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return pipeline().connect(remoteAddress, localAddress);
    }

    /** 连接远端地址。 */
    @Override
    default ChannelFuture connect(SocketAddress remoteAddress) {
        return pipeline().connect(remoteAddress);
    }

    /** 绑定本地地址。 */
    @Override
    default ChannelFuture bind(SocketAddress localAddress) {
        return pipeline().bind(localAddress);
    }

    /** 创建新的 {@link ChannelPromise}。 */
    @Override
    default ChannelPromise newPromise() {
        return pipeline().newPromise();
    }

    @Override
    default ChannelProgressivePromise newProgressivePromise() {
        return pipeline().newProgressivePromise();
    }

    /** 创建已成功完成的 future。 */
    @Override
    default ChannelFuture newSucceededFuture() {
        return pipeline().newSucceededFuture();
    }

    /** 创建已失败的 future。 */
    @Override
    default ChannelFuture newFailedFuture(Throwable cause) {
        return pipeline().newFailedFuture(cause);
    }

    /** 返回 void promise，操作结果不会被单独通知。 */
    @Override
    default ChannelPromise voidPromise() {
        return pipeline().voidPromise();
    }

    /**
     * <em>Unsafe</em> operations that should <em>never</em> be called from user-code. These methods
     * are only provided to implement the actual transport, and must be invoked from an I/O thread except for the
     * following methods:
     * <ul>
     *   <li>{@link #localAddress()}</li>
     *   <li>{@link #remoteAddress()}</li>
     *   <li>{@link #closeForcibly()}</li>
     *   <li>{@link #register(EventLoop, ChannelPromise)}</li>
     *   <li>{@link #deregister(ChannelPromise)}</li>
     *   <li>{@link #voidPromise()}</li>
     * </ul>
     * <p>仅供传输层实现的底层操作接口；除列出的少数方法外须在 I/O 线程调用。</p>
     */
    interface Unsafe {

        /**
         * Return the assigned {@link RecvByteBufAllocator.Handle} which will be used to allocate {@link ByteBuf}'s when
         * receiving data.
         * <p>返回接收数据时分配 {@link ByteBuf} 的句柄。</p>
         */
        RecvByteBufAllocator.Handle recvBufAllocHandle();

        /**
         * Return the {@link SocketAddress} to which is bound local or
         * {@code null} if none.
         * <p>返回本地绑定地址，未绑定时为 {@code null}。</p>
         */
        SocketAddress localAddress();

        /**
         * Return the {@link SocketAddress} to which is bound remote or
         * {@code null} if none is bound yet.
         * <p>返回远端地址，尚未连接/接受时为 {@code null}。</p>
         */
        SocketAddress remoteAddress();

        /**
         * Register the {@link Channel} of the {@link ChannelPromise} and notify
         * the {@link ChannelFuture} once the registration was complete.
         * <p>将通道注册到 {@link EventLoop} 并在完成后通知 promise。</p>
         */
        void register(EventLoop eventLoop, ChannelPromise promise);

        /**
         * Bind the {@link SocketAddress} to the {@link Channel} of the {@link ChannelPromise} and notify
         * it once its done.
         * <p>绑定本地地址并在完成后通知 promise。</p>
         */
        void bind(SocketAddress localAddress, ChannelPromise promise);

        /**
         * Connect the {@link Channel} of the given {@link ChannelFuture} with the given remote {@link SocketAddress}.
         * If a specific local {@link SocketAddress} should be used it need to be given as argument. Otherwise just
         * pass {@code null} to it.
         *
         * The {@link ChannelPromise} will get notified once the connect operation was complete.
         * <p>连接远端地址，可选指定本地地址。</p>
         */
        void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);

        /**
         * Disconnect the {@link Channel} of the {@link ChannelFuture} and notify the {@link ChannelPromise} once the
         * operation was complete.
         * <p>断开连接并在完成后通知 promise。</p>
         */
        void disconnect(ChannelPromise promise);

        /**
         * Close the {@link Channel} of the {@link ChannelPromise} and notify the {@link ChannelPromise} once the
         * operation was complete.
         * <p>关闭通道并在完成后通知 promise。</p>
         */
        void close(ChannelPromise promise);

        /**
         * Closes the {@link Channel} immediately without firing any events.  Probably only useful
         * when registration attempt failed.
         * <p>立即强制关闭，不触发 pipeline 事件（如注册失败时使用）。</p>
         */
        void closeForcibly();

        /**
         * Deregister the {@link Channel} of the {@link ChannelPromise} from {@link EventLoop} and notify the
         * {@link ChannelPromise} once the operation was complete.
         * <p>从 {@link EventLoop} 注销通道。</p>
         */
        void deregister(ChannelPromise promise);

        /**
         * Schedules a read operation that fills the inbound buffer of the first {@link ChannelInboundHandler} in the
         * {@link ChannelPipeline}.  If there's already a pending read operation, this method does nothing.
         * <p>调度读操作填充入站缓冲；已有 pending read 时不重复调度。</p>
         */
        void beginRead();

        /**
         * Schedules a write operation.
         * <p>调度写操作，数据进入出站缓冲。</p>
         */
        void write(Object msg, ChannelPromise promise);

        /**
         * Flush out all write operations scheduled via {@link #write(Object, ChannelPromise)}.
         * <p>刷出所有已通过 {@link #write} 调度的写操作。</p>
         */
        void flush();

        /**
         * Return a special ChannelPromise which can be reused and passed to the operations in {@link Unsafe}.
         * It will never be notified of a success or error and so is only a placeholder for operations
         * that take a {@link ChannelPromise} as argument but for which you not want to get notified.
         * <p>返回可复用的 void promise，永不单独通知成功或失败。</p>
         */
        ChannelPromise voidPromise();

        /**
         * Returns the {@link ChannelOutboundBuffer} of the {@link Channel} where the pending write requests are stored.
         * <p>返回存放 pending 写请求的 {@link ChannelOutboundBuffer}。</p>
         */
        ChannelOutboundBuffer outboundBuffer();
    }
}
