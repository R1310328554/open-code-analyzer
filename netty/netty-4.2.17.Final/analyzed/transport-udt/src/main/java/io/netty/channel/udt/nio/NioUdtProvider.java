/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.udt.nio;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.ChannelUDT;
import com.barchart.udt.nio.KindUDT;
import com.barchart.udt.nio.RendezvousChannelUDT;
import com.barchart.udt.nio.SelectorProviderUDT;
import com.barchart.udt.nio.ServerSocketChannelUDT;
import com.barchart.udt.nio.SocketChannelUDT;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFactory;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.UdtServerChannel;

import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;

/**
 * UDT NIO components provider:
 * <p>
 * Provides {@link ChannelFactory} for UDT channels.
 * <p>
 * Provides {@link SelectorProvider} for UDT channels.
 * <p>UDT 传输工厂与选择器提供者：按 {@link TypeUDT}（STREAM/DATAGRAM） 与 {@link KindUDT}（ACCEPTOR/CONNECTOR/RENDEZVOUS）组合暴露 六个 {@link ChannelFactory} 常量及 BYTE/MESSAGE {@link SelectorProvider}。 亦提供底层 {@link ChannelUDT}/{@link SocketUDT} 访问以便监控调试。</p>
 *
 * @deprecated The UDT transport is no longer maintained and will be removed.
 */
@Deprecated
public final class NioUdtProvider<T extends UdtChannel> implements ChannelFactory<T> {

    /**
     * {@link ChannelFactory} for UDT Byte Acceptor. See {@link TypeUDT#STREAM}
     * and {@link KindUDT#ACCEPTOR}.
     * <p>字节流 Acceptor 工厂（{@link NioUdtByteAcceptorChannel}）。</p>
     */
    public static final ChannelFactory<UdtServerChannel> BYTE_ACCEPTOR = new NioUdtProvider<UdtServerChannel>(
            TypeUDT.STREAM, KindUDT.ACCEPTOR);

    /**
     * {@link ChannelFactory} for UDT Byte Connector. See {@link TypeUDT#STREAM}
     * and {@link KindUDT#CONNECTOR}.
     * <p>字节流 Connector 工厂（{@link NioUdtByteConnectorChannel}）。</p>
     */
    public static final ChannelFactory<UdtChannel> BYTE_CONNECTOR = new NioUdtProvider<UdtChannel>(
            TypeUDT.STREAM, KindUDT.CONNECTOR);

    /**
     * {@link SelectorProvider} for UDT Byte channels. See
     * {@link TypeUDT#STREAM}.
     * <p>STREAM 类型 UDT 的 {@link SelectorProviderUDT}。</p>
     */
    public static final SelectorProvider BYTE_PROVIDER = SelectorProviderUDT.STREAM;

    /**
     * {@link ChannelFactory} for UDT Byte Rendezvous. See
     * {@link TypeUDT#STREAM} and {@link KindUDT#RENDEZVOUS}.
     * <p>字节流 Rendezvous 工厂（{@link NioUdtByteRendezvousChannel}）。</p>
     */
    public static final ChannelFactory<UdtChannel> BYTE_RENDEZVOUS = new NioUdtProvider<UdtChannel>(
            TypeUDT.STREAM, KindUDT.RENDEZVOUS);

    /**
     * {@link ChannelFactory} for UDT Message Acceptor. See
     * {@link TypeUDT#DATAGRAM} and {@link KindUDT#ACCEPTOR}.
     * <p>DATAGRAM 消息 Acceptor 工厂（{@link NioUdtMessageAcceptorChannel}）。</p>
     */
    public static final ChannelFactory<UdtServerChannel> MESSAGE_ACCEPTOR = new NioUdtProvider<UdtServerChannel>(
            TypeUDT.DATAGRAM, KindUDT.ACCEPTOR);

    /**
     * {@link ChannelFactory} for UDT Message Connector. See
     * {@link TypeUDT#DATAGRAM} and {@link KindUDT#CONNECTOR}.
     * <p>DATAGRAM 消息 Connector 工厂（{@link NioUdtMessageConnectorChannel}）。</p>
     */
    public static final ChannelFactory<UdtChannel> MESSAGE_CONNECTOR = new NioUdtProvider<UdtChannel>(
            TypeUDT.DATAGRAM, KindUDT.CONNECTOR);

    /**
     * {@link SelectorProvider} for UDT Message channels. See
     * {@link TypeUDT#DATAGRAM}.
     * <p>DATAGRAM 类型 UDT 的 {@link SelectorProviderUDT}。</p>
     */
    public static final SelectorProvider MESSAGE_PROVIDER = SelectorProviderUDT.DATAGRAM;

    /**
     * {@link ChannelFactory} for UDT Message Rendezvous. See
     * {@link TypeUDT#DATAGRAM} and {@link KindUDT#RENDEZVOUS}.
     * <p>DATAGRAM Rendezvous 工厂（{@link NioUdtMessageRendezvousChannel}）。</p>
     */
    public static final ChannelFactory<UdtChannel> MESSAGE_RENDEZVOUS = new NioUdtProvider<UdtChannel>(
            TypeUDT.DATAGRAM, KindUDT.RENDEZVOUS);

    /**
     * Expose underlying {@link ChannelUDT} for debugging and monitoring.
     * <p>从 Netty {@link UdtChannel} 实现提取底层 barchart {@link ChannelUDT}； 非 UDT 通道返回 {@code null}。</p>
     * <p>
     * @return underlying {@link ChannelUDT} or null, if parameter is not
     *         {@link UdtChannel}
     */
    public static ChannelUDT channelUDT(final Channel channel) {
        // 字节流 Acceptor/Connector/Rendezvous
        if (channel instanceof NioUdtByteAcceptorChannel) {
            return ((NioUdtByteAcceptorChannel) channel).javaChannel();
        }
        if (channel instanceof NioUdtByteRendezvousChannel) {
            return ((NioUdtByteRendezvousChannel) channel).javaChannel();
        }
        if (channel instanceof NioUdtByteConnectorChannel) {
            return ((NioUdtByteConnectorChannel) channel).javaChannel();
        }

        // 消息模式 Acceptor/Connector/Rendezvous
        if (channel instanceof NioUdtMessageAcceptorChannel) {
            return ((NioUdtMessageAcceptorChannel) channel).javaChannel();
        }
        if (channel instanceof NioUdtMessageRendezvousChannel) {
            return ((NioUdtMessageRendezvousChannel) channel).javaChannel();
        }
        if (channel instanceof NioUdtMessageConnectorChannel) {
            return ((NioUdtMessageConnectorChannel) channel).javaChannel();
        }

        return null;
    }

    /**
     * Convenience factory for {@link KindUDT#ACCEPTOR} channels.
     * <p>打开 {@link ServerSocketChannelUDT}，失败包装为 {@link ChannelException}。</p>
     */
    static ServerSocketChannelUDT newAcceptorChannelUDT(
            final TypeUDT type) {
        try {
            return SelectorProviderUDT.from(type).openServerSocketChannel();
        } catch (final IOException e) {
            throw new ChannelException("failed to open a server socket channel", e);
        }
    }

    /**
     * Convenience factory for {@link KindUDT#CONNECTOR} channels.
     * <p>打开 {@link SocketChannelUDT}。</p>
     */
    static SocketChannelUDT newConnectorChannelUDT(final TypeUDT type) {
        try {
            return SelectorProviderUDT.from(type).openSocketChannel();
        } catch (final IOException e) {
            throw new ChannelException("failed to open a socket channel", e);
        }
    }

    /**
     * Convenience factory for {@link KindUDT#RENDEZVOUS} channels.
     * <p>打开 {@link RendezvousChannelUDT} 用于对称连接。</p>
     */
    static RendezvousChannelUDT newRendezvousChannelUDT(
            final TypeUDT type) {
        try {
            return SelectorProviderUDT.from(type).openRendezvousChannel();
        } catch (final IOException e) {
            throw new ChannelException("failed to open a rendezvous channel", e);
        }
    }

    /**
     * Expose underlying {@link SocketUDT} for debugging and monitoring.
     * <p>经 {@link #channelUDT} 取得 {@link ChannelUDT} 再返回其 {@link SocketUDT}。</p>
     * <p>
     * @return underlying {@link SocketUDT} or null, if parameter is not
     *         {@link UdtChannel}
     */
    public static SocketUDT socketUDT(final Channel channel) {
        final ChannelUDT channelUDT = channelUDT(channel);
        if (channelUDT == null) {
            return null;
        } else {
            return channelUDT.socketUDT();
        }
    }

    private final KindUDT kind;
    private final TypeUDT type;

    /**
     * {@link ChannelFactory} for given {@link TypeUDT} and {@link KindUDT}
     * <p>私有构造：保存 type/kind，由静态常量实例化。</p>
     */
    private NioUdtProvider(final TypeUDT type, final KindUDT kind) {
        this.type = type;
        this.kind = kind;
    }

    /**
     * UDT Channel Kind. See {@link KindUDT}
     * <p>返回本工厂对应的 ACCEPTOR/CONNECTOR/RENDEZVOUS。</p>
     */
    public KindUDT kind() {
        return kind;
    }

    /**
     * Produce new {@link UdtChannel} based on factory {@link #kind()} and
     * {@link #type()}
     * <p>按 kind+type 组合实例化对应 Netty UDT 通道实现。</p>
     */
    @SuppressWarnings("unchecked")
    @Override
    public T newChannel() {
        switch (kind) {
            case ACCEPTOR:
                switch (type) {
                    case DATAGRAM:
                        return (T) new NioUdtMessageAcceptorChannel();
                    case STREAM:
                        return (T) new NioUdtByteAcceptorChannel();
                    default:
                        throw new IllegalStateException("wrong type=" + type);
                }
            case CONNECTOR:
                switch (type) {
                    case DATAGRAM:
                        return (T) new NioUdtMessageConnectorChannel();
                    case STREAM:
                        return (T) new NioUdtByteConnectorChannel();
                    default:
                        throw new IllegalStateException("wrong type=" + type);
                }
            case RENDEZVOUS:
                switch (type) {
                    case DATAGRAM:
                        return (T) new NioUdtMessageRendezvousChannel();
                    case STREAM:
                        return (T) new NioUdtByteRendezvousChannel();
                    default:
                        throw new IllegalStateException("wrong type=" + type);
                }
            default:
                throw new IllegalStateException("wrong kind=" + kind);
        }
    }

    /**
     * UDT Socket Type. See {@link TypeUDT}
     * <p>返回 STREAM 或 DATAGRAM。</p>
     */
    public TypeUDT type() {
        return type;
    }
}
