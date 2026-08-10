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

/**
 * 创建 {@link DnsQueryLifecycleObserver} 实例的工厂。
 * <p>每次新 DNS 查询开始时由 {@link DnsNameResolver} 调用，用于注入日志、指标等观测逻辑。</p>
 */
public interface DnsQueryLifecycleObserverFactory {
    /**
     * 为新查询创建 {@link DnsQueryLifecycleObserver} 实例。
     * @param question The question being asked.
     * @return a new instance of a {@link DnsQueryLifecycleObserver}.
     */
    DnsQueryLifecycleObserver newDnsQueryLifecycleObserver(DnsQuestion question);
}
