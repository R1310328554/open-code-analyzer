/*
 * Copyright 2024 The Netty Project
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

/**
 * 控制 DNS 查询期间 {@link io.netty.channel.Channel} 使用方式的策略。
 * <p>影响 UDP 查询是否复用同一数据报通道，进而影响源端口随机化与性能/健壮性权衡。</p>
 */
public enum DnsNameResolverChannelStrategy {
    /**
     * 单个 {@link DnsNameResolver} 实例的所有查询复用同一底层 {@link io.netty.channel.Channel}。
     * <p>
     * As the same {@link io.netty.channel.Channel} is used for all queries we will also use the same source port
     * for all of these. To minimize the risk of spoofing integrators should ideally use multiple resolvers randomly,
     * so that there is source port randomization following the recommendations of
     * <a href="https://www.rfc-editor.org/rfc/rfc5452#section-9.2">RFC5452 Section 9.2</a>.
     * <p>性能较好但源端口固定；建议部署多个解析器实例以实现 RFC5452 推荐的端口随机化。</p>
     */
    ChannelPerResolver,
    /**
     * 每次解析或显式查询使用新的 {@link io.netty.channel.Channel}（类似 JDK {@link io.netty.resolver.DefaultNameResolver}）。
     * <p>
     * As we will need to open and close a new socket for each resolution it will come with a performance overhead.
     * That said using this strategy should be the most robust and also guard against problems that can arise in
     * kubernetes (or similar) setups.
     * <p>每次开闭套接字有性能开销，但在 Kubernetes 等环境中更健壮。</p>
     */
    ChannelPerResolution
}
