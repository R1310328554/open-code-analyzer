/*
 * Copyright 2015 The Netty Project
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

import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;

import java.net.InetSocketAddress;

/**
 * {@link DnsNameResolver} 查询失败时抛出的 {@link RuntimeException}。
 * <p>携带远程 DNS 服务器地址与失败的 {@link DnsQuestion}，便于定位具体查询。</p>
 */
public class DnsNameResolverException extends RuntimeException {

    private static final long serialVersionUID = -8826717909627131850L;

    /** 失败查询所发往的 DNS 服务器地址。 */
    private final InetSocketAddress remoteAddress;
    /** 失败的 DNS 问题（类型与域名）。 */
    private final DnsQuestion question;

    public DnsNameResolverException(InetSocketAddress remoteAddress, DnsQuestion question, String message) {
        super(message);
        this.remoteAddress = validateRemoteAddress(remoteAddress);
        this.question = validateQuestion(question);
    }

    public DnsNameResolverException(
            InetSocketAddress remoteAddress, DnsQuestion question, String message, Throwable cause) {
        super(message, cause);
        this.remoteAddress = validateRemoteAddress(remoteAddress);
        this.question = validateQuestion(question);
    }

    private static InetSocketAddress validateRemoteAddress(InetSocketAddress remoteAddress) {
        return ObjectUtil.checkNotNull(remoteAddress, "remoteAddress");
    }

    private static DnsQuestion validateQuestion(DnsQuestion question) {
        return ObjectUtil.checkNotNull(question, "question");
    }

    /**
     * 返回失败 DNS 查询的目标 {@link InetSocketAddress}。
     */
    public InetSocketAddress remoteAddress() {
        return remoteAddress;
    }

    /**
     * 返回失败 DNS 查询的 {@link DnsQuestion}。
     */
    public DnsQuestion question() {
        return question;
    }

    // 不填充堆栈以降低分配开销；方法无需同步
    // Suppress a warning since the method doesn't need synchronization
    @Override
    public Throwable fillInStackTrace() {
        setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        return this;
    }
}
