/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api.bitvector;

import org.redisson.api.RBitVectorStore;

import java.time.Duration;

/**
 * {@link RBitVectorStore#matchAll}、{@link RBitVectorStore#matchAny}、
 * {@link RBitVectorStore#matchNone} 查询的参数构建器。
 * <p>携带必需位掩码及可选迭代调优参数（服务端分批拉取）。
 * <p>通过 {@link #mask(long)} 创建并链式配置 chunkSize/chunkFetchTTL 等。
 *
 * @see MatchExactArgs
 * @author Nikita Koksharov
 */
public interface MatchArgs {

    /**
     * 创建a new {@code MatchArgs} with the given bitmask. The mask selects。
     * which bit positions participate in the query predicate; its precise role
     * depends on which match method it is passed to:
     * <ul>
     *   <li>{@code matchAll(args)} — every set bit of the mask must be set in the vector</li>
     *   <li>{@code matchAny(args)} — at least one set bit of the mask must be set in the vector</li>
     *   <li>{@code matchNone(args)} — no set bit of the mask may be set in the vector</li>
     * </ul>
     *
     * @param value 位掩码
     * @return a new {@code MatchArgs} carrying the mask and default tuning values
     */
    static MatchArgs mask(long value) {
        return new MatchParams(value);
    }

    /**
     * 设置the number of keys fetched per server round-trip during result iteration.。
     *
     * @param value 批次大小（须为正数）
     * @return this builder, for chaining
     */
    MatchArgs chunkSize(int value);

    /**
     * 设置the time-to-live applied to the server-side iteration state created by。
     * the query. This is a safety net: if the caller abandons the iterator without
     * consuming it fully (or the JVM dies mid-iteration), the server-side state
     * will be reclaimed automatically once the TTL expires.
     *
     * @param value 服务端迭代状态 TTL
     * @return this builder, for chaining
     */
    MatchArgs chunkFetchTTL(Duration value);

}