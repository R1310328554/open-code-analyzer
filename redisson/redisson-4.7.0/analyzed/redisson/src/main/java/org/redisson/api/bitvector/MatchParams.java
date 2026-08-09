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

import java.time.Duration;

/**
 * {@link MatchArgs} 的默认实现，封装 matchAll/matchAny/matchNone 查询的掩码及迭代调优参数。
 *
 * @author Nikita Koksharov
 */
public final class MatchParams implements MatchArgs {

    /** 参与查询谓词的位掩码。 */
    long mask;
    /** 每次服务端往返拉取的键数量，默认 10。 */
    int chunkSize = 10;
    /** 服务端迭代状态 TTL，默认 5 分钟。 */
    Duration chunkFetchTTL = Duration.ofMinutes(5);

    /** 以给定掩码创建参数对象。 */
    MatchParams(long mask) {
        this.mask = mask;
    }

    @Override
    public MatchArgs chunkSize(int value) {
        this.chunkSize = value;
        return this;
    }

    @Override
    public MatchArgs chunkFetchTTL(Duration value) {
        this.chunkFetchTTL = value;
        return this;
    }

    /** 返回位掩码。 */
    public long getMask() {
        return mask;
    }

    /** 返回迭代批次大小。 */
    public int getChunkSize() {
        return chunkSize;
    }

    /** 返回服务端迭代状态 TTL。 */
    public Duration getChunkFetchTTL() {
        return chunkFetchTTL;
    }
}
