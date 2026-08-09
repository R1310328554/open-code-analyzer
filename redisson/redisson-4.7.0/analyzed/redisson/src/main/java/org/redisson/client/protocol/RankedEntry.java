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
package org.redisson.client.protocol;

import java.util.Objects;

/**
 * 有序集合排名查询结果：成员在 ZSET 中的名次与分数。
 * <p>
 * 由 {@code ZRANK}/{@code ZREVRANK} 等命令的解码器构造。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class RankedEntry<V> {

    /** 成员分数。 */
    private final Double score;
    /** 从零开始的排名（或逆序排名）。 */
    private final Integer rank;

    /** 指定排名与分数构造条目。 */
    public RankedEntry(Integer rank, Double score) {
        super();
        this.score = score;
        this.rank = rank;
    }

    /** 返回成员分数。 */
    public Double getScore() {
        return score;
    }

    /** 返回成员排名。 */
    public Integer getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankedEntry<?> that = (RankedEntry<?>) o;
        return Objects.equals(score, that.score) && Objects.equals(rank, that.rank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, rank);
    }
}
