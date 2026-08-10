/*
 * Copyright 2018 The Netty Project
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

import static io.netty.resolver.dns.DnsAddressDecoder.decodeAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.util.concurrent.Promise;

/**
 * 将 DNS A/AAAA 记录解析为 {@link InetAddress} 的 {@link DnsResolveContext} 实现。
 * <p>负责地址解码、结果排序、解析缓存与权威服务器缓存的写入，以及搜索域查询前的缓存短路。</p>
 */
final class DnsAddressResolveContext extends DnsResolveContext<InetAddress> {

    /** 成功/失败解析结果缓存。 */
    private final DnsCache resolveCache;
    /** 权威名称服务器地址缓存。 */
    private final AuthoritativeDnsServerCache authoritativeDnsServerCache;
    /** 是否在获得首选地址族的首个结果后立即完成 Promise。 */
    private final boolean completeEarlyIfPossible;

    DnsAddressResolveContext(DnsNameResolver parent, Channel channel,
                             Promise<?> originalPromise, String hostname, DnsRecord[] additionals,
                             DnsServerAddressStream nameServerAddrs, int allowedQueries, DnsCache resolveCache,
                             AuthoritativeDnsServerCache authoritativeDnsServerCache,
                             boolean completeEarlyIfPossible) {
        super(parent, channel, originalPromise, hostname, DnsRecord.CLASS_IN,
              parent.resolveRecordTypes(), additionals, nameServerAddrs, allowedQueries);
        this.resolveCache = resolveCache;
        this.authoritativeDnsServerCache = authoritativeDnsServerCache;
        this.completeEarlyIfPossible = completeEarlyIfPossible;
    }

    @Override
    DnsResolveContext<InetAddress> newResolverContext(DnsNameResolver parent, Channel channel,
                                                      Promise<?> originalPromise,
                                                      String hostname,
                                                      int dnsClass, DnsRecordType[] expectedTypes,
                                                      DnsRecord[] additionals,
                                                      DnsServerAddressStream nameServerAddrs, int allowedQueries) {
        return new DnsAddressResolveContext(parent, channel, originalPromise, hostname, additionals,
                nameServerAddrs, allowedQueries, resolveCache, authoritativeDnsServerCache, completeEarlyIfPossible);
    }

    @Override
    InetAddress convertRecord(DnsRecord record, String hostname, DnsRecord[] additionals, EventLoop eventLoop) {
        return decodeAddress(record, hostname, parent.isDecodeIdn());
    }

    @Override
    List<InetAddress> filterResults(List<InetAddress> unfiltered) {
        Collections.sort(unfiltered, PreferredAddressTypeComparator.comparator(parent.preferredAddressType()));
        return unfiltered;
    }

    @Override
    boolean isCompleteEarly(InetAddress resolved) {
        return completeEarlyIfPossible &&
                DnsNameResolver.addressType(parent.preferredAddressType()) == resolved.getClass();
    }

    @Override
    boolean isDuplicateAllowed() {
        // 不包含重复地址，与 JDK 解析行为一致。
        return false;
    }

    @Override
    void cache(String hostname, DnsRecord[] additionals,
               DnsRecord result, InetAddress convertedResult) {
        resolveCache.cache(hostname, additionals, convertedResult, result.timeToLive(),
                channel().eventLoop());
    }

    @Override
    void cache(String hostname, DnsRecord[] additionals, UnknownHostException cause) {
        resolveCache.cache(hostname, additionals, cause, channel().eventLoop());
    }

    @Override
    void doSearchDomainQuery(String hostname, Promise<List<InetAddress>> nextPromise) {
        // 先查缓存，未命中再走父类搜索域逻辑。
        if (!DnsNameResolver.doResolveAllCached(hostname, additionals, nextPromise, resolveCache,
                parent.searchDomains(), parent.ndots(), parent.resolvedInternetProtocolFamiliesUnsafe())) {
            super.doSearchDomainQuery(hostname, nextPromise);
        }
    }

    @Override
    DnsCache resolveCache() {
        return resolveCache;
    }

    @Override
    AuthoritativeDnsServerCache authoritativeDnsServerCache() {
        return authoritativeDnsServerCache;
    }
}
