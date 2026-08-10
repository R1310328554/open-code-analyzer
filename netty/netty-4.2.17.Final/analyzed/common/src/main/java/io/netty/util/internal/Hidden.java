/*
 * Copyright 2019 The Netty Project
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

package io.netty.util.internal;

import io.netty.util.concurrent.FastThreadLocalThread;
import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Contains classes that must have public visibility but are not public API.
 *
 * <p>收纳必须对外可见、但不属于公开 API 的内部类（如 BlockHound 集成）。</p>
 */
class Hidden {

    /**
     * This class integrates Netty with BlockHound.
     * <p>
     * It is public but only because of the ServiceLoader's limitations
     * and SHOULD NOT be considered a public API.
     *
     * <p>通过 {@link BlockHoundIntegration} 向 BlockHound 注册 Netty 中已知、可接受的阻塞调用点，
     * 并扩展「非阻塞线程」判定以识别 {@link FastThreadLocalThread}。</p>
     */
    @UnstableApi
    public static final class NettyBlockHoundIntegration implements BlockHoundIntegration {

        @Override
        public void applyTo(BlockHound.Builder builder) {
            // EventLoop 异常处理中可能短暂阻塞，允许白名单
            builder.allowBlockingCallsInside(
                    "io.netty.channel.nio.NioEventLoop",
                    "handleLoopException"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.channel.kqueue.KQueueEventLoop",
                    "handleLoopException"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.channel.epoll.EpollEventLoop",
                    "handleLoopException"
            );

            // HashedWheelTimer 启动/停止与 tick 等待涉及同步
            builder.allowBlockingCallsInside(
                    "io.netty.util.HashedWheelTimer",
                    "start"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.util.HashedWheelTimer",
                    "stop"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.util.HashedWheelTimer$Worker",
                    "waitForNextTick"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.util.concurrent.SingleThreadEventExecutor",
                    "confirmShutdown"
            );

            // 内存池分配路径中的锁
            builder.allowBlockingCallsInside(
                    "io.netty.buffer.PoolArena",
                    "lock"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.buffer.PoolSubpage",
                    "lock"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.buffer.PoolChunk",
                    "allocateRun"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.buffer.PoolChunk",
                    "free"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.buffer.AdaptivePoolingAllocator$1",
                    "initialValue"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.buffer.AdaptivePoolingAllocator$1",
                    "onRemoval"
            );

            // SSL 握手与委托任务执行
            builder.allowBlockingCallsInside(
                    "io.netty.handler.ssl.SslHandler",
                    "handshake"
            );

            builder.allowBlockingCallsInside(
                    "io.netty.handler.ssl.SslHandler",
                    "runAllDelegatedTasks"
            );
            builder.allowBlockingCallsInside(
                    "io.netty.handler.ssl.SslHandler",
                    "runDelegatedTasks"
            );
            builder.allowBlockingCallsInside(
                    "io.netty.util.concurrent.GlobalEventExecutor",
                    "takeTask");

            builder.allowBlockingCallsInside(
                    "io.netty.util.concurrent.GlobalEventExecutor",
                    "addTask");

            builder.allowBlockingCallsInside(
                    "io.netty.util.concurrent.SingleThreadEventExecutor",
                    "pollTaskFrom");

            builder.allowBlockingCallsInside(
                    "io.netty.util.concurrent.SingleThreadEventExecutor",
                    "takeTask");

            builder.allowBlockingCallsInside(
                    "io.netty.util.concurrent.SingleThreadEventExecutor",
                    "addTask");

            builder.allowBlockingCallsInside(
                    "io.netty.handler.ssl.ReferenceCountedOpenSslClientContext$ExtendedTrustManagerVerifyCallback",
                    "verify");

            builder.allowBlockingCallsInside(
                    "io.netty.handler.ssl.JdkSslContext$Defaults",
                    "init");

            // Let's whitelist SSLEngineImpl.unwrap(...) for now as it may fail otherwise for TLS 1.3.
            // See https://mail.openjdk.java.net/pipermail/security-dev/2020-August/022271.html
            // JDK SSLEngine 在 TLS 1.3 下 unwrap/wrap 可能触发阻塞，需白名单
            builder.allowBlockingCallsInside(
                    "sun.security.ssl.SSLEngineImpl",
                    "unwrap");

            builder.allowBlockingCallsInside(
                    "sun.security.ssl.SSLEngineImpl",
                    "wrap");

            // DNS 解析与 hosts 文件解析
            builder.allowBlockingCallsInside(
                    "io.netty.resolver.dns.UnixResolverDnsServerAddressStreamProvider",
                    "parse");

            builder.allowBlockingCallsInside(
                    "io.netty.resolver.dns.UnixResolverDnsServerAddressStreamProvider",
                    "parseEtcResolverSearchDomains");

            builder.allowBlockingCallsInside(
                    "io.netty.resolver.dns.UnixResolverDnsServerAddressStreamProvider",
                    "parseEtcResolverOptions");

            builder.allowBlockingCallsInside(
                    "io.netty.resolver.dns.DnsQueryIdSpace",
                    "nextId");

            builder.allowBlockingCallsInside(
                    "io.netty.resolver.dns.DnsQueryIdSpace$DnsQueryIdRange",
                    "pushId");

            builder.allowBlockingCallsInside(
                    "io.netty.resolver.HostsFileEntriesProvider$ParserImpl",
                    "parse");

            builder.allowBlockingCallsInside(
                    "io.netty.util.NetUtil$SoMaxConnAction",
                    "run");

            builder.allowBlockingCallsInside("io.netty.util.internal.ReferenceCountUpdater",
                    "retryRelease0");

            builder.allowBlockingCallsInside("io.netty.util.internal.PlatformDependent", "createTempFile");
            // 扩展非阻塞线程谓词：FastThreadLocalThread 且未显式 permit 阻塞时视为非阻塞
            builder.nonBlockingThreadPredicate(new Function<Predicate<Thread>, Predicate<Thread>>() {
                @Override
                public Predicate<Thread> apply(final Predicate<Thread> p) {
                    return new Predicate<Thread>() {
                        @Override
                        public boolean test(Thread thread) {
                            return p.test(thread) ||
                                    thread instanceof FastThreadLocalThread &&
                                            !((FastThreadLocalThread) thread).permitBlockingCalls();
                        }
                    };
                }
            });
        }

        @Override
        public int compareTo(BlockHoundIntegration o) {
            return 0;
        }
    }
}
