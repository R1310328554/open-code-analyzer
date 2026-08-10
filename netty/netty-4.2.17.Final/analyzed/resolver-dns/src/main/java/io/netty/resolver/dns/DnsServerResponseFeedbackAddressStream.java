/*
 * Copyright 2022 The Netty Project
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

import java.net.InetSocketAddress;

/**
 * 支持查询反馈的 DNS 服务器地址流。
 * <p>查询成功时上报耗时，失败时上报原因，供 {@link #next()} 优选服务器。</p>
 */
public interface DnsServerResponseFeedbackAddressStream extends DnsServerAddressStream {

    /**
     * 向地址流反馈一次成功查询，以便调整后续 {@link #next()} 的服务器选择。
     * <p>无论 DNS 响应 RCode 如何均会调用。</p>
     *
     * @param address The address returned by {@link #next()} that feedback needs to be applied to
     * @param queryResponseTimeNanos The response time of a query against the given DNS server
     */
    void feedbackSuccess(InetSocketAddress address, long queryResponseTimeNanos);

    /**
     * 向地址流反馈一次失败查询，以便按失败类型调整 {@link #next()} 的服务器选择。
     *
     * @param address The address returned by {@link #next()} that feedback needs to be applied to
     * @param failureCause The reason the DNS query failed, can be used to penalize failures differently
     * @param queryResponseTimeNanos The response time of a query against the given DNS server
     */
    void feedbackFailure(InetSocketAddress address, Throwable failureCause, long queryResponseTimeNanos);
}
