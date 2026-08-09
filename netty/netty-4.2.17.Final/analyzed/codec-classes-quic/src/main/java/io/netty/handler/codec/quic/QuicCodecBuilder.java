/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.quic;

import io.netty.channel.ChannelHandler;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.netty.util.internal.ObjectUtil.checkInRange;
import static io.netty.util.internal.ObjectUtil.checkPositive;
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * {@code QUIC} 编解码器构建器的抽象基类，封装 Quiche 传输参数与 SSL 配置。
 *
 * @param <B> the type of the {@link QuicCodecBuilder}.
 */
public abstract class QuicCodecBuilder<B extends QuicCodecBuilder<B>> {
    private final boolean server;
    private Boolean grease;
    private Long maxIdleTimeout;
    private Long maxRecvUdpPayloadSize;
    private Long maxSendUdpPayloadSize;
    private Long initialMaxData;
    private Long initialMaxStreamDataBidiLocal;
    private Long initialMaxStreamDataBidiRemote;
    private Long initialMaxStreamDataUni;
    private Long initialMaxStreamsBidi;
    private Long initialMaxStreamsUni;
    private Long ackDelayExponent;
    private Long maxAckDelay;
    private Boolean disableActiveMigration;
    private Boolean enableHystart;
    private Boolean discoverPmtu;
    private QuicCongestionControlAlgorithm congestionControlAlgorithm;
    private Integer initialCongestionWindowPackets;
    private int localConnIdLength;
    private Function<QuicChannel, ? extends QuicSslEngine> sslEngineProvider;
    private FlushStrategy flushStrategy = FlushStrategy.DEFAULT;
    private Integer recvQueueLen;
    private Integer sendQueueLen;
    private Long activeConnectionIdLimit;
    private byte[] statelessResetToken;

    private Executor sslTaskExecutor;

    /** 仅包内/测试可见：协商使用的 QUIC 协议版本。 */
    int version;

    QuicCodecBuilder(boolean server) {
        Quic.ensureAvailability();
        this.version = Quiche.QUICHE_PROTOCOL_VERSION;
        this.localConnIdLength = Quiche.QUICHE_MAX_CONN_ID_LEN;
        this.server = server;
    }

    QuicCodecBuilder(QuicCodecBuilder<B> builder) {
        Quic.ensureAvailability();
        this.server = builder.server;
        this.grease = builder.grease;
        this.maxIdleTimeout = builder.maxIdleTimeout;
        this.maxRecvUdpPayloadSize = builder.maxRecvUdpPayloadSize;
        this.maxSendUdpPayloadSize = builder.maxSendUdpPayloadSize;
        this.initialMaxData = builder.initialMaxData;
        this.initialMaxStreamDataBidiLocal = builder.initialMaxStreamDataBidiLocal;
        this.initialMaxStreamDataBidiRemote = builder.initialMaxStreamDataBidiRemote;
        this.initialMaxStreamDataUni = builder.initialMaxStreamDataUni;
        this.initialMaxStreamsBidi = builder.initialMaxStreamsBidi;
        this.initialMaxStreamsUni = builder.initialMaxStreamsUni;
        this.ackDelayExponent = builder.ackDelayExponent;
        this.maxAckDelay = builder.maxAckDelay;
        this.disableActiveMigration = builder.disableActiveMigration;
        this.enableHystart = builder.enableHystart;
        this.discoverPmtu = builder.discoverPmtu;
        this.congestionControlAlgorithm = builder.congestionControlAlgorithm;
        this.initialCongestionWindowPackets = builder.initialCongestionWindowPackets;
        this.localConnIdLength = builder.localConnIdLength;
        this.sslEngineProvider = builder.sslEngineProvider;
        this.flushStrategy = builder.flushStrategy;
        this.recvQueueLen = builder.recvQueueLen;
        this.sendQueueLen = builder.sendQueueLen;
        this.activeConnectionIdLimit = builder.activeConnectionIdLimit;
        this.statelessResetToken = builder.statelessResetToken;
        this.sslTaskExecutor = builder.sslTaskExecutor;
        this.version = builder.version;
    }

    /**
     * 返回构建器自身，供链式调用。
     *
     * @return itself.
     */
    @SuppressWarnings("unchecked")
    protected final B self() {
        return (B) this;
    }

    /**
     * 设置自动 flush 检测策略 {@link FlushStrategy}。
     *
     * @param flushStrategy   the strategy to use.
     * @return                the instance itself.
     */
    public final B flushStrategy(FlushStrategy flushStrategy) {
        this.flushStrategy = Objects.requireNonNull(flushStrategy, "flushStrategy");
        return self();
    }

    /**
     * 设置拥塞控制算法，默认为 {@link QuicCongestionControlAlgorithm#CUBIC}。
     *
     * @param congestionControlAlgorithm    the {@link QuicCongestionControlAlgorithm} to use.
     * @return                              the instance itself.
     */
    public final B congestionControlAlgorithm(QuicCongestionControlAlgorithm congestionControlAlgorithm) {
        this.congestionControlAlgorithm = congestionControlAlgorithm;
        return self();
    }

    /**
     * 以报文个数设置初始拥塞窗口大小，默认 10 个报文。
     *
     * @param numPackets number of packets for the initial congestion window
     * @return
     */
    public final B initialCongestionWindowPackets(int numPackets) {
        this.initialCongestionWindowPackets = numPackets;
        return self();
    }

    /**
     * 是否启用 <a href="https://tools.ietf.org/html/draft-thomson-quic-bit-grease-00">GREASE</a> 位填充，默认 {@code true}。
     *
     * @param enable    {@code true} if enabled, {@code false} otherwise.
     * @return          the instance itself.
     */
    public final B grease(boolean enable) {
        grease = enable;
        return self();
    }

    /**
     * See <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.set_max_idle_timeout">
     *     set_max_idle_timeout</a>.
     *
     * 默认无超时（无限空闲时间）。
     *
     * @param amount    the maximum idle timeout.
     * @param unit      the {@link TimeUnit}.
     * @return          the instance itself.
     */
    public final B maxIdleTimeout(long amount, TimeUnit unit) {
        this.maxIdleTimeout = unit.toMillis(checkPositiveOrZero(amount, "amount"));
        return self();
    }

    /**
     * See <a href="https://github.com/cloudflare/quiche/blob/35e38d987c1e53ef2bd5f23b754c50162b5adac8/src/lib.rs#L669">
     *     set_max_send_udp_payload_size</a>.
     *
     * 默认且最小值为 1200 字节。
     *
     * @param size    the maximum payload size that is advertised to the remote peer.
     * @return        the instance itself.
     */
    public final B maxSendUdpPayloadSize(long size) {
        this.maxSendUdpPayloadSize = checkPositiveOrZero(size, "value");
        return self();
    }

    /**
     * See <a href="https://github.com/cloudflare/quiche/blob/35e38d987c1e53ef2bd5f23b754c50162b5adac8/src/lib.rs#L662">
     *     set_max_recv_udp_payload_size</a>.
     *
     * 默认值为 65527 字节。
     *
     * @param size    the maximum payload size that is advertised to the remote peer.
     * @return        the instance itself.
     */
    public final B maxRecvUdpPayloadSize(long size) {
        this.maxRecvUdpPayloadSize = checkPositiveOrZero(size, "value");
        return self();
    }

    /**
     * See <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.set_initial_max_data">
     *     set_initial_max_data</a>.
     *
     * 默认值为 0。
     *
     * @param value   the initial maximum data limit.
     * @return        the instance itself.
     */
    public final B initialMaxData(long value) {
        this.initialMaxData = checkPositiveOrZero(value, "value");
        return self();
    }

    /**
     * See
     * <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.set_initial_max_stream_data_bidi_local">
     *     set_initial_max_stream_data_bidi_local</a>.
     *
     * The default value is 0.
     *
     * @param value   the initial maximum data limit for local bidirectional streams.
     * @return        the instance itself.
     */
    public final B initialMaxStreamDataBidirectionalLocal(long value) {
        this.initialMaxStreamDataBidiLocal = checkPositiveOrZero(value, "value");
        return self();
    }

    /**
     * See
     * <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.set_initial_max_stream_data_bidi_remote">
     *     set_initial_max_stream_data_bidi_remote</a>.
     *
     * The default value is 0.
     *
     * @param value   the initial maximum data limit for remote bidirectional streams.
     * @return        the instance itself.
     */
    public final B initialMaxStreamDataBidirectionalRemote(long value) {
        this.initialMaxStreamDataBidiRemote = checkPositiveOrZero(value, "value");
        return self();
    }

    /**
     * See
     * <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.set_initial_max_stream_data_uni">
     *     set_initial_max_stream_data_uni</a>.
     *
     * The default value is 0.
     *
     * @param value   the initial maximum data limit for unidirectional streams.
     * @return        the instance itself.
     */
    public final B initialMaxStreamDataUnidirectional(long value) {
        this.initialMaxStreamDataUni = checkPositiveOrZero(value, "value");
        return self();
    }

    /**
     * See
     * <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.set_initial_max_streams_bidi">
     *     set_initial_max_streams_bidi</a>.
     *
     * The default value is 0.
     *
     * @param value   the initial maximum stream limit for bidirectional streams.
     * @return        the instance itself.
     */
    public final B initialMaxStreamsBidirectional(long value) {
        this.initialMaxStreamsBidi = checkPositiveOrZero(value, "value");
        return self();
    }

    /**
     * See
     * <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.set_initial_max_streams_uni">
     *     set_initial_max_streams_uni</a>.
     *
     * The default value is 0.
     *
     * @param value   the initial maximum stream limit for unidirectional streams.
     * @return        the instance itself.
     */
    public final B initialMaxStreamsUnidirectional(long value) {
        this.initialMaxStreamsUni = checkPositiveOrZero(value, "value");
        return self();
    }

    /**
     * See
     * <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.set_ack_delay_exponent">
     *     set_ack_delay_exponent</a>.
     *
     * 默认值为 3。
     *
     * @param value   the delay exponent used for ACKs.
     * @return        the instance itself.
     */
    public final B ackDelayExponent(long value) {
        this.ackDelayExponent = checkPositiveOrZero(value, "value");
        return self();
    }

    /**
     * See
     * <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.set_max_ack_delay">
     *     set_max_ack_delay</a>.
     *
     * 默认 25 毫秒。
     *
     * @param amount    the max ack delay.
     * @param unit      the {@link TimeUnit}.
     * @return          the instance itself.
     */
    public final B maxAckDelay(long amount, TimeUnit unit) {
        this.maxAckDelay = unit.toMillis(checkPositiveOrZero(amount, "amount"));
        return self();
    }

    /**
     * See
     * <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.set_disable_active_migration">
     *     set_disable_active_migration</a>.
     *
     * 默认禁用主动迁移（{@code disable_active_migration=true}）。
     *
     * @param enable  {@code true} if migration should be enabled, {@code false} otherwise.
     * @return        the instance itself.
     */
    public final B activeMigration(boolean enable) {
        this.disableActiveMigration = !enable;
        return self();
    }

    /**
     * See
     * <a href="https://docs.rs/quiche/0.6.0/quiche/struct.Config.html#method.enable_hystart">
     *     enable_hystart</a>.
     *
     * 默认启用 Hystart 慢启动优化。
     *
     * @param enable  {@code true} if Hystart should be enabled.
     * @return        the instance itself.
     */
    public final B hystart(boolean enable) {
        this.enableHystart = enable;
        return self();
    }

    /**
     * See
     * <a href="https://docs.rs/quiche/latest/quiche/struct.Config.html#method.discover_pmtu">
     *     discover_pmtu</a>.
     *
     * 是否启用路径 MTU 发现（PMTU），默认 {@code false}。
     *
     * @param enable  {@code true} if path MTU discovery should be enabled.
     * @return        the instance itself.
     */
    public final B discoverPmtu(boolean enable) {
        this.discoverPmtu = enable;
        return self();
    }

    /**
     * 设置本地生成的连接 ID 长度，默认 20（亦为支持的最大值）。
     *
     * @param value   the length of local generated connections ids.
     * @return        the instance itself.
     */
    public final B localConnectionIdLength(int value) {
        this.localConnIdLength = checkInRange(value, 0, Quiche.QUICHE_MAX_CONN_ID_LEN,  "value");
        return self();
    }

    /**
     * 配置使用的 {@code QUIC} 协议版本，默认为底层库支持的最新版本。
     *
     * @param version the {@code QUIC version} to use.
     * @return        the instance itself.
     */
    public final B version(int version) {
        this.version = version;
        return self();
    }

    /**
     * 启用 <a href="https://tools.ietf.org/html/draft-ietf-quic-datagram-01">Datagram 扩展</a> 并配置收发队列长度。
     * @param recvQueueLen  the RECV queue length.
     * @param sendQueueLen  the SEND queue length.
     * @return              the instance itself.
     */
    public final B datagram(int recvQueueLen, int sendQueueLen) {
        checkPositive(recvQueueLen, "recvQueueLen");
        checkPositive(sendQueueLen, "sendQueueLen");

        this.recvQueueLen = recvQueueLen;
        this.sendQueueLen = sendQueueLen;
        return self();
    }

    /**
     * 指定用于为 {@link QuicChannel} 创建 {@link QuicSslEngine} 的 {@link QuicSslContext}。
     * 更灵活时可改用 {@link #sslEngineProvider(Function)}。
     *
     * @param sslContext    the context.
     * @return              the instance itself.
     */
    public final B sslContext(QuicSslContext sslContext) {
        if (server != sslContext.isServer()) {
            throw new IllegalArgumentException("QuicSslContext.isServer() " + sslContext.isServer()
                    + " isn't supported by this builder");
        }
        return sslEngineProvider(q -> sslContext.newEngine(q.alloc()));
    }

    /**
     * 设置按 {@link QuicChannel} 动态提供 {@link QuicSslEngine} 的 {@link Function}。
     *
     * @param sslEngineProvider    the provider.
     * @return                      the instance itself.
     */
    public final B sslEngineProvider(Function<QuicChannel, ? extends QuicSslEngine> sslEngineProvider) {
        this.sslEngineProvider = sslEngineProvider;
        return self();
    }

    /**
     * 配置执行昂贵 SSL 操作的 {@link Executor}，避免阻塞 I/O 线程。
     *
     * @param sslTaskExecutor       the {@link Executor} that will be used to offload expensive SSL operations.
     * @return                      the instance itself.
     */
    public final B sslTaskExecutor(Executor sslTaskExecutor) {
        this.sslTaskExecutor = sslTaskExecutor;
        return self();
    }

    /**
     * 配置 active connection ID 数量上限。
     *
     * @param limit     the limit to use.
     * @return          the instance itself.
     */
    public final B activeConnectionIdLimit(long limit) {
        checkPositive(limit, "limit");
        activeConnectionIdLimit = limit;
        return self();
    }

    /**
     * 配置无状态重置令牌（stateless reset token，须为 16 字节）。
     *
     * @param token     the token to use.
     * @return          the instance itself.
     */
    public final B statelessResetToken(byte[] token) {
        if (token.length != 16) {
            throw new IllegalArgumentException("token must be 16 bytes but was " + token.length);
        }

        this.statelessResetToken = token.clone();
        return self();
    }

    private QuicheConfig createConfig() {
        return new QuicheConfig(version, grease,
                maxIdleTimeout, maxSendUdpPayloadSize, maxRecvUdpPayloadSize, initialMaxData,
                initialMaxStreamDataBidiLocal, initialMaxStreamDataBidiRemote,
                initialMaxStreamDataUni, initialMaxStreamsBidi, initialMaxStreamsUni,
                ackDelayExponent, maxAckDelay, disableActiveMigration, enableHystart, discoverPmtu,
                congestionControlAlgorithm, initialCongestionWindowPackets, recvQueueLen, sendQueueLen,
                activeConnectionIdLimit, statelessResetToken);
    }

    /** 构建编解码器前校验配置（如 SSL 提供者非空）。 */
    protected void validate() {
        if (sslEngineProvider == null) {
            throw new IllegalStateException("sslEngineProvider can't be null");
        }
    }

    /**
     * 构建应加入底层传输 {@link io.netty.channel.Channel} pipeline 的 QUIC 编解码 {@link ChannelHandler}。
     *
     * @return the {@link ChannelHandler} which acts as QUIC codec.
     */
    public final ChannelHandler build() {
        validate();
        QuicheConfig config = createConfig();
        try {
            return build(config, sslEngineProvider, sslTaskExecutor, localConnIdLength, flushStrategy);
        } catch (Throwable cause) {
            config.free();
            throw cause;
        }
    }

    /**
     * 克隆构建器，复制当前全部配置。
     *
     * @return the new instance that is a clone if this instance.
     */
    public abstract B clone();

    /**
     * 子类实现：根据 Quiche 配置与 SSL 上下文构建具体 QUIC 编解码器。
     *
     * @param config                the {@link QuicheConfig} that should be used.
     * @param sslContextProvider    the context provider
     * @param sslTaskExecutor       the {@link Executor} to use.
     * @param localConnIdLength     the local connection id length.
     * @param flushStrategy         the {@link FlushStrategy}  that should be used.
     * @return                      the {@link ChannelHandler} which acts as codec.
     */
    abstract ChannelHandler build(QuicheConfig config,
                                            Function<QuicChannel, ? extends QuicSslEngine> sslContextProvider,
                                            Executor sslTaskExecutor,
                                            int localConnIdLength, FlushStrategy flushStrategy);
}
