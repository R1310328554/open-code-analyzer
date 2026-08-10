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
package io.netty.resolver.dns;

import io.netty.handler.codec.dns.DnsQuestion;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 将两个 {@link DnsQueryLifecycleObserverFactory} 组合为单一工厂。
 * <p>每次 DNS 查询会为两个子工厂各创建观察者，并包装为 {@link BiDnsQueryLifecycleObserver}。</p>
 */
public final class BiDnsQueryLifecycleObserverFactory implements DnsQueryLifecycleObserverFactory {
    /** 优先创建观察者的工厂。 */
    private final DnsQueryLifecycleObserverFactory a;
    /** 第二个观察者对应的工厂。 */
    private final DnsQueryLifecycleObserverFactory b;

    /**
     * 创建组合工厂实例。
     * @param a The {@link DnsQueryLifecycleObserverFactory} that will receive events first.
     * @param b The {@link DnsQueryLifecycleObserverFactory} that will receive events second.
     */
    public BiDnsQueryLifecycleObserverFactory(DnsQueryLifecycleObserverFactory a, DnsQueryLifecycleObserverFactory b) {
        this.a = checkNotNull(a, "a");
        this.b = checkNotNull(b, "b");
    }

    @Override
    public DnsQueryLifecycleObserver newDnsQueryLifecycleObserver(DnsQuestion question) {
        return new BiDnsQueryLifecycleObserver(a.newDnsQueryLifecycleObserver(question),
                                               b.newDnsQueryLifecycleObserver(question));
    }
}
