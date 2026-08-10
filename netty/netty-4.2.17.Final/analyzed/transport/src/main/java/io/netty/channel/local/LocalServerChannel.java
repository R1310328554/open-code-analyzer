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
package io.netty.channel.local;

import io.netty.channel.AbstractServerChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.EventLoop;
import io.netty.channel.IoEvent;
import io.netty.channel.IoEventLoop;
import io.netty.channel.IoEventLoopGroup;
import io.netty.channel.IoRegistration;
import io.netty.channel.PreferHeapByteBufAllocator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.ServerChannel;
import io.netty.channel.ServerChannelRecvByteBufAllocator;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.util.concurrent.SingleThreadEventExecutor;

import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A {@link ServerChannel} for the local transport which allows in VM communication.
 * <p>本地传输的 {@link ServerChannel}，用于 JVM 内进程间通信，接受 {@link LocalChannel} 连接。</p>
 */
public class LocalServerChannel extends AbstractServerChannel {

    /** channel 配置 */
    private final ChannelConfig config =
            new DefaultChannelConfig(this, new ServerChannelRecvByteBufAllocator()) { };
    /** 待 accept 的入站 {@link LocalChannel} 缓冲队列 */
    private final Queue<Object> inboundBuffer = new ArrayDeque<Object>();

    /** 在 {@link IoEventLoop} 上注册后得到的 I/O 注册句柄 */
    private IoRegistration registration;

    /** 生命周期状态：0 打开 / 1 激活 / 2 已关闭 */
    private volatile int state; // 0 - open, 1 - active, 2 - closed
    /** 绑定后的本地地址 */
    private volatile LocalAddress localAddress;
    /** 是否正在等待新的 accept（入站缓冲为空时置 true） */
    private volatile boolean acceptInProgress;

    public LocalServerChannel() {
        config().setAllocator(new PreferHeapByteBufAllocator(config.getAllocator()));
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    @Override
    public LocalAddress localAddress() {
        return (LocalAddress) super.localAddress();
    }

    @Override
    public LocalAddress remoteAddress() {
        return (LocalAddress) super.remoteAddress();
    }

    @Override
    public boolean isOpen() {
        return state < 2;
    }

    @Override
    public boolean isActive() {
        return state == 1;
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof SingleThreadEventLoop ||
                (loop instanceof IoEventLoop && ((IoEventLoop) loop).isCompatible(LocalServerUnsafe.class));
    }

    @Override
    protected SocketAddress localAddress0() {
        return localAddress;
    }

    @Override
    protected void doRegister(ChannelPromise promise) {
        EventLoop loop = eventLoop();
        if (loop instanceof IoEventLoop) {
            assert registration == null;
            ((IoEventLoop) loop).register((LocalServerUnsafe) unsafe()).addListener(f -> {
                if (f.isSuccess()) {
                    registration = (IoRegistration) f.getNow();
                    promise.setSuccess();
                } else {
                    promise.setFailure(f.cause());
                }
            });
        } else {
            try {
                ((LocalServerUnsafe) unsafe()).registered();
            } catch (Throwable cause) {
                promise.setFailure(cause);
                return;
            }
            promise.setSuccess();
        }
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        this.localAddress = LocalChannelRegistry.register(this, this.localAddress, localAddress);
        state = 1;
    }

    @Override
    protected void doClose() throws Exception {
        if (state <= 1) {
            // Update all internal state before the closeFuture is notified.
            if (localAddress != null) {
                LocalChannelRegistry.unregister(localAddress);
                localAddress = null;
            }
            state = 2;
        }
    }

    @Override
    protected void doDeregister() throws Exception {
        EventLoop loop = eventLoop();
        if (loop instanceof IoEventLoop) {
            IoRegistration registration = this.registration;
            if (registration != null) {
                this.registration = null;
                registration.cancel();
            }
        } else {
            ((LocalServerUnsafe) unsafe()).unregistered();
        }
    }

    @Override
    protected void doBeginRead() throws Exception {
        if (acceptInProgress) {
            return;
        }

        Queue<Object> inboundBuffer = this.inboundBuffer;
        if (inboundBuffer.isEmpty()) {
            acceptInProgress = true;
            return;
        }

        readInbound();
    }

    /** 接受本地连接并创建子 channel */
    LocalChannel serve(final LocalChannel peer) {
        final LocalChannel child = newLocalChannel(peer);
        if (eventLoop().inEventLoop()) {
            serve0(child);
        } else {
            eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    serve0(child);
                }
            });
        }
        return child;
    }

    /** 将 inboundBuffer 中的对象 fireChannelRead 到 pipeline */
    private void readInbound() {
        RecvByteBufAllocator.Handle handle = unsafe().recvBufAllocHandle();
        handle.reset(config());
        ChannelPipeline pipeline = pipeline();
        do {
            Object m = inboundBuffer.poll();
            if (m == null) {
                break;
            }
            pipeline.fireChannelRead(m);
        } while (handle.continueReading());
        handle.readComplete();
        pipeline.fireChannelReadComplete();
    }

    /**
     * A factory method for {@link LocalChannel}s. Users may override it
     * to create custom instances of {@link LocalChannel}s.
     * <p>创建子 {@link LocalChannel} 的工厂方法，子类可覆盖以返回自定义实现。</p>
     */
    protected LocalChannel newLocalChannel(LocalChannel peer) {
        return new LocalChannel(this, peer);
    }

    /** 在 EventLoop 上将 peer 放入 inbound 并触发 read */
    private void serve0(final LocalChannel child) {
        inboundBuffer.add(child);
        if (acceptInProgress) {
            acceptInProgress = false;

            readInbound();
        }
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new LocalServerUnsafe();
    }

    private class LocalServerUnsafe extends AbstractUnsafe implements LocalIoHandle {
        /** EventLoop 关闭时自动 close 的钩子 */
        private final Runnable shutdownHook = this::closeNow;

        @Override
        public void close() {
            close(voidPromise());
        }

        @Override
        public void handle(IoRegistration registration, IoEvent event) {
            // NOOP
        }

        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            safeSetFailure(promise, new UnsupportedOperationException());
        }

        @Override
        public void registered() {
            EventLoop loop = eventLoop();
            if (!(loop instanceof IoEventLoop) && loop instanceof SingleThreadEventExecutor) {
                ((SingleThreadEventExecutor) eventLoop()).addShutdownHook(shutdownHook);
            }
        }

        @Override
        public void unregistered() {
            EventLoop loop = eventLoop();
            if (!(loop instanceof IoEventLoop) && loop instanceof SingleThreadEventExecutor) {
                ((SingleThreadEventExecutor) eventLoop()).removeShutdownHook(shutdownHook);
            }
        }

        @Override
        public void closeNow() {
            close(voidPromise());
        }
    }
}
