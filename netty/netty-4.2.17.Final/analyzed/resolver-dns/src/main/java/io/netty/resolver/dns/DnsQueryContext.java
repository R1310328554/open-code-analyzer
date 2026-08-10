/*
 * Copyright 2014 The Netty Project
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
package io.netty.resolver.dns;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.dns.AbstractDnsOptPseudoRrRecord;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.handler.codec.dns.TcpDnsQueryEncoder;
import io.netty.handler.codec.dns.TcpDnsResponseDecoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * DNS 单次查询的抽象上下文：分配 ID、构造 {@link DnsQuery}、写通道、超时与 TCP 回退。
 * <p>子类实现 {@link #newQuery} 与 {@link #protocol()} 以区分 UDP/TCP 传输。</p>
 */
abstract class DnsQueryContext {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DnsQueryContext.class);
    /** 超时或取消后延迟回收 query ID 的毫秒数，降低与迟到的响应 ID 冲突风险。 */
    private static final long ID_REUSE_ON_TIMEOUT_DELAY_MILLIS;

    static {
        ID_REUSE_ON_TIMEOUT_DELAY_MILLIS =
                SystemPropertyUtil.getLong("io.netty.resolver.dns.idReuseOnTimeoutDelayMillis", 10000);
        logger.debug("-Dio.netty.resolver.dns.idReuseOnTimeoutDelayMillis: {}", ID_REUSE_ON_TIMEOUT_DELAY_MILLIS);
    }

    private static final TcpDnsQueryEncoder TCP_ENCODER = new TcpDnsQueryEncoder();

    /** 发送查询的数据报或 TCP 通道。 */
    private final Channel channel;
    /** 目标权威/递归 DNS 服务器地址。 */
    private final InetSocketAddress nameServerAddr;
    private final DnsQueryContextManager queryContextManager;
    private final DnsQueryLifecycleObserver queryLifecycleObserver;
    private final Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise;

    private final DnsQuestion question;
    private final DnsRecord[] additionals;
    private final DnsRecord optResource;

    private final boolean recursionDesired;

    private final Bootstrap socketBootstrap;

    private final boolean retryWithTcpOnTimeout;
    private final long queryTimeoutMillis;

    /** 查询超时定时任务；完成时取消。 */
    private volatile Future<?> timeoutFuture;

    /** 已分配的 DNS 事务 ID；{@code Integer.MIN_VALUE} 表示尚未写入。 */
    private int id = Integer.MIN_VALUE;

    DnsQueryContext(Channel channel,
                    InetSocketAddress nameServerAddr,
                    DnsQueryContextManager queryContextManager,
                    DnsQueryLifecycleObserver queryLifecycleObserver,
                    int maxPayLoadSize,
                    boolean recursionDesired,
                    long queryTimeoutMillis,
                    DnsQuestion question,
                    DnsRecord[] additionals,
                    Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise,
                    Bootstrap socketBootstrap,
                    boolean retryWithTcpOnTimeout) {
        this.channel = checkNotNull(channel, "channel");
        this.queryContextManager = checkNotNull(queryContextManager, "queryContextManager");
        this.queryLifecycleObserver = checkNotNull(queryLifecycleObserver, "queryLifecycleObserver");
        this.nameServerAddr = checkNotNull(nameServerAddr, "nameServerAddr");
        this.question = checkNotNull(question, "question");
        this.additionals = checkNotNull(additionals, "additionals");
        this.promise = checkNotNull(promise, "promise");
        this.recursionDesired = recursionDesired;
        this.queryTimeoutMillis = queryTimeoutMillis;
        this.socketBootstrap = socketBootstrap;
        this.retryWithTcpOnTimeout = retryWithTcpOnTimeout;

        if (maxPayLoadSize > 0 &&
                // RFC6891：附加区最多一条 OPT；已有则不再注入
                // Only add the extra OPT record if there is not already one. This is required as only one is allowed
                // as per RFC:
                //  - https://datatracker.ietf.org/doc/html/rfc6891#section-6.1.1
                !hasOptRecord(additionals)) {
            optResource = new AbstractDnsOptPseudoRrRecord(maxPayLoadSize, 0, 0) {
                // We may want to remove this in the future and let the user just specify the opt record in the query.
            };
        } else {
            optResource = null;
        }
    }

    private static boolean hasOptRecord(DnsRecord[] additionals) {
        if (additionals != null && additionals.length > 0) {
            for (DnsRecord additional: additionals) {
                if (additional.type() == DnsRecordType.OPT) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 查询是否已完成（成功、失败或取消）。
     *
     * @return {@code true} if done.
     */
    final boolean isDone() {
        return promise.isDone();
    }

    /**
     * 返回将写入 {@link DnsQuery} 的 {@link DnsQuestion}。
     *
     * @return the question.
     */
    final DnsQuestion question() {
        return question;
    }

    /**
     * 创建新的 {@link DnsQuery}（由 UDP/TCP 子类实现）。
     *
     * @param id                the transaction id to use.
     * @param nameServerAddr    the nameserver to which the query will be send.
     * @return                  the new query.
     */
    protected abstract DnsQuery newQuery(int id, InetSocketAddress nameServerAddr);

    /**
     * 返回传输协议标识（如 {@code UDP} / {@code TCP}）。
     *
     * @return  the protocol.
     */
    protected abstract String protocol();

    /**
     * 写入 DNS 查询：向 {@link DnsQueryContextManager} 申请 ID、组装报文并发送。
     *
     * @param flush                 {@code true} if {@link Channel#flush()} should be called as well.
     */
    final void writeQuery(boolean flush) {
        assert id == Integer.MIN_VALUE : this.getClass().getSimpleName() +
                ".writeQuery(...) can only be executed once.";

        if ((id = queryContextManager.add(nameServerAddr, this)) == -1) {
            // 16 位 ID 空间耗尽，直接失败
            // We did exhaust the id space, fail the query
            IllegalStateException e = new IllegalStateException("query ID space exhausted: " + question());
            finishFailure("failed to send a query via " + protocol(), e, false);
            queryLifecycleObserver.queryWritten(nameServerAddr, channel.newFailedFuture(e));
            return;
        }

        // 查询结束时从管理器移除 ID；超时/取消则延迟移除
        // Ensure we remove the id from the QueryContextManager once the query completes.
        promise.addListener((FutureListener<AddressedEnvelope<DnsResponse, InetSocketAddress>>) future -> {
            // Cancel the timeout task.
            Future<?> timeoutFuture = DnsQueryContext.this.timeoutFuture;
            if (timeoutFuture != null) {
                DnsQueryContext.this.timeoutFuture = null;
                timeoutFuture.cancel(false);
            }

            Throwable cause = future.cause();
            if (cause instanceof DnsNameResolverTimeoutException || cause instanceof CancellationException) {
                // 超时/取消：延迟回收 ID，避免远端迟到响应与重用 ID 冲突
                // This query was failed due a timeout or cancellation. Let's delay the removal of the id to reduce
                // the risk of reusing the same id again while the remote nameserver might send the response after
                // the timeout.
                channel.eventLoop().schedule(new Runnable() {
                    @Override
                    public void run() {
                        removeFromContextManager(nameServerAddr);
                    }
                }, ID_REUSE_ON_TIMEOUT_DELAY_MILLIS, TimeUnit.MILLISECONDS);
            } else {
                // Remove the id from the manager as soon as the query completes. This may be because of success,
                // failure or cancellation
                removeFromContextManager(nameServerAddr);
            }
        });
        final DnsQuestion question = question();
        final DnsQuery query = newQuery(id, nameServerAddr);

        query.setRecursionDesired(recursionDesired);

        query.addRecord(DnsSection.QUESTION, question);

        for (DnsRecord record: additionals) {
            query.addRecord(DnsSection.ADDITIONAL, record);
        }

        if (optResource != null) {
            query.addRecord(DnsSection.ADDITIONAL, optResource);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} WRITE: {}, [{}: {}], {}",
                    channel, protocol(), id, nameServerAddr, question);
        }

        ChannelFuture f = sendQuery(query, flush);
        queryLifecycleObserver.queryWritten(nameServerAddr, f);
    }

    private void removeFromContextManager(InetSocketAddress nameServerAddr) {
        DnsQueryContext self = queryContextManager.remove(nameServerAddr, id);

        assert self == this : "Removed DnsQueryContext is not the correct instance";
    }

    private ChannelFuture sendQuery(final DnsQuery query, final boolean flush) {
        final ChannelPromise writePromise = channel.newPromise();
        writeQuery(query, flush, writePromise);
        return writePromise;
    }

    private void writeQuery(final DnsQuery query,
                            final boolean flush, ChannelPromise promise) {
        final ChannelFuture writeFuture = flush ? channel.writeAndFlush(query, promise) :
                channel.write(query, promise);
        if (writeFuture.isDone()) {
            onQueryWriteCompletion(queryTimeoutMillis, writeFuture);
        } else {
            writeFuture.addListener((ChannelFutureListener) future ->
                    onQueryWriteCompletion(queryTimeoutMillis, future));
        }
    }

    private void onQueryWriteCompletion(final long queryTimeoutMillis,
                                        ChannelFuture writeFuture) {
        if (!writeFuture.isSuccess()) {
            finishFailure("failed to send a query '" + id + "' via " + protocol(), writeFuture.cause(), false);
            return;
        }

        // 按需注册查询超时任务
        // Schedule a query timeout task if necessary.
        if (queryTimeoutMillis > 0) {
            timeoutFuture = channel.eventLoop().schedule(new Runnable() {
                @Override
                public void run() {
                    if (promise.isDone()) {
                        // 超时前已收到响应
                        // Received a response before the query times out.
                        return;
                    }

                    finishFailure("query '" + id + "' via " + protocol() + " timed out after " +
                            queryTimeoutMillis + " milliseconds", null, true);
                }
            }, queryTimeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 收到响应并完成 {@link Promise}；本方法接管 {@link AddressedEnvelope} 引用。
     * This method takes ownership of passed {@link AddressedEnvelope}.
     */
    void finishSuccess(AddressedEnvelope<? extends DnsResponse, InetSocketAddress> envelope, boolean truncated) {
        // 未截断或可走 TCP 回退时尝试成功完成
        // Check if the response was not truncated or if a fallback to TCP is possible.
        if (!truncated || !retryWithTcp(envelope)) {
            final DnsResponse res = envelope.content();
            if (res.count(DnsSection.QUESTION) != 1) {
                logger.warn("{} Received a DNS response with invalid number of questions. Expected: 1, found: {}",
                        channel, envelope);
            } else if (!question().equals(res.recordAt(DnsSection.QUESTION))) {
                logger.warn("{} Received a mismatching DNS response. Expected: [{}], found: {}",
                        channel, question(), envelope);
            } else if (trySuccess(envelope)) {
                return; // Ownership transferred, don't release
            }
            envelope.release();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean trySuccess(AddressedEnvelope<? extends DnsResponse, InetSocketAddress> envelope) {
        return promise.trySuccess((AddressedEnvelope<DnsResponse, InetSocketAddress>) envelope);
    }

    /**
     * 因失败结束查询并 fail {@link Promise}；超时可能触发 TCP 重试。
     */
    final boolean finishFailure(String message, Throwable cause, boolean timeout) {
        if (promise.isDone()) {
            return false;
        }
        final DnsQuestion question = question();

        final StringBuilder buf = new StringBuilder(message.length() + 128);
        buf.append('[')
           .append(id)
           .append(": ")
           .append(nameServerAddr)
           .append("] ")
           .append(question)
           .append(' ')
           .append(message)
           .append(" (no stack trace available)");

        final DnsNameResolverException e;
        if (timeout) {
            // 超时使用 DnsNameResolverTimeoutException，便于调用方重试
            // This was caused by a timeout so use DnsNameResolverTimeoutException to allow the user to
            // handle it special (like retry the query).
            e = new DnsNameResolverTimeoutException(nameServerAddr, question, buf.toString());
            if (retryWithTcpOnTimeout && retryWithTcp(e)) {
                // We did successfully retry with TCP.
                return false;
            }
        } else {
            e = new DnsNameResolverException(nameServerAddr, question, buf.toString(), cause);
        }
        return promise.tryFailure(e);
    }

    /**
     * 在响应被截断或配置允许时，通过 TCP 重发同一查询。
     *
     * @param originalResult    the result of the original {@link DnsQueryContext}.
     * @return                  {@code true} if retry via TCP is supported and so the ownership of
     *                          {@code originalResult} was transferred, {@code false} otherwise.
     */
    private boolean retryWithTcp(final Object originalResult) {
        if (socketBootstrap == null) {
            return false;
        }

        socketBootstrap.connect(nameServerAddr).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                logger.debug("{} Unable to fallback to TCP [{}: {}]",
                        future.channel(), id, nameServerAddr, future.cause());
                // TCP 回退失败，回退到截断 UDP 响应或原始错误
                // TCP fallback failed, just use the truncated response or error.
                finishOriginal(originalResult, future);
                return;
            }
            final Channel tcpCh = future.channel();
            Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise =
                    tcpCh.eventLoop().newPromise();
            final TcpDnsQueryContext tcpCtx = new TcpDnsQueryContext(tcpCh,
                    (InetSocketAddress) tcpCh.remoteAddress(), queryContextManager, queryLifecycleObserver, 0,
                    recursionDesired, queryTimeoutMillis, question(), additionals, promise);
            tcpCh.pipeline().addLast(TCP_ENCODER);
            tcpCh.pipeline().addLast(new TcpDnsResponseDecoder());
            tcpCh.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    Channel tcpCh = ctx.channel();
                    DnsResponse response = (DnsResponse) msg;
                    int queryId = response.id();

                    if (logger.isDebugEnabled()) {
                        logger.debug("{} RECEIVED: TCP [{}: {}], {}", tcpCh, queryId,
                                tcpCh.remoteAddress(), response);
                    }

                    DnsQueryContext foundCtx = queryContextManager.get(nameServerAddr, queryId);
                    if (foundCtx != null && foundCtx.isDone()) {
                        logger.debug("{} Received a DNS response for a query that was timed out or cancelled " +
                                ": TCP [{}: {}]", tcpCh, queryId, nameServerAddr);
                        response.release();
                    } else if (foundCtx == tcpCtx) {
                        tcpCtx.finishSuccess(new AddressedEnvelopeAdapter(
                                (InetSocketAddress) ctx.channel().remoteAddress(),
                                (InetSocketAddress) ctx.channel().localAddress(),
                                response), false);
                    } else {
                        response.release();
                        tcpCtx.finishFailure("Received TCP DNS response with unexpected ID", null, false);
                        if (logger.isDebugEnabled()) {
                            logger.debug("{} Received a DNS response with an unexpected ID: TCP [{}: {}]",
                                    tcpCh, queryId, tcpCh.remoteAddress());
                        }
                    }
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    if (tcpCtx.finishFailure(
                            "TCP fallback error", cause, false) && logger.isDebugEnabled()) {
                        logger.debug("{} Error during processing response: TCP [{}: {}]",
                                ctx.channel(), id,
                                ctx.channel().remoteAddress(), cause);
                    }
                }
            });

            promise.addListener(
                    (FutureListener<AddressedEnvelope<DnsResponse, InetSocketAddress>>) future1 -> {
                        if (future1.isSuccess()) {
                            finishSuccess(future1.getNow(), false);
                            // Release the original result.
                            ReferenceCountUtil.release(originalResult);
                        } else {
                            // TCP fallback failed, just use the truncated response or error.
                            finishOriginal(originalResult, future1);
                        }
                        tcpCh.close();
                    });
            tcpCtx.writeQuery(true);
        });
        return true;
    }

    @SuppressWarnings("unchecked")
    private void finishOriginal(Object originalResult, Future<?> future) {
        if (originalResult instanceof Throwable) {
            Throwable error = (Throwable) originalResult;
            ThrowableUtil.addSuppressed(error, future.cause());
            promise.tryFailure(error);
        } else {
            finishSuccess((AddressedEnvelope<? extends DnsResponse, InetSocketAddress>) originalResult, false);
        }
    }

    /** TCP 回退路径上包装 {@link DnsResponse} 的轻量 {@link AddressedEnvelope} 适配器。 */
    private static final class AddressedEnvelopeAdapter implements AddressedEnvelope<DnsResponse, InetSocketAddress> {
        private final InetSocketAddress sender;
        private final InetSocketAddress recipient;
        private final DnsResponse response;

        AddressedEnvelopeAdapter(InetSocketAddress sender, InetSocketAddress recipient, DnsResponse response) {
            this.sender = sender;
            this.recipient = recipient;
            this.response = response;
        }

        @Override
        public DnsResponse content() {
            return response;
        }

        @Override
        public InetSocketAddress sender() {
            return sender;
        }

        @Override
        public InetSocketAddress recipient() {
            return recipient;
        }

        @Override
        public AddressedEnvelope<DnsResponse, InetSocketAddress> retain() {
            response.retain();
            return this;
        }

        @Override
        public AddressedEnvelope<DnsResponse, InetSocketAddress> retain(int increment) {
            response.retain(increment);
            return this;
        }

        @Override
        public AddressedEnvelope<DnsResponse, InetSocketAddress> touch() {
            response.touch();
            return this;
        }

        @Override
        public AddressedEnvelope<DnsResponse, InetSocketAddress> touch(Object hint) {
            response.touch(hint);
            return this;
        }

        @Override
        public int refCnt() {
            return response.refCnt();
        }

        @Override
        public boolean release() {
            return response.release();
        }

        @Override
        public boolean release(int decrement) {
            return response.release(decrement);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof AddressedEnvelope)) {
                return false;
            }

            @SuppressWarnings("unchecked")
            final AddressedEnvelope<?, SocketAddress> that = (AddressedEnvelope<?, SocketAddress>) obj;
            if (sender() == null) {
                if (that.sender() != null) {
                    return false;
                }
            } else if (!sender().equals(that.sender())) {
                return false;
            }

            if (recipient() == null) {
                if (that.recipient() != null) {
                    return false;
                }
            } else if (!recipient().equals(that.recipient())) {
                return false;
            }

            return response.equals(obj);
        }

        @Override
        public int hashCode() {
            int hashCode = response.hashCode();
            if (sender() != null) {
                hashCode = hashCode * 31 + sender().hashCode();
            }
            if (recipient() != null) {
                hashCode = hashCode * 31 + recipient().hashCode();
            }
            return hashCode;
        }
    }
}
