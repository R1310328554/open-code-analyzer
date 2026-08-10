/*
 * Copyright 2017 The Netty Project
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
package io.netty.handler.ssl;

import static io.netty.handler.ssl.SslUtils.toSSLHandshakeException;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static java.lang.Math.min;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.JdkApplicationProtocolNegotiator.ProtocolSelectionListener;
import io.netty.handler.ssl.JdkApplicationProtocolNegotiator.ProtocolSelector;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.SystemPropertyUtil;
import org.conscrypt.AllocatedBuffer;
import org.conscrypt.BufferAllocator;
import org.conscrypt.Conscrypt;
import org.conscrypt.HandshakeListener;

/**
 * A {@link JdkSslEngine} that uses the Conscrypt provider or SSL with ALPN.
 *
 * <p>基于 Conscrypt 的 {@link JdkSslEngine} 子类，集成 ALPN 协议协商与 Netty {@link ByteBufAllocator} 缓冲适配。</p>
 */
abstract class ConscryptAlpnSslEngine extends JdkSslEngine {
    /** 是否让 Conscrypt 通过 Netty 分配器申请直接缓冲（默认 true，权衡内存与 JNI 性能）。 */
    private static final boolean USE_BUFFER_ALLOCATOR = SystemPropertyUtil.getBoolean(
            "io.netty.handler.ssl.conscrypt.useBufferAllocator", true);

    /** 工厂：创建客户端 ALPN 引擎。 */
    static ConscryptAlpnSslEngine newClientEngine(SSLEngine engine, ByteBufAllocator alloc,
            JdkApplicationProtocolNegotiator applicationNegotiator) {
        return new ClientEngine(engine, alloc, applicationNegotiator);
    }

    /** 工厂：创建服务端 ALPN 引擎。 */
    static ConscryptAlpnSslEngine newServerEngine(SSLEngine engine, ByteBufAllocator alloc,
            JdkApplicationProtocolNegotiator applicationNegotiator) {
        return new ServerEngine(engine, alloc, applicationNegotiator);
    }

    private ConscryptAlpnSslEngine(SSLEngine engine, ByteBufAllocator alloc, List<String> protocols) {
        super(engine);

        // 配置 Conscrypt 缓冲分配策略：Netty 分配器 vs 引擎内部分配直接缓冲
        // performance.
        //
        // If no allocator is provided, the engine will internally allocate a direct buffer of max packet size in
        // order to optimize JNI calls (this happens the first time it is provided a non-direct buffer from the
        // application).
        //
        // Alternatively, if an allocator is provided, no internal buffer will be created and direct buffers will be
        // retrieved from the allocator on-demand.
        if (USE_BUFFER_ALLOCATOR) {
            Conscrypt.setBufferAllocator(engine, new BufferAllocatorAdapter(alloc));
        }

        // 在引擎上注册支持的 ALPN 协议列表
        Conscrypt.setApplicationProtocols(engine, protocols.toArray(EmptyArrays.EMPTY_STRINGS));
    }

    /**
     * Calculates the maximum size of the encrypted output buffer required to wrap the given plaintext bytes. Assumes
     * as a worst case that there is one TLS record per buffer.
     *
     * @param plaintextBytes the number of plaintext bytes to be wrapped.
     * @param numBuffers the number of buffers that the plaintext bytes are spread across.
     * @return the maximum size of the encrypted output buffer required for the wrap operation.
     */
    final int calculateOutNetBufSize(int plaintextBytes, int numBuffers) {
        // 复合缓冲最坏情况每组件一条 TLS 记录，累加 seal 开销
        return calculateSpace(plaintextBytes, numBuffers, Integer.MAX_VALUE);
    }

    /**
     * Calculate the space necessary in an out buffer to hold the max size that the given
     * plaintextBytes and numBuffers can produce when encrypted. Assumes as a worst case
     * that there is one TLS record per buffer.
     * @param plaintextBytes the number of plaintext bytes to be wrapped.
     * @param numBuffers the number of buffers that the plaintext bytes are spread across.
     * @return the maximum size of the encrypted output buffer required for the wrap operation.
     */
    final int calculateRequiredOutBufSpace(int plaintextBytes, int numBuffers) {
        return calculateSpace(plaintextBytes, numBuffers, Conscrypt.maxEncryptedPacketLength());
    }

    private int calculateSpace(int plaintextBytes, int numBuffers, long maxPacketLength) {
         long maxOverhead = (long) Conscrypt.maxSealOverhead(getWrappedEngine()) * numBuffers;
         return (int) min(maxPacketLength, plaintextBytes + maxOverhead);
    }

    /** 委托 Conscrypt 多缓冲 unwrap。 */
    final SSLEngineResult unwrap(ByteBuffer[] srcs, ByteBuffer[] dests) throws SSLException {
        return Conscrypt.unwrap(getWrappedEngine(), srcs, dests);
    }

    /** 客户端：握手完成后通过 ProtocolSelectionListener 通知选定协议。 */
    private static final class ClientEngine extends ConscryptAlpnSslEngine {
        private final ProtocolSelectionListener protocolListener;

        ClientEngine(SSLEngine engine, ByteBufAllocator alloc,
                JdkApplicationProtocolNegotiator applicationNegotiator) {
            super(engine, alloc, applicationNegotiator.protocols());
            // 注册握手完成回调以触发 ALPN 协议选择
            Conscrypt.setHandshakeListener(engine, new HandshakeListener() {
                @Override
                public void onHandshakeFinished() throws SSLException {
                    selectProtocol();
                }
            });

            protocolListener = checkNotNull(applicationNegotiator
                            .protocolListenerFactory().newListener(this, applicationNegotiator.protocols()),
                    "protocolListener");
        }

        private void selectProtocol() throws SSLException {
            String protocol = Conscrypt.getApplicationProtocol(getWrappedEngine());
            try {
                protocolListener.selected(protocol);
            } catch (Throwable e) {
                throw toSSLHandshakeException(e);
            }
        }
    }

    /** 服务端：握手完成后由 ProtocolSelector 从对端 ALPN 列表中选择协议。 */
    private static final class ServerEngine extends ConscryptAlpnSslEngine {
        private final ProtocolSelector protocolSelector;

        ServerEngine(SSLEngine engine, ByteBufAllocator alloc,
                     JdkApplicationProtocolNegotiator applicationNegotiator) {
            super(engine, alloc, applicationNegotiator.protocols());

            // Register for completion of the handshake.
            Conscrypt.setHandshakeListener(engine, new HandshakeListener() {
                @Override
                public void onHandshakeFinished() throws SSLException {
                    selectProtocol();
                }
            });

            protocolSelector = checkNotNull(applicationNegotiator.protocolSelectorFactory()
                            .newSelector(this,
                                    new LinkedHashSet<String>(applicationNegotiator.protocols())),
                    "protocolSelector");
        }

        private void selectProtocol() throws SSLException {
            try {
                String protocol = Conscrypt.getApplicationProtocol(getWrappedEngine());
                protocolSelector.select(protocol != null ? Collections.singletonList(protocol)
                        : Collections.<String>emptyList());
            } catch (Throwable e) {
                throw toSSLHandshakeException(e);
            }
        }
    }

    /** 将 Conscrypt {@link BufferAllocator} 桥接到 Netty {@link ByteBufAllocator}。 */
    private static final class BufferAllocatorAdapter extends BufferAllocator {
        private final ByteBufAllocator alloc;

        BufferAllocatorAdapter(ByteBufAllocator alloc) {
            this.alloc = alloc;
        }

        @Override
        public AllocatedBuffer allocateDirectBuffer(int capacity) {
            return new BufferAdapter(alloc.directBuffer(capacity));
        }
    }

    /** 包装 Netty {@link ByteBuf} 为 Conscrypt {@link AllocatedBuffer}，支持 retain/release。 */
    private static final class BufferAdapter extends AllocatedBuffer {
        private final ByteBuf nettyBuffer;
        private final ByteBuffer buffer;

        BufferAdapter(ByteBuf nettyBuffer) {
            this.nettyBuffer = nettyBuffer;
            buffer = nettyBuffer.nioBuffer(0, nettyBuffer.capacity());
        }

        @Override
        public ByteBuffer nioBuffer() {
            return buffer;
        }

        @Override
        public AllocatedBuffer retain() {
            nettyBuffer.retain();
            return this;
        }

        @Override
        public AllocatedBuffer release() {
            nettyBuffer.release();
            return this;
        }
    }
}
