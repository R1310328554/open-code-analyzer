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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.dns.DefaultDnsQuestion;
import io.netty.handler.codec.dns.DefaultDnsRecordDecoder;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRawRecord;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsResponseCode;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static io.netty.handler.codec.dns.DnsResponseCode.NXDOMAIN;
import static io.netty.handler.codec.dns.DnsResponseCode.SERVFAIL;
import static io.netty.resolver.dns.DnsAddressDecoder.decodeAddress;
import static java.lang.Math.min;

/**
 * DNS 解析上下文抽象基类，驱动单次 resolve/query 的完整流程。
 * <p>负责 search 域、CNAME 跟随、多 nameserver 重试、重定向与结果缓存；子类实现记录类型转换与过滤。</p>
 */
abstract class DnsResolveContext<T> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DnsResolveContext.class);
    /** 系统属性：地址查询失败时是否最后尝试 CNAME 查询。 */
    private static final String PROP_TRY_FINAL_CNAME_ON_ADDRESS_LOOKUPS =
            "io.netty.resolver.dns.tryCnameOnAddressLookups";
    /** 是否在 A/AAAA 失败后额外发起 CNAME 查询。 */
    static boolean TRY_FINAL_CNAME_ON_ADDRESS_LOOKUPS;

    static {
        TRY_FINAL_CNAME_ON_ADDRESS_LOOKUPS =
                SystemPropertyUtil.getBoolean(PROP_TRY_FINAL_CNAME_ON_ADDRESS_LOOKUPS, false);

        if (logger.isDebugEnabled()) {
            logger.debug("-D{}: {}", PROP_TRY_FINAL_CNAME_ON_ADDRESS_LOOKUPS, TRY_FINAL_CNAME_ON_ADDRESS_LOOKUPS);
        }
    }

    private static final RuntimeException NXDOMAIN_QUERY_FAILED_EXCEPTION =
            DnsResolveContextException.newStatic("No answer found and NXDOMAIN response code returned",
            DnsResolveContext.class, "onResponse(..)");
    private static final RuntimeException CNAME_NOT_FOUND_QUERY_FAILED_EXCEPTION =
            DnsResolveContextException.newStatic("No matching CNAME record found",
            DnsResolveContext.class, "onResponseCNAME(..)");
    private static final RuntimeException NO_MATCHING_RECORD_QUERY_FAILED_EXCEPTION =
            DnsResolveContextException.newStatic("No matching record type found",
            DnsResolveContext.class, "onResponseAorAAAA(..)");
    private static final RuntimeException UNRECOGNIZED_TYPE_QUERY_FAILED_EXCEPTION =
            DnsResolveContextException.newStatic("Response type was unrecognized",
            DnsResolveContext.class, "onResponse(..)");
    private static final RuntimeException NAME_SERVERS_EXHAUSTED_EXCEPTION =
            DnsResolveContextException.newStatic("No name servers returned an answer",
            DnsResolveContext.class, "tryToFinishResolve(..)");
    private static final RuntimeException SERVFAIL_QUERY_FAILED_EXCEPTION =
            DnsErrorCauseException.newStatic("Query failed with SERVFAIL", SERVFAIL,
                    DnsResolveContext.class, "onResponse(..)");
    private static final RuntimeException NXDOMAIN_CAUSE_QUERY_FAILED_EXCEPTION =
            DnsErrorCauseException.newStatic("Query failed with NXDOMAIN", NXDOMAIN,
                    DnsResolveContext.class, "onResponse(..)");

    /** 所属 {@link DnsNameResolver}。 */
    final DnsNameResolver parent;
    private final Channel channel;
    private final Promise<?> originalPromise;
    /** 本次解析使用的 nameserver 地址流。 */
    private final DnsServerAddressStream nameServerAddrs;
    private final String hostname;
    private final int dnsClass;
    /** 期望的 DNS 记录类型（如 A、AAAA）。 */
    private final DnsRecordType[] expectedTypes;
    final DnsRecord[] additionals;

    /** 尚未完成的 DNS 查询 Future 集合。 */
    private final Set<Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>> queriesInProgress =
            Collections.newSetFromMap(
                    new IdentityHashMap<Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>, Boolean>());

    private List<T> finalResult;
    /** 剩余允许的 DNS 查询次数。 */
    private int allowedQueries;
    /** 是否已尝试过最终 CNAME 回退查询。 */
    private boolean triedCNAME;
    /** 是否已满足“提前完成”条件，仍会继续查询以填充缓存。 */
    private boolean completeEarly;

    DnsResolveContext(DnsNameResolver parent, Channel channel,
                      Promise<?> originalPromise, String hostname, int dnsClass, DnsRecordType[] expectedTypes,
                      DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs, int allowedQueries) {
        assert expectedTypes.length > 0;

        this.parent = parent;
        this.channel = channel;
        this.originalPromise = originalPromise;
        this.hostname = hostname;
        this.dnsClass = dnsClass;
        this.expectedTypes = expectedTypes;
        this.additionals = additionals;

        this.nameServerAddrs = ObjectUtil.checkNotNull(nameServerAddrs, "nameServerAddrs");
        this.allowedQueries = allowedQueries;
    }

    static final class DnsResolveContextException extends RuntimeException {

        private static final long serialVersionUID = 1209303419266433003L;

        private DnsResolveContextException(String message, boolean shared) {
            super(message, null, false, true);
            assert shared;
        }

        // Override fillInStackTrace() so we not populate the backtrace via a native call and so leak the
        // Classloader.
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }

        static DnsResolveContextException newStatic(String message, Class<?> clazz, String method) {
            final DnsResolveContextException exception = new DnsResolveContextException(message, true);
            return ThrowableUtil.unknownStackTrace(exception, clazz, method);
        }
    }

    /**
     * 本解析使用的 {@link Channel}。
     */
    Channel channel() {
        return channel;
    }

    /**
     * 解析过程中使用的 {@link DnsCache}。
     */
    DnsCache resolveCache() {
        return parent.resolveCache();
    }

    /**
     * 解析过程中使用的 {@link DnsCnameCache}。
     */
    DnsCnameCache cnameCache() {
        return parent.cnameCache();
    }

    /**
     * 解析过程中使用的 {@link AuthoritativeDnsServerCache}。
     */
    AuthoritativeDnsServerCache authoritativeDnsServerCache() {
        return parent.authoritativeDnsServerCache();
    }

    /**
     * 使用给定参数创建新的解析上下文（子类实现）。
     */
    abstract DnsResolveContext<T> newResolverContext(DnsNameResolver parent, Channel channel,
                                                     Promise<?> originalPromise,
                                                     String hostname,
                                                     int dnsClass, DnsRecordType[] expectedTypes,
                                                     DnsRecord[] additionals,
                                                     DnsServerAddressStream nameServerAddrs, int allowedQueries);

    /**
     * 将 {@link DnsRecord} 转换为结果类型 {@code T}。
     */
    abstract T convertRecord(DnsRecord record, String hostname, DnsRecord[] additionals, EventLoop eventLoop);

    /**
     * 过滤未加工结果，得到最终 DNS 解析列表（需考虑 {@link NetUtil#isIpV6AddressesPreferred()} 等 JDK 语义）。
     */
    abstract List<T> filterResults(List<T> unfiltered);

    /** 给定单条结果是否足以提前结束解析。 */
    abstract boolean isCompleteEarly(T resolved);

    /**
     * 最终结果是否允许重复条目。
     */
    abstract boolean isDuplicateAllowed();

    /**
     * 缓存一次成功解析。
     */
    abstract void cache(String hostname, DnsRecord[] additionals,
                        DnsRecord result, T convertedResult);

    /**
     * 缓存一次失败解析。
     */
    abstract void cache(String hostname, DnsRecord[] additionals,
                        UnknownHostException cause);

    /** 入口：按 search 域策略或直接 internalResolve。 */
    void resolve(final Promise<List<T>> promise) {
        final String[] searchDomains = parent.searchDomains();
        if (searchDomains.length == 0 || parent.ndots() == 0 || StringUtil.endsWith(hostname, '.')) {
            internalResolve(hostname, promise);
        } else {
            // 按 ndots 决定先查 FQDN 还是 hostname + searchDomain
            final boolean startWithoutSearchDomain = hasNDots();
            final String initialHostname = startWithoutSearchDomain ? hostname : hostname + '.' + searchDomains[0];
            final int initialSearchDomainIdx = startWithoutSearchDomain ? 0 : 1;

            final Promise<List<T>> searchDomainPromise = parent.executor().newPromise();
            searchDomainPromise.addListener(new FutureListener<List<T>>() {
                private int searchDomainIdx = initialSearchDomainIdx;
                @Override
                public void operationComplete(Future<List<T>> future) {
                    Throwable cause = future.cause();
                    if (cause == null) {
                        final List<T> result = future.getNow();
                        if (!promise.trySuccess(result)) {
                            for (T item : result) {
                                ReferenceCountUtil.safeRelease(item);
                            }
                        }
                    } else {
                        if (DnsNameResolver.isTransportOrTimeoutError(cause)) {
                            promise.tryFailure(new SearchDomainUnknownHostException(cause, hostname, expectedTypes,
                                    searchDomains));
                        } else if (searchDomainIdx < searchDomains.length) {
                            Promise<List<T>> newPromise = parent.executor().newPromise();
                            newPromise.addListener(this);
                            doSearchDomainQuery(hostname + '.' + searchDomains[searchDomainIdx++], newPromise);
                        } else if (!startWithoutSearchDomain) {
                            internalResolve(hostname, promise);
                        } else {
                            promise.tryFailure(new SearchDomainUnknownHostException(cause, hostname, expectedTypes,
                                    searchDomains));
                        }
                    }
                }
            });
            doSearchDomainQuery(initialHostname, searchDomainPromise);
        }
    }

    private boolean hasNDots() {
        for (int idx = hostname.length() - 1, dots = 0; idx >= 0; idx--) {
            if (hostname.charAt(idx) == '.' && ++dots >= parent.ndots()) {
                return true;
            }
        }
        return false;
    }

    private static final class SearchDomainUnknownHostException extends UnknownHostException {
        private static final long serialVersionUID = -8573510133644997085L;

        SearchDomainUnknownHostException(Throwable cause, String originalHostname,
                DnsRecordType[] queryTypes, String[] searchDomains) {
            super("Failed to resolve '" + originalHostname + "' " + Arrays.toString(queryTypes) +
                    " and search domain query for configured domains failed as well: " +
                    Arrays.toString(searchDomains));
            setStackTrace(cause.getStackTrace());
            // Preserve the cause
            initCause(cause.getCause());
        }

        // Suppress a warning since this method doesn't need synchronization
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    void doSearchDomainQuery(String hostname, Promise<List<T>> nextPromise) {
        DnsResolveContext<T> nextContext = newResolverContext(parent, channel,
                originalPromise, hostname, dnsClass,
                expectedTypes, additionals, nameServerAddrs,
                parent.maxQueriesPerResolve());
        nextContext.internalResolve(hostname, nextPromise);
    }

    private static String hostnameWithDot(String name) {
        if (StringUtil.endsWith(name, '.')) {
            return name;
        }
        return name + '.';
    }

    // 从 CNAME 缓存链式解析直至无更多映射；检测环路并提前返回
    //
    // Visible for testing only
    static String cnameResolveFromCache(DnsCnameCache cnameCache, String name) throws UnknownHostException {
        String first = cnameCache.get(hostnameWithDot(name));
        if (first == null) {
            // 缓存中无任何 CNAME
            return name;
        }

        String second = cnameCache.get(hostnameWithDot(first));
        if (second == null) {
            // 无后续链，返回首条映射
            return first;
        }

        checkCnameLoop(name, first, second);
        return cnameResolveFromCacheLoop(cnameCache, name, first, second);
    }

    private static String cnameResolveFromCacheLoop(
            DnsCnameCache cnameCache, String hostname, String first, String mapping) throws UnknownHostException {
        // Floyd 龟兔算法检测 CNAME 环
        boolean advance = false;

        String name = mapping;
        // 沿 CNAME 缓存继续解析
        while ((mapping = cnameCache.get(hostnameWithDot(name))) != null) {
            checkCnameLoop(hostname, first, mapping);
            name = mapping;
            if (advance) {
                first = cnameCache.get(first);
            }
            advance = !advance;
        }
        return name;
    }

    private static void checkCnameLoop(String hostname, String first, String second) throws UnknownHostException {
        if (first.equals(second)) {
            // CNAME 环，解析失败
            throw new UnknownHostException("CNAME loop detected for '" + hostname + '\'');
        }
    }
    /** 核心解析：CNAME 缓存预解析后向各 expectedType 发起查询。 */
    private void internalResolve(String name, Promise<List<T>> promise) {
        try {
            // 先沿 CNAME 缓存展开真实查询名
            name = cnameResolveFromCache(cnameCache(), name);
        } catch (Throwable cause) {
            promise.tryFailure(cause);
            return;
        }

        try {
            DnsServerAddressStream nameServerAddressStream = getNameServers(name);

            final int end = expectedTypes.length - 1;
            for (int i = 0; i < end; ++i) {
                if (!query(name, expectedTypes[i], nameServerAddressStream.duplicate(), false, promise)) {
                    return;
                }
            }
            query(name, expectedTypes[end], nameServerAddressStream, false, promise);
        } finally {
            // 刷新 Channel 上已提交的 DNS 查询
            channel.flush();
        }
    }

    /**
     * 从权威服务器缓存获取 hostname 对应的 {@link DnsServerAddressStream}，未命中则 {@code null}。
     */
    private DnsServerAddressStream getNameServersFromCache(String hostname) {
        int len = hostname.length();

        if (len == 0) {
            // 根服务器从不走缓存
            return null;
        }

        // 缓存键一律带尾随 '.'
        if (hostname.charAt(len - 1) != '.') {
            hostname += ".";
        }

        int idx = hostname.indexOf('.');
        if (idx == hostname.length() - 1) {
            // '.' 单独作为域名时不查缓存
            return null;
        }

        // 从最接近的父域向上逐级查找
        for (;;) {
            // Skip '.' as well.
            hostname = hostname.substring(idx + 1);

            int idx2 = hostname.indexOf('.');
            if (idx2 <= 0 || idx2 == hostname.length() - 1) {
                // We are not interested in handling '.TLD.' as we should never serve the root servers from cache.
                return null;
            }
            idx = idx2;

            DnsServerAddressStream entries = authoritativeDnsServerCache().get(hostname);
            if (entries != null) {
                // The returned List may contain unresolved InetSocketAddress instances that will be
                // resolved on the fly in query(....).
                return entries;
            }
        }
    }

    private void query(final DnsServerAddressStream nameServerAddrStream,
                       final int nameServerAddrStreamIndex,
                       final DnsQuestion question,
                       final DnsQueryLifecycleObserver queryLifecycleObserver,
                       final boolean flush,
                       final Promise<List<T>> promise,
                       final Throwable cause) {
        if (completeEarly || nameServerAddrStreamIndex >= nameServerAddrStream.size() ||
                allowedQueries == 0 || originalPromise.isCancelled() || promise.isCancelled()) {
            tryToFinishResolve(nameServerAddrStream, nameServerAddrStreamIndex, question, queryLifecycleObserver,
                               promise, cause);
            return;
        }

        --allowedQueries;

        final InetSocketAddress nameServerAddr = nameServerAddrStream.next();
        if (nameServerAddr.isUnresolved()) {
            queryUnresolvedNameServer(nameServerAddr, nameServerAddrStream, nameServerAddrStreamIndex, question,
                                      queryLifecycleObserver, promise, cause);
            return;
        }
        final Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> queryPromise =
                channel.eventLoop().newPromise();

        final long queryStartTimeNanos;
        final boolean isFeedbackAddressStream;
        if (nameServerAddrStream instanceof DnsServerResponseFeedbackAddressStream) {
            queryStartTimeNanos = System.nanoTime();
            isFeedbackAddressStream = true;
        } else {
            queryStartTimeNanos = -1;
            isFeedbackAddressStream = false;
        }

        final Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> f =
                parent.doQuery(channel, nameServerAddr, question,
                        queryLifecycleObserver, additionals, flush, queryPromise);

        queriesInProgress.add(f);

        f.addListener((FutureListener<AddressedEnvelope<DnsResponse, InetSocketAddress>>) future -> {
            queriesInProgress.remove(future);

            if (promise.isDone() || future.isCancelled()) {
                queryLifecycleObserver.queryCancelled(allowedQueries);

                // Check if we need to release the envelope itself. If the query was cancelled the getNow() will
                // return null as well as the Future will be failed with a CancellationException.
                AddressedEnvelope<DnsResponse, InetSocketAddress> result = future.getNow();
                if (result != null) {
                    result.release();
                }
                return;
            }

            final Throwable queryCause = future.cause();
            try {
                if (queryCause == null) {
                    if (isFeedbackAddressStream) {
                        final DnsServerResponseFeedbackAddressStream feedbackNameServerAddrStream =
                                (DnsServerResponseFeedbackAddressStream) nameServerAddrStream;
                        feedbackNameServerAddrStream.feedbackSuccess(nameServerAddr,
                                System.nanoTime() - queryStartTimeNanos);
                    }
                    onResponse(nameServerAddrStream, nameServerAddrStreamIndex, question, future.getNow(),
                               queryLifecycleObserver, promise);
                } else {
                    // 服务器无响应或 I/O 错误，尝试下一个 nameserver
                    if (isFeedbackAddressStream) {
                        final DnsServerResponseFeedbackAddressStream feedbackNameServerAddrStream =
                                (DnsServerResponseFeedbackAddressStream) nameServerAddrStream;
                        feedbackNameServerAddrStream.feedbackFailure(nameServerAddr, queryCause,
                                System.nanoTime() - queryStartTimeNanos);
                    }
                    queryLifecycleObserver.queryFailed(queryCause);
                    query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question,
                          newDnsQueryLifecycleObserver(question), true, promise, queryCause);
                }
            } finally {
                tryToFinishResolve(nameServerAddrStream, nameServerAddrStreamIndex, question,
                                   // queryLifecycleObserver has already been terminated at this point so we must
                                   // not allow it to be terminated again by tryToFinishResolve.
                                   NoopDnsQueryLifecycleObserver.INSTANCE,
                                   promise, queryCause);
            }
        });
    }

    private void queryUnresolvedNameServer(final InetSocketAddress nameServerAddr,
                                           final DnsServerAddressStream nameServerAddrStream,
                                           final int nameServerAddrStreamIndex,
                                           final DnsQuestion question,
                                           final DnsQueryLifecycleObserver queryLifecycleObserver,
                                           final Promise<List<T>> promise,
                                           final Throwable cause) {
        final String nameServerName = nameServerAddr.getHostString();
        assert nameServerName != null;

        // 占位 Future，避免在解析未完成 nameserver 时过早 finish
        final Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> resolveFuture = parent.executor()
                .newSucceededFuture(null);
        queriesInProgress.add(resolveFuture);

        Promise<List<InetAddress>> resolverPromise = parent.executor().newPromise();
        resolverPromise.addListener((FutureListener<List<InetAddress>>) future -> {
            // Remove placeholder.
            queriesInProgress.remove(resolveFuture);

            if (future.isSuccess()) {
                List<InetAddress> resolvedAddresses = future.getNow();
                DnsServerAddressStream addressStream = new CombinedDnsServerAddressStream(
                        nameServerAddr, resolvedAddresses, nameServerAddrStream);
                query(addressStream, nameServerAddrStreamIndex, question,
                      queryLifecycleObserver, true, promise, cause);
            } else {
                // 忽略该服务器，尝试流中下一个
                query(nameServerAddrStream, nameServerAddrStreamIndex + 1,
                      question, queryLifecycleObserver, true, promise, cause);
            }
        });
        DnsCache resolveCache = resolveCache();
        if (!DnsNameResolver.doResolveAllCached(nameServerName, additionals, resolverPromise, resolveCache,
                parent.searchDomains(), parent.ndots(), parent.resolvedInternetProtocolFamiliesUnsafe())) {

            new DnsAddressResolveContext(parent, channel,
                    originalPromise, nameServerName, additionals, parent.newNameServerAddressStream(nameServerName),
                    // Resolving the unresolved nameserver must be limited by allowedQueries
                    // so we eventually fail
                    allowedQueries,
                    resolveCache,
                    redirectAuthoritativeDnsServerCache(authoritativeDnsServerCache()), false)
                    .resolve(resolverPromise);
        }
    }

    private static AuthoritativeDnsServerCache redirectAuthoritativeDnsServerCache(
            AuthoritativeDnsServerCache authoritativeDnsServerCache) {
        // 避免重复包装导致 StackOverflowError
        if (authoritativeDnsServerCache instanceof RedirectAuthoritativeDnsServerCache) {
            return authoritativeDnsServerCache;
        }
        return new RedirectAuthoritativeDnsServerCache(authoritativeDnsServerCache);
    }

    private static final class RedirectAuthoritativeDnsServerCache implements AuthoritativeDnsServerCache {
        private final AuthoritativeDnsServerCache wrapped;

        RedirectAuthoritativeDnsServerCache(AuthoritativeDnsServerCache authoritativeDnsServerCache) {
            wrapped = authoritativeDnsServerCache;
        }

        @Override
        public DnsServerAddressStream get(String hostname) {
            // 跟随重定向时不读缓存，防止环路；仅初始查询使用缓存
            return null;
        }

        @Override
        public void cache(String hostname, InetSocketAddress address, long originalTtl, EventLoop loop) {
            wrapped.cache(hostname, address, originalTtl, loop);
        }

        @Override
        public void clear() {
            wrapped.clear();
        }

        @Override
        public boolean clear(String hostname) {
            return wrapped.clear(hostname);
        }
    }

    private void onResponse(final DnsServerAddressStream nameServerAddrStream, final int nameServerAddrStreamIndex,
                            final DnsQuestion question, AddressedEnvelope<DnsResponse, InetSocketAddress> envelope,
                            final DnsQueryLifecycleObserver queryLifecycleObserver,
                            Promise<List<T>> promise) {
        try {
            final DnsResponse res = envelope.content();
            final DnsResponseCode code = res.code();
            if (code == DnsResponseCode.NOERROR) {
                if (handleRedirect(question, envelope, queryLifecycleObserver, promise)) {
                    // Was a redirect so return here as everything else is handled in handleRedirect(...)
                    return;
                }
                final DnsRecordType type = question.type();

                if (type == DnsRecordType.CNAME) {
                    onResponseCNAME(question,
                            buildAliasMap(question.name(), envelope.content(), cnameCache(), parent.executor()),
                            queryLifecycleObserver, promise);
                    return;
                }

                for (DnsRecordType expectedType : expectedTypes) {
                    if (type == expectedType) {
                        onExpectedResponse(question, envelope, queryLifecycleObserver, promise);
                        return;
                    }
                }

                queryLifecycleObserver.queryFailed(UNRECOGNIZED_TYPE_QUERY_FAILED_EXCEPTION);
                return;
            }

            // Retry with the next server if the server did not tell us that the domain does not exist.
            if (code != DnsResponseCode.NXDOMAIN) {
                query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question,
                      queryLifecycleObserver.queryNoAnswer(code), true, promise, cause(code));
            } else {
                queryLifecycleObserver.queryFailed(NXDOMAIN_QUERY_FAILED_EXCEPTION);

                // Try with the next server if is not authoritative for the domain.
                //
                // From https://tools.ietf.org/html/rfc1035 :
                //
                //   RCODE        Response code - this 4 bit field is set as part of
                //                responses.  The values have the following
                //                interpretation:
                //
                //                ....
                //                ....
                //
                //                3               Name Error - Meaningful only for
                //                                responses from an authoritative name
                //                                server, this code signifies that the
                //                                domain name referenced in the query does
                //                                not exist.
                //                ....
                //                ....
                if (!res.isAuthoritativeAnswer()) {
                    query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question,
                            newDnsQueryLifecycleObserver(question), true, promise, cause(code));
                } else {
                    // Failed with NX cause - distinction between NXDOMAIN vs a timeout
                    tryToFinishResolve(nameServerAddrStream, nameServerAddrStreamIndex, question,
                            queryLifecycleObserver, promise, NXDOMAIN_CAUSE_QUERY_FAILED_EXCEPTION);
                }
            }
        } finally {
            ReferenceCountUtil.safeRelease(envelope);
        }
    }

    /**
     * 若响应为重定向则处理并返回 {@code true}（已发起后续查询）。
     */
    private boolean handleRedirect(
            DnsQuestion question, AddressedEnvelope<DnsResponse, InetSocketAddress> envelope,
            final DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
        final DnsResponse res = envelope.content();

        // 无 ANSWER 时可能为非权威 NS，需处理重定向
        if (res.count(DnsSection.ANSWER) == 0) {
            AuthoritativeNameServerList serverNames = extractAuthoritativeNameServers(question.name(), res);
            if (serverNames != null) {
                int additionalCount = res.count(DnsSection.ADDITIONAL);

                AuthoritativeDnsServerCache authoritativeDnsServerCache = authoritativeDnsServerCache();
                for (int i = 0; i < additionalCount; i++) {
                    final DnsRecord r = res.recordAt(DnsSection.ADDITIONAL, i);

                    if (r.type() == DnsRecordType.A && !parent.supportsARecords() ||
                        r.type() == DnsRecordType.AAAA && !parent.supportsAAAARecords()) {
                        continue;
                    }

                    // We may have multiple ADDITIONAL entries for the same nameserver name. For example one AAAA and
                    // one A record.
                    serverNames.handleWithAdditional(parent, r, authoritativeDnsServerCache);
                }

                // Process all unresolved nameservers as well.
                serverNames.handleWithoutAdditionals(parent, resolveCache(), authoritativeDnsServerCache);

                List<InetSocketAddress> addresses = serverNames.addressList();

                // 允许用户排序或过滤重定向使用的 nameserver
                DnsServerAddressStream serverStream = parent.newRedirectDnsServerStream(
                        question.name(), addresses);

                if (serverStream != null) {
                    query(serverStream, 0, question,
                          queryLifecycleObserver.queryRedirected(new DnsAddressStreamList(serverStream)),
                          true, promise, null);
                    return true;
                }
            }
        }
        return false;
    }

    private static Throwable cause(final DnsResponseCode code) {
        assert code != null;
        if (SERVFAIL.intValue() == code.intValue()) {
            return SERVFAIL_QUERY_FAILED_EXCEPTION;
        } else if (NXDOMAIN.intValue() == code.intValue()) {
            return NXDOMAIN_CAUSE_QUERY_FAILED_EXCEPTION;
        }

        return null;
    }

    private static final class DnsAddressStreamList extends AbstractList<InetSocketAddress> {

        private final DnsServerAddressStream duplicate;
        private List<InetSocketAddress> addresses;

        DnsAddressStreamList(DnsServerAddressStream stream) {
            duplicate = stream.duplicate();
        }

        @Override
        public InetSocketAddress get(int index) {
            if (addresses == null) {
                DnsServerAddressStream stream = duplicate.duplicate();
                addresses = new ArrayList<InetSocketAddress>(size());
                for (int i = 0; i < stream.size(); i++) {
                    addresses.add(stream.next());
                }
            }
            return addresses.get(index);
        }

        @Override
        public int size() {
            return duplicate.size();
        }

        @Override
        public Iterator<InetSocketAddress> iterator() {
            return new Iterator<InetSocketAddress>() {
                private final DnsServerAddressStream stream = duplicate.duplicate();
                private int i;

                @Override
                public boolean hasNext() {
                    return i < stream.size();
                }

                @Override
                public InetSocketAddress next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    i++;
                    return stream.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /**
     * 返回 {@link DnsSection#AUTHORITY} 中的 {@link AuthoritativeNameServerList}，无则 {@code null}。
     */
    private static AuthoritativeNameServerList extractAuthoritativeNameServers(String questionName, DnsResponse res) {
        int authorityCount = res.count(DnsSection.AUTHORITY);
        if (authorityCount == 0) {
            return null;
        }

        AuthoritativeNameServerList serverNames = new AuthoritativeNameServerList(questionName);
        for (int i = 0; i < authorityCount; i++) {
            serverNames.add(res.recordAt(DnsSection.AUTHORITY, i));
        }
        return serverNames.isEmpty() ? null : serverNames;
    }

    private void onExpectedResponse(
            DnsQuestion question, AddressedEnvelope<DnsResponse, InetSocketAddress> envelope,
            final DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {

        // A/AAAA 查询常附带 CNAME 记录
        final DnsResponse response = envelope.content();
        final Map<String, String> cnames = buildAliasMap(question.name(), response, cnameCache(), parent.executor());
        final int answerCount = response.count(DnsSection.ANSWER);

        boolean found = false;
        boolean completeEarly = this.completeEarly;
        boolean cnameNeedsFollow = !cnames.isEmpty();
        for (int i = 0; i < answerCount; i ++) {
            final DnsRecord r = response.recordAt(DnsSection.ANSWER, i);
            final DnsRecordType type = r.type();
            boolean matches = false;
            for (DnsRecordType expectedType : expectedTypes) {
                if (type == expectedType) {
                    matches = true;
                    break;
                }
            }

            if (!matches) {
                continue;
            }

            final String questionName = question.name().toLowerCase(Locale.US);
            final String recordName = r.name().toLowerCase(Locale.US);

            // 确保记录名与问题域匹配（含 CNAME 链与 search 域）
            if (!recordName.equals(questionName)) {
                Map<String, String> cnamesCopy = new HashMap<String, String>(cnames);
                // Even if the record's name is not exactly same, it might be an alias defined in the CNAME records.
                String resolved = questionName;
                do {
                    resolved = cnamesCopy.remove(resolved);
                    if (recordName.equals(resolved)) {
                        // We followed a CNAME chain that was part of the response without any extra queries.
                        cnameNeedsFollow = false;
                        break;
                    }
                } while (resolved != null);

                if (resolved == null) {
                    assert questionName.isEmpty() || questionName.charAt(questionName.length() - 1) == '.';

                    for (String searchDomain : parent.searchDomains()) {
                        if (searchDomain.isEmpty()) {
                            continue;
                        }

                        final String fqdn;
                        if (searchDomain.charAt(searchDomain.length() - 1) == '.') {
                            fqdn = questionName + searchDomain;
                        } else {
                            fqdn = questionName + searchDomain + '.';
                        }
                        if (recordName.equals(fqdn)) {
                            resolved = recordName;
                            break;
                        }
                    }
                    if (resolved == null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("{} Ignoring record {} for [{}: {}] as it contains a different name than " +
                                            "the question name [{}]. Cnames: {}, Search domains: {}",
                                    channel, r.toString(), response.id(), envelope.sender(),
                                    questionName, cnames, parent.searchDomains());
                        }
                        continue;
                    }
                }
            }

            final T converted = convertRecord(r, hostname, additionals, parent.executor());
            if (converted == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} Ignoring record {} for [{}: {}] as the converted record is null. "
                                    + "Hostname [{}], Additionals: {}",
                            channel, r.toString(), response.id(),
                            envelope.sender(), hostname, additionals);
                }
                continue;
            }

            boolean shouldRelease = false;
            // Check if we did determine we wanted to complete early before. If this is the case we want to not
            // include the result
            if (!completeEarly) {
                completeEarly = isCompleteEarly(converted);
            }

            // Check if the promise was done already, and only if not add things to the finalResult. Otherwise lets
            // just release things after we cached it.
            if (!promise.isDone()) {
                // 避免 finalResult 中出现重复（优先 ArrayList 而非 HashSet 以降低常见路径开销）
                //
                // While using a LinkedHashSet or HashSet may sound like the perfect fit for this we will use an
                // ArrayList here as duplicates should be found quite unfrequently in the wild and we dont want to pay
                // for the extra memory copy and allocations in this cases later on.
                if (finalResult == null) {
                    finalResult = new ArrayList<T>(8);
                    finalResult.add(converted);
                } else if (isDuplicateAllowed() || !finalResult.contains(converted)) {
                    finalResult.add(converted);
                } else {
                    shouldRelease = true;
                }
            } else {
                shouldRelease = true;
            }

            cache(hostname, additionals, r, converted);
            found = true;

            if (shouldRelease) {
                ReferenceCountUtil.release(converted);
            }
            // 不 break，继续解码/缓存所有 A/AAAA 记录
        }

        if (found && !cnameNeedsFollow) {
            // 已有正确结果且无需再跟 CNAME
            if (completeEarly) {
                this.completeEarly = true;
            }
            queryLifecycleObserver.querySucceed();
        } else if (cnames.isEmpty()) {
            queryLifecycleObserver.queryFailed(NO_MATCHING_RECORD_QUERY_FAILED_EXCEPTION);
        } else {
            queryLifecycleObserver.querySucceed();
            // 响应中含 CNAME，需继续查询别名目标
            onResponseCNAME(question, cnames, newDnsQueryLifecycleObserver(question), promise);
        }
    }

    private void onResponseCNAME(
            DnsQuestion question, Map<String, String> cnames,
            final DnsQueryLifecycleObserver queryLifecycleObserver,
            Promise<List<T>> promise) {

        // 将问题中的主机名沿 CNAME 映射解析为真实名
        String resolved = question.name().toLowerCase(Locale.US);
        boolean found = false;
        while (!cnames.isEmpty()) { // Do not attempt to call Map.remove() when the Map is empty
                                    // because it can be Collections.emptyMap()
                                    // whose remove() throws a UnsupportedOperationException.
            final String next = cnames.remove(resolved);
            if (next != null) {
                found = true;
                resolved = next;
            } else {
                break;
            }
        }

        if (found) {
            followCname(question, resolved, queryLifecycleObserver, promise);
        } else {
            queryLifecycleObserver.queryFailed(CNAME_NOT_FOUND_QUERY_FAILED_EXCEPTION);
        }
    }

    private static Map<String, String> buildAliasMap(
            String queryName, DnsResponse response, DnsCnameCache cache, EventLoop loop) {
        final int answerCount = response.count(DnsSection.ANSWER);
        Map<String, String> cnames = null;
        for (int i = 0; i < answerCount; i ++) {
            final DnsRecord r = response.recordAt(DnsSection.ANSWER, i);
            final DnsRecordType type = r.type();
            if (type != DnsRecordType.CNAME) {
                continue;
            }

            if (!(r instanceof DnsRawRecord)) {
                continue;
            }

            final ByteBuf recordContent = ((ByteBufHolder) r).content();
            final String domainName = decodeDomainName(recordContent);
            if (domainName == null) {
                continue;
            }

            if (cnames == null) {
                cnames = new HashMap<String, String>(min(8, answerCount));
            }

            String name = r.name().toLowerCase(Locale.US);
            String mapping = domainName.toLowerCase(Locale.US);

            // 将 CNAME 写入缓存（仅当 owner 在原始查询名的 bailiwick 内）
            String nameWithDot = hostnameWithDot(name);
            String mappingWithDot = hostnameWithDot(mapping);
            if (!nameWithDot.equalsIgnoreCase(mappingWithDot)) {
                String queryNameWithDot = hostnameWithDot(queryName.toLowerCase(Locale.US));
                // 仅当 CNAME owner 在查询名管辖范围内才缓存
                boolean inBailiwick = nameWithDot.equals(queryNameWithDot) ||
                        nameWithDot.endsWith("." + queryNameWithDot);
                if (inBailiwick) {
                    cache.cache(nameWithDot, mappingWithDot, r.timeToLive(), loop);
                }
                cnames.put(name, mapping);
            }
        }

        return cnames != null? cnames : Collections.<String, String>emptyMap();
    }

    private void tryToFinishResolve(final DnsServerAddressStream nameServerAddrStream,
                                    final int nameServerAddrStreamIndex,
                                    final DnsQuestion question,
                                    final DnsQueryLifecycleObserver queryLifecycleObserver,
                                    final Promise<List<T>> promise,
                                    final Throwable cause) {

        // 仍有进行中的查询时不结束
        if (!completeEarly && !queriesInProgress.isEmpty()) {
            queryLifecycleObserver.queryCancelled(allowedQueries);

            // 尚有查询未完成，等下一次回调再 tryToFinish
            return;
        }

        // 尚无成功结果
        if (finalResult == null) {
            if (nameServerAddrStreamIndex < nameServerAddrStream.size()) {
                if (queryLifecycleObserver == NoopDnsQueryLifecycleObserver.INSTANCE) {
                    // If the queryLifecycleObserver has already been terminated we should create a new one for this
                    // fresh query.
                    query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question,
                          newDnsQueryLifecycleObserver(question), true, promise, cause);
                } else {
                    query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, queryLifecycleObserver,
                          true, promise, cause);
                }
                return;
            }

            queryLifecycleObserver.queryFailed(NAME_SERVERS_EXHAUSTED_EXCEPTION);

            // .. and we could not find any expected records.

            // 可选：A/AAAA 全失败时最后尝试 CNAME（受系统属性控制）
            if (TRY_FINAL_CNAME_ON_ADDRESS_LOOKUPS) {
                // cause 非空表示超时/取消/传输错误，此时不应再查 CNAME
                final boolean isValidResponse =
                        cause == NXDOMAIN_CAUSE_QUERY_FAILED_EXCEPTION || cause == SERVFAIL_QUERY_FAILED_EXCEPTION;
                if ((cause == null || isValidResponse) && !triedCNAME &&
                        (question.type() == DnsRecordType.A || question.type() == DnsRecordType.AAAA)) {
                    // 最后手段：向 nameserver 查询 CNAME
                    triedCNAME = true;

                    query(hostname, DnsRecordType.CNAME, getNameServers(hostname), true, promise);
                    return;
                }
            }
        } else {
            queryLifecycleObserver.queryCancelled(allowedQueries);
        }

        // 至少有一条结果或已尝试 CNAME 回退
        finishResolve(promise, cause);
    }

    /** 将 finalResult 写入 promise 或构造 UnknownHostException 失败。 */
    private void finishResolve(Promise<List<T>> promise, Throwable cause) {
        // completeEarly 时仍可能取消进行中的查询以尽快返回
        if (!completeEarly && !queriesInProgress.isEmpty()) {
            // 已有最终结果，取消剩余进行中的查询
            for (Iterator<Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>> i = queriesInProgress.iterator();
                 i.hasNext();) {
                Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> f = i.next();
                i.remove();

                f.cancel(false);
            }
        }

        if (finalResult != null) {
            if (!promise.isDone()) {
                // 至少解析到一条记录
                final List<T> result = filterResults(finalResult);
                // Lets replace the previous stored result.
                finalResult = Collections.emptyList();
                if (!DnsNameResolver.trySuccess(promise, result)) {
                    for (T item : result) {
                        ReferenceCountUtil.safeRelease(item);
                    }
                }
            } else {
                // This should always be the case as we replaced the list once notify the promise with an empty one
                // and never add to it again.
                assert finalResult.isEmpty();
            }
            return;
        }

        // 未解析到任何地址
        final int maxAllowedQueries = parent.maxQueriesPerResolve();
        final int tries = maxAllowedQueries - allowedQueries;
        final StringBuilder buf = new StringBuilder(64);

        buf.append("Failed to resolve '").append(hostname).append("' ").append(Arrays.toString(expectedTypes));
        if (tries > 1) {
            if (tries < maxAllowedQueries) {
                buf.append(" after ")
                   .append(tries)
                   .append(" queries ");
            } else {
                buf.append(". Exceeded max queries per resolve ")
                .append(maxAllowedQueries)
                .append(' ');
            }
        }
        final UnknownHostException unknownHostException = new UnknownHostException(buf.toString());
        if (cause == null) {
            // 非 I/O/超时类失败才写入负缓存
            cache(hostname, additionals, unknownHostException);
        } else {
            unknownHostException.initCause(cause);
        }
        promise.tryFailure(unknownHostException);
    }

    /** 解码 DNS 域名；损坏帧时返回 null。 */
    static String decodeDomainName(ByteBuf in) {
        in.markReaderIndex();
        try {
            return DefaultDnsRecordDecoder.decodeName(in);
        } catch (CorruptedFrameException e) {
            // In this case we just return null.
            return null;
        } finally {
            in.resetReaderIndex();
        }
    }

    /** 获取 name 对应的 nameserver 流（缓存、CNAME 跟随或 duplicate 原流）。 */
    private DnsServerAddressStream getNameServers(String name) {
        DnsServerAddressStream stream = getNameServersFromCache(name);
        if (stream == null) {
            // CNAME 跟随等场景需向 parent 索取新流；同名则 duplicate 以正确推进索引
            if (name.equals(hostname)) {
                return nameServerAddrs.duplicate();
            }
            return parent.newNameServerAddressStream(name);
        }
        return stream;
    }

    private void followCname(DnsQuestion question, String cname, DnsQueryLifecycleObserver queryLifecycleObserver,
                             Promise<List<T>> promise) {
        final DnsQuestion cnameQuestion;
        final DnsServerAddressStream stream;
        try {
            cname = cnameResolveFromCache(cnameCache(), cname);
            stream = getNameServers(cname);
            cnameQuestion = new DefaultDnsQuestion(cname, question.type(), dnsClass);
        } catch (Throwable cause) {
            queryLifecycleObserver.queryFailed(cause);
            PlatformDependent.throwException(cause);
            return;
        }
        query(stream, 0, cnameQuestion, queryLifecycleObserver.queryCNAMEd(cnameQuestion),
              true, promise, null);
    }

    private boolean query(String hostname, DnsRecordType type, DnsServerAddressStream dnsServerAddressStream,
                          boolean flush, Promise<List<T>> promise) {
        final DnsQuestion question;
        try {
            question = new DefaultDnsQuestion(hostname, type, dnsClass);
        } catch (Throwable cause) {
            // Assume a single failure means that queries will succeed. If the hostname is invalid for one type
            // there is no case where it is known to be valid for another type.
            promise.tryFailure(new IllegalArgumentException("Unable to create DNS Question for: [" + hostname + ", " +
                    type + ']', cause));
            return false;
        }
        query(dnsServerAddressStream, 0, question, newDnsQueryLifecycleObserver(question), flush, promise, null);
        return true;
    }

    private DnsQueryLifecycleObserver newDnsQueryLifecycleObserver(DnsQuestion question) {
        return parent.dnsQueryLifecycleObserverFactory().newDnsQueryLifecycleObserver(question);
    }

    /** 将未解析 nameserver 替换为已解析地址列表的组合地址流。 */
    private final class CombinedDnsServerAddressStream implements DnsServerAddressStream {
        private final InetSocketAddress replaced;
        private final DnsServerAddressStream originalStream;
        private final List<InetAddress> resolvedAddresses;
        private Iterator<InetAddress> resolved;

        CombinedDnsServerAddressStream(InetSocketAddress replaced, List<InetAddress> resolvedAddresses,
                                       DnsServerAddressStream originalStream) {
            this.replaced = replaced;
            this.resolvedAddresses = resolvedAddresses;
            this.originalStream = originalStream;
            resolved = resolvedAddresses.iterator();
        }

        @Override
        public InetSocketAddress next() {
            if (resolved.hasNext()) {
                return nextResolved0();
            }
            InetSocketAddress address = originalStream.next();
            if (address.equals(replaced)) {
                resolved = resolvedAddresses.iterator();
                return nextResolved0();
            }
            return address;
        }

        private InetSocketAddress nextResolved0() {
            return parent.newRedirectServerAddress(resolved.next());
        }

        @Override
        public int size() {
            return originalStream.size() + resolvedAddresses.size() - 1;
        }

        @Override
        public DnsServerAddressStream duplicate() {
            return new CombinedDnsServerAddressStream(replaced, resolvedAddresses, originalStream.duplicate());
        }
    }

    /**
     * 保存某域最接近的权威 DNS 服务器链表。
     */
    private static final class AuthoritativeNameServerList {

        private final String questionName;

        // 链表通常较短，无需双向链表
        private AuthoritativeNameServer head;

        private int nameServerCount;

        AuthoritativeNameServerList(String questionName) {
            this.questionName = questionName.toLowerCase(Locale.US);
        }

        void add(DnsRecord r) {
            if (r.type() != DnsRecordType.NS || !(r instanceof DnsRawRecord)) {
                return;
            }

            // 仅保留与问题域后缀匹配的 NS
            if (questionName.length() <  r.name().length()) {
                return;
            }

            String recordName = r.name().toLowerCase(Locale.US);

            int dots = 0;
            for (int a = recordName.length() - 1, b = questionName.length() - 1; a >= 0; a--, b--) {
                char c = recordName.charAt(a);
                if (questionName.charAt(b) != c) {
                    return;
                }
                if (c == '.') {
                    dots++;
                }
            }

            if (head != null && head.dots > dots) {
                // 已有更近匹配，忽略本条
                return;
            }

            final ByteBuf recordContent = ((ByteBufHolder) r).content();
            final String domainName = decodeDomainName(recordContent);
            if (domainName == null) {
                // Could not be parsed, ignore.
                return;
            }

            // 保留与 qName 最接近的 nameserver，dots 更少则丢弃旧链
            if (head == null || head.dots < dots) {
                nameServerCount = 1;
                head = new AuthoritativeNameServer(dots, r.timeToLive(), recordName, domainName);
            } else if (head.dots == dots) {
                AuthoritativeNameServer serverName = head;
                while (serverName.next != null) {
                    serverName = serverName.next;
                }
                serverName.next = new AuthoritativeNameServer(dots, r.timeToLive(), recordName, domainName);
                nameServerCount++;
            }
        }

        /** 用 ADDITIONAL 段 A/AAAA 填充 NS 地址并写入权威缓存。 */
        void handleWithAdditional(
                DnsNameResolver parent, DnsRecord r, AuthoritativeDnsServerCache authoritativeCache) {
            // Just walk the linked-list and mark the entry as handled when matched.
            AuthoritativeNameServer serverName = head;

            String nsName = r.name();
            InetAddress resolved = decodeAddress(r, nsName, parent.isDecodeIdn());
            if (resolved == null) {
                // Could not parse the address, just ignore.
                return;
            }

            while (serverName != null) {
                if (serverName.nsName.equalsIgnoreCase(nsName)) {
                    if (serverName.address != null) {
                        // We received multiple ADDITIONAL records for the same name.
                        // Search for the last we insert before and then append a new one.
                        while (serverName.next != null && serverName.next.isCopy) {
                            serverName = serverName.next;
                        }
                        AuthoritativeNameServer server = new AuthoritativeNameServer(serverName);
                        server.next = serverName.next;
                        serverName.next = server;
                        serverName = server;

                        nameServerCount++;
                    }
                    // We should replace the TTL if needed with the one of the ADDITIONAL record so we use
                    // the smallest for caching.
                    serverName.update(parent.newRedirectServerAddress(resolved), r.timeToLive());

                    // Cache the server now.
                    cache(serverName, authoritativeCache, parent.executor());
                    return;
                }
                serverName = serverName.next;
            }
        }

        /** 对无 ADDITIONAL 的 NS 尝试从 {@link DnsCache} 解析地址。 */
        void handleWithoutAdditionals(
                DnsNameResolver parent, DnsCache cache, AuthoritativeDnsServerCache authoritativeCache) {
            AuthoritativeNameServer serverName = head;

            while (serverName != null) {
                if (serverName.address == null) {
                    // These will be resolved on the fly if needed.
                    cacheUnresolved(serverName, authoritativeCache, parent.executor());

                    // Try to resolve via cache as we had no ADDITIONAL entry for the server.

                    List<? extends DnsCacheEntry> entries = cache.get(serverName.nsName, null);
                    if (entries != null && !entries.isEmpty()) {
                        InetAddress address = entries.get(0).address();

                        // If address is null we have a resolution failure cached so just use an unresolved address.
                        if (address != null) {
                            serverName.update(parent.newRedirectServerAddress(address));

                            for (int i = 1; i < entries.size(); i++) {
                                address = entries.get(i).address();

                                assert address != null :
                                        "Cache returned a cached failure, should never return anything else";

                                AuthoritativeNameServer server = new AuthoritativeNameServer(serverName);
                                server.next = serverName.next;
                                serverName.next = server;
                                serverName = server;
                                serverName.update(parent.newRedirectServerAddress(address));

                                nameServerCount++;
                            }
                        }
                    }
                }
                serverName = serverName.next;
            }
        }

        private void cacheUnresolved(
                AuthoritativeNameServer server, AuthoritativeDnsServerCache authoritativeCache, EventLoop loop) {
            // We still want to cached the unresolved address
            server.address = InetSocketAddress.createUnresolved(
                    server.nsName, DefaultDnsServerAddressStreamProvider.DNS_PORT);

            // Cache the server now.
            cache(server, authoritativeCache, loop);
        }

        private void cache(AuthoritativeNameServer server, AuthoritativeDnsServerCache cache, EventLoop loop) {
            // 根区 NS 不缓存
            if (server.isRootServer()) {
                return;
            }
            // Bailiwick 检查（RFC 2181 §5.4.1）：仅当 NS 区等于问题名或其子域时才缓存
            if (!server.domainName.equals(questionName) &&
                    !server.domainName.endsWith("." + questionName)) {
                return;
            }
            cache.cache(server.domainName, server.address, server.ttl, loop);
        }

        /**
         * 若列表为空则 {@code true}。
         */
        boolean isEmpty() {
            return nameServerCount == 0;
        }

        /**
         * 构造包含全部 {@link InetSocketAddress} 的 {@link List}。
         */
        List<InetSocketAddress> addressList() {
            List<InetSocketAddress> addressList = new ArrayList<InetSocketAddress>(nameServerCount);

            AuthoritativeNameServer server = head;
            while (server != null) {
                if (server.address != null) {
                    addressList.add(server.address);
                }
                server = server.next;
            }
            return addressList;
        }
    }

    private static final class AuthoritativeNameServer {
        private final int dots;
        private final String domainName;
        final boolean isCopy;
        final String nsName;

        private long ttl;
        private InetSocketAddress address;

        AuthoritativeNameServer next;

        AuthoritativeNameServer(int dots, long ttl, String domainName, String nsName) {
            this.dots = dots;
            this.ttl = ttl;
            this.nsName = nsName;
            this.domainName = domainName;
            isCopy = false;
        }

        AuthoritativeNameServer(AuthoritativeNameServer server) {
            dots = server.dots;
            ttl = server.ttl;
            nsName = server.nsName;
            domainName = server.domainName;
            isCopy = true;
        }

        /**
         * 是否为根（.）nameserver。
         */
        boolean isRootServer() {
            return dots == 1;
        }

        /**
         * 用给定地址与 TTL 更新服务器（取较小 TTL）。
         */
        void update(InetSocketAddress address, long ttl) {
            assert this.address == null || this.address.isUnresolved();
            this.address = address;
            this.ttl = min(this.ttl, ttl);
        }

        void update(InetSocketAddress address) {
            update(address, Long.MAX_VALUE);
        }
    }
}
