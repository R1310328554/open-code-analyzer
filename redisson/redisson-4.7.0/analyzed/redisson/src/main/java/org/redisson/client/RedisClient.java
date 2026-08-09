/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.channel.uring.IoUringChannelOption;
import io.netty.channel.uring.IoUringSocketChannel;
import io.netty.resolver.AddressResolver;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.util.HashedWheelTimer;
import io.netty.util.NetUtil;
import io.netty.util.Timer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.redisson.api.RFuture;
import org.redisson.client.handler.RedisChannelInitializer;
import org.redisson.client.handler.RedisChannelInitializer.Type;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.misc.RedisURI;

import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 底层 Redis 客户端，基于 Netty 管理连接生命周期。
 * <p>
 * 负责 DNS 解析、TCP/UDS 连接、普通命令与 Pub/Sub 双 Bootstrap，以及优雅关闭。
 *
 * @author Nikita Koksharov
 *
 */
public final class RedisClient {

    /** 异步 DNS 解析结果的 Future 引用。 */
    private final AtomicReference<CompletableFuture<InetSocketAddress>> resolvedAddrFuture = new AtomicReference<>();
    /** 普通命令连接的 Netty Bootstrap。 */
    private final Bootstrap bootstrap;
    /** 发布/订阅连接的 Netty Bootstrap。 */
    private final Bootstrap pubSubBootstrap;
    /** Redis 连接 URI。 */
    private final RedisURI uri;
    /** 已解析的套接字地址（IP 或 UDS）。 */
    private SocketAddress resolvedAddr;
    /** 本客户端打开的全部 Channel 组，用于批量关闭。 */
    private final ChannelGroup channels;

    private final ExecutorService executor;
    private final long commandTimeout;
    private final Timer timer;
    private final RedisClientConfig config;

    /** 是否由本客户端创建并负责关闭 Timer。 */
    private boolean hasOwnTimer;
    /** 是否由本客户端创建并负责关闭 Executor。 */
    private boolean hasOwnExecutor;
    /** 是否由本客户端创建并负责关闭 EventLoopGroup。 */
    private boolean hasOwnGroup;
    /** 是否由本客户端创建并负责关闭 AddressResolverGroup。 */
    private boolean hasOwnResolver;
    /** 是否已进入关闭流程。 */
    private volatile boolean shutdown;

    /**
     * 根据配置创建 Redis 客户端实例。
     *
     * @param config 客户端配置
     * @return 新的 {@link RedisClient}
     */
    public static RedisClient create(RedisClientConfig config) {
        return new RedisClient(config);
    }
    
    private RedisClient(RedisClientConfig config) {
        RedisClientConfig copy = new RedisClientConfig(config);
        if (copy.getTimer() == null) {
            copy.setTimer(new HashedWheelTimer());
            hasOwnTimer = true;
        }
        if (copy.getGroup() == null) {
            copy.setGroup(new NioEventLoopGroup());
            hasOwnGroup = true;
        }
        if (copy.getExecutor() == null) {
            copy.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
            hasOwnExecutor = true;
        }
        if (copy.getResolverGroup() == null) {
            if (config.getSocketChannelClass() == EpollSocketChannel.class) {
                copy.setResolverGroup(new DnsAddressResolverGroup(EpollDatagramChannel.class, DnsServerAddressStreamProviders.platformDefault()));
            } else if (config.getSocketChannelClass() == KQueueSocketChannel.class) {
                copy.setResolverGroup(new DnsAddressResolverGroup(KQueueDatagramChannel.class, DnsServerAddressStreamProviders.platformDefault()));
            } else {
                copy.setResolverGroup(new DnsAddressResolverGroup(NioDatagramChannel.class, DnsServerAddressStreamProviders.platformDefault()));
            }
            hasOwnResolver = true;
        }

        this.config = copy;
        this.executor = copy.getExecutor();
        this.timer = copy.getTimer();
        
        uri = copy.getAddress();
        resolvedAddr = copy.getAddr();

        if (uri.isUDS()) {
            resolvedAddr = new DomainSocketAddress(uri.getHost());
        }
        if (resolvedAddr != null) {
            resolvedAddrFuture.set(CompletableFuture.completedFuture(getAddr()));
        }

        channels = new DefaultChannelGroup(copy.getGroup().next());
        bootstrap = createBootstrap(copy, Type.PLAIN);
        pubSubBootstrap = createBootstrap(copy, Type.PUBSUB);
        
        this.commandTimeout = copy.getCommandTimeout();
    }

    /** 创建并配置指定类型（PLAIN 或 PUBSUB）的 Netty Bootstrap。 */
    private Bootstrap createBootstrap(RedisClientConfig config, Type type) {
        Bootstrap bootstrap = new Bootstrap()
                        .resolver(config.getResolverGroup())
                        .channel(config.getSocketChannelClass())
                        .group(config.getGroup());

        bootstrap.handler(new RedisChannelInitializer(bootstrap, config, this, channels, type));
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout());

        if (!DomainSocketChannel.class.isAssignableFrom(config.getSocketChannelClass())) {
            applyTCPOptions(config, bootstrap);
        }

        config.getNettyHook().afterBoostrapInitialization(bootstrap);
        return bootstrap;
    }

    /** 根据配置为 Bootstrap 设置 TCP KeepAlive、NODELAY 等平台相关选项。 */
    private void applyTCPOptions(RedisClientConfig config, Bootstrap bootstrap) {
        bootstrap.option(ChannelOption.SO_KEEPALIVE, config.isKeepAlive());
        bootstrap.option(ChannelOption.TCP_NODELAY, config.isTcpNoDelay());

        if (config.getSocketChannelClass() == NioSocketChannel.class) {
            SocketOption<Integer> countOption = null;
            SocketOption<Integer> idleOption = null;
            SocketOption<Integer> intervalOption = null;
            try {
                // 兼容 JDK 1.8 下 IntelliJ 编译 ExtendedSocketOptions
                Class<?> options = Class.forName("jdk.net.ExtendedSocketOptions");

                countOption = (SocketOption<Integer>) options.getDeclaredField("TCP_KEEPCOUNT").get(null);
                idleOption = (SocketOption<Integer>) options.getDeclaredField("TCP_KEEPIDLE").get(null);
                intervalOption = (SocketOption<Integer>) options.getDeclaredField("TCP_KEEPINTERVAL").get(null);
            } catch (ReflectiveOperationException e) {
                // 平台不支持时跳过
            }

            if (config.getTcpKeepAliveCount() > 0 && countOption != null) {
                bootstrap.option(NioChannelOption.of(countOption), config.getTcpKeepAliveCount());
            }
            if (config.getTcpKeepAliveIdle() > 0 && idleOption != null) {
                bootstrap.option(NioChannelOption.of(idleOption), config.getTcpKeepAliveIdle());
            }
            if (config.getTcpKeepAliveInterval() > 0 && intervalOption != null) {
                bootstrap.option(NioChannelOption.of(intervalOption), config.getTcpKeepAliveInterval());
            }
        } else if (config.getSocketChannelClass() == EpollSocketChannel.class) {
            if (config.getTcpKeepAliveCount() > 0) {
                bootstrap.option(EpollChannelOption.TCP_KEEPCNT, config.getTcpKeepAliveCount());
            }
            if (config.getTcpKeepAliveIdle() > 0) {
                bootstrap.option(EpollChannelOption.TCP_KEEPIDLE, config.getTcpKeepAliveIdle());
            }
            if (config.getTcpKeepAliveInterval() > 0) {
                bootstrap.option(EpollChannelOption.TCP_KEEPINTVL, config.getTcpKeepAliveInterval());
            }
            if (config.getTcpUserTimeout() > 0) {
                bootstrap.option(EpollChannelOption.TCP_USER_TIMEOUT, config.getTcpUserTimeout());
            }
        } else if (config.getSocketChannelClass() == IoUringSocketChannel.class) {
            if (config.getTcpKeepAliveCount() > 0) {
                bootstrap.option(IoUringChannelOption.TCP_KEEPCNT, config.getTcpKeepAliveCount());
            }
            if (config.getTcpKeepAliveIdle() > 0) {
                bootstrap.option(IoUringChannelOption.TCP_KEEPIDLE, config.getTcpKeepAliveIdle());
            }
            if (config.getTcpKeepAliveInterval() > 0) {
                bootstrap.option(IoUringChannelOption.TCP_KEEPINTVL, config.getTcpKeepAliveInterval());
            }
            if (config.getTcpUserTimeout() > 0) {
                bootstrap.option(IoUringChannelOption.TCP_USER_TIMEOUT, config.getTcpUserTimeout());
            }
        }
    }

    /**
     * 返回已解析的连接地址。
     * <p>
     * UDS 连接时包装为带路径字符串的 {@link InetSocketAddress}。
     *
     * @return 套接字地址
     */
    public InetSocketAddress getAddr() {
        if (resolvedAddr instanceof DomainSocketAddress) {
            try {
                return new InetSocketAddress(InetAddress.getByAddress(((DomainSocketAddress) resolvedAddr).path(), new byte[]{127, 0, 0, 1}), uri.getPort()) {
                    @Override
                    public String toString() {
                        return ((DomainSocketAddress) resolvedAddr).path();
                    }
                };
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
        }

        return (InetSocketAddress) resolvedAddr;
    }

    /** 返回命令执行超时时间（毫秒）。 */
    public long getCommandTimeout() {
        return commandTimeout;
    }

    /** 返回客户端配置的不可变副本引用。 */
    public RedisClientConfig getConfig() {
        return config;
    }

    /** 返回 Netty 定时器，用于超时调度。 */
    public Timer getTimer() {
        return timer;
    }
    
    /**
     * 同步建立普通 Redis 连接。
     *
     * @return 已就绪的 {@link RedisConnection}
     * @throws RedisException 连接或握手失败
     */
    public RedisConnection connect() {
        try {
            return connectAsync().toCompletableFuture().join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RedisException) {
                throw (RedisException) e.getCause();
            } else {
                throw new RedisConnectionException("Unable to connect to: " + uri, e);
            }
        }
    }
    
    /**
     * 异步解析主机名为 {@link InetSocketAddress}。
     * <p>
     * 字面量 IP 或已预设地址时立即完成；否则通过 Netty DNS 解析器异步解析。
     *
     * @return 解析结果的 Future
     */
    public CompletableFuture<InetSocketAddress> resolveAddr() {
        if (resolvedAddrFuture.get() != null) {
            return resolvedAddrFuture.get();
        }
        
        CompletableFuture<InetSocketAddress> promise = new CompletableFuture<>();
        if (!resolvedAddrFuture.compareAndSet(null, promise)) {
            return resolvedAddrFuture.get();
        }
        
        byte[] addr = NetUtil.createByteArrayFromIpAddressString(uri.getHost());
        if (addr != null) {
            try {
                resolvedAddr = new InetSocketAddress(InetAddress.getByAddress(uri.getHost(), addr), uri.getPort());
            } catch (UnknownHostException e) {
                // 解析失败时跳过
            }
            promise.complete((InetSocketAddress) resolvedAddr);
            return promise;
        }
        
        AddressResolver<InetSocketAddress> resolver = (AddressResolver<InetSocketAddress>) bootstrap.config().resolver().getResolver(bootstrap.config().group().next());
        Future<InetSocketAddress> resolveFuture = resolver.resolve(InetSocketAddress.createUnresolved(uri.getHost(), uri.getPort()));
        resolveFuture.addListener((FutureListener<InetSocketAddress>) future -> {
            if (!future.isSuccess()) {
                promise.completeExceptionally(new RedisConnectionException(future.cause()));
                return;
            }

            InetSocketAddress resolved = future.getNow();
            byte[] addr1 = resolved.getAddress().getAddress();
            resolvedAddr = new InetSocketAddress(InetAddress.getByAddress(uri.getHost(), addr1), resolved.getPort());
            promise.complete((InetSocketAddress) resolvedAddr);
        });
        return promise;
    }

    /**
     * 异步建立普通 Redis 连接。
     *
     * @return 连接就绪后的 {@link RFuture}
     */
    public RFuture<RedisConnection> connectAsync() {
        CompletionStage<SocketAddress> addrFuture = resolveSocket();
        CompletionStage<RedisConnection> f = addrFuture.thenCompose(res -> {
            CompletableFuture<RedisConnection> r = new CompletableFuture<>();
            ChannelFuture channelFuture = bootstrap.connect(res);
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (isShutdown()) {
                    RedisConnectionException cause = new RedisConnectionException("RedisClient is shutdown");
                    r.completeExceptionally(cause);
                    return;
                }

                if (future.isSuccess()) {
                    RedisConnection c = RedisConnection.getFrom(future.channel());
                    c.getConnectionPromise().whenComplete((res1, e) -> {
                        bootstrap.config().group().execute(() -> {
                            if (e == null) {
                                if (!r.complete(c)) {
                                    c.closeAsync();
                                } else {
                                    executor.execute(() -> {
                                        if (config.getConnectedListener() != null) {
                                            config.getConnectedListener().accept((InetSocketAddress) getAddr());
                                        }
                                    });
                                }
                            } else {
                                r.completeExceptionally(e);
                                c.closeAsync();
                            }
                        });
                    });
                } else {
                    bootstrap.config().group().execute(() -> r.completeExceptionally(future.cause()));
                }
            });
            return r;
        });
        return new CompletableFutureWrapper<>(f);
    }

    /** UDS 直接返回已解析地址，否则异步 DNS 解析。 */
    private CompletionStage<SocketAddress> resolveSocket() {
        if (uri.isUDS()) {
            return CompletableFuture.completedFuture(resolvedAddr);
        }
        return resolveAddr().thenApply(s -> s);
    }

    /**
     * 同步建立 Pub/Sub 专用连接。
     *
     * @return 已就绪的 {@link RedisPubSubConnection}
     * @throws RedisException 连接或握手失败
     */
    public RedisPubSubConnection connectPubSub() {
        try {
            return connectPubSubAsync().toCompletableFuture().join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RedisException) {
                throw (RedisException) e.getCause();
            } else {
                throw new RedisConnectionException("Unable to connect to: " + uri, e);
            }
        }
    }

    /**
     * 异步建立 Pub/Sub 专用连接。
     *
     * @return 连接就绪后的 {@link RFuture}
     */
    public RFuture<RedisPubSubConnection> connectPubSubAsync() {
        CompletionStage<SocketAddress> nameFuture = resolveSocket();
        CompletionStage<RedisPubSubConnection> f = nameFuture.thenCompose(res -> {
            CompletableFuture<RedisPubSubConnection> r = new CompletableFuture<>();
            ChannelFuture channelFuture = pubSubBootstrap.connect(res);
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (isShutdown()) {
                    RedisConnectionException cause = new RedisConnectionException("RedisClient is shutdown");
                    r.completeExceptionally(cause);
                    return;
                }

                if (future.isSuccess()) {
                    RedisPubSubConnection c = RedisPubSubConnection.getFrom(future.channel());
                    c.getConnectionPromise().whenComplete((res1, e) -> {
                        pubSubBootstrap.config().group().execute(() -> {
                            if (e == null) {
                                if (!r.complete(c)) {
                                    c.closeAsync();
                                }
                            } else {
                                r.completeExceptionally(e);
                                c.closeAsync();
                            }
                        });
                    });
                } else {
                    pubSubBootstrap.config().group().execute(() -> r.completeExceptionally(future.cause()));
                }
            });
            return r;
        });
        return new CompletableFutureWrapper<>(f);
    }

    /** 同步关闭客户端及自有 Timer、Executor、EventLoopGroup 等资源。 */
    public void shutdown() {
        shutdownAsync().toCompletableFuture().join();
    }

    /**
     * 异步关闭所有 Channel 并释放自有资源。
     *
     * @return 关闭完成后的 Future
     */
    public RFuture<Void> shutdownAsync() {
        shutdown = true;
        CompletableFuture<Void> result = new CompletableFuture<>();
        if (channels.isEmpty() || config.getGroup().isShuttingDown()) {
            shutdown(result);
            return new CompletableFutureWrapper<>(result);
        }

        for (Channel channel : channels) {
            RedisConnection connection = RedisConnection.getFrom(channel);
            if (connection != null) {
                connection.closeAsync();
            }
        }

        ChannelGroupFuture channelsFuture = channels.close();
        channelsFuture.addListener((FutureListener<Void>) future -> {
            if (!future.isSuccess()) {
                result.completeExceptionally(future.cause());
                return;
            }

            shutdown(result);
        });

        return new CompletableFutureWrapper<>(result);
    }

    /** 判断客户端或 EventLoopGroup 是否已关闭/正在关闭。 */
    public boolean isShutdown() {
        return shutdown || bootstrap.config().group().isShuttingDown();
    }

    /** 在独立线程中停止 Timer、Executor、Resolver 与 EventLoopGroup。 */
    private void shutdown(CompletableFuture<Void> result) {
        if (!hasOwnTimer && !hasOwnExecutor && !hasOwnResolver && !hasOwnGroup) {
            result.complete(null);
        } else {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        if (hasOwnTimer) {
                            timer.stop();
                        }
                        
                        if (hasOwnExecutor) {
                            executor.shutdown();
                            executor.awaitTermination(15, TimeUnit.SECONDS);
                        }
                        
                        if (hasOwnResolver) {
                            bootstrap.config().resolver().close();
                        }
                        if (hasOwnGroup) {
                            bootstrap.config().group().shutdownGracefully();
                        }
                    } catch (Exception e) {
                        result.completeExceptionally(e);
                        return;
                    }

                    result.complete(null);
                }
            };
            t.start();
        }
    }

    @Override
    public String toString() {
        return "[addr=" + uri + "," + resolvedAddr + "]";
    }

}
