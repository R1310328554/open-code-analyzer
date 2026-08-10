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
package io.netty.channel.pool;

/**
 * Allows to map {@link ChannelPool} implementations to a specific key.
 * <p>将 key 映射到 {@link ChannelPool}，便于按目标（如 host:port）复用连接池。</p>
 *
 * @param <K> the type of the key
 * @param <P> the type of the {@link ChannelPool}
 */
public interface ChannelPoolMap<K, P extends ChannelPool> {
    /**
     * Return the {@link ChannelPool} for the {@code code}. This will never return {@code null},
     * but create a new {@link ChannelPool} if non exists for they requested {@code key}.
     * <p>返回 key 对应的 {@link ChannelPool}；不存在则创建，永不为 {@code null}。</p>
     *
     * Please note that {@code null} keys are not allowed.
     */
    P get(K key);

    /**
     * Returns {@code true} if a {@link ChannelPool} exists for the given {@code key}.
     * <p>是否已有 key 对应的 {@link ChannelPool}。</p>
     *
     * Please note that {@code null} keys are not allowed.
     */
    boolean contains(K key);
}
