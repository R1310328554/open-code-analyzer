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
 * 向量集合相似度查询结果：成员值、相似度分数及附加属性字符串。
 * <p>
 * 由 {@code VSIM ... WITHSCORES ATTRIBS} 等命令的解码器构造，用于
 * {@link org.redisson.api.RVectorSet} 带属性相似度查询。
 *
 * @author seakider
 *
 * @param <V> value type
 */
public class ScoreAttributesEntry<V> {
    /** 相似度分数（距离或相似度，取决于命令语义）。 */
    private final Double score;
    /** 成员值。 */
    private final V value;
    /** 附加属性字符串（通常为 JSON 或键值序列）。 */
    private final String attributes;

    /** 指定分数、成员值与属性构造条目。 */
    public ScoreAttributesEntry(Double score, V value, String attributes) {
        super();
        this.score = score;
        this.attributes = attributes;
        this.value = value;
    }

    /** 返回相似度分数。 */
    public Double getScore() {
        return score;
    }

    /** 返回附加属性字符串。 */
    public String getAttributes() {
        return attributes;
    }

    /** 返回成员值。 */
    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreAttributesEntry<?> that = (ScoreAttributesEntry<?>) o;
        return Objects.equals(score, that.score) && Objects.equals(value, that.value) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, value, attributes);
    }

    @Override
    public String toString() {
        return "ScoreAttributesEntry{" +
                "score=" + score +
                ", value=" + value +
                ", attributes=" + attributes +
                '}';
    }
}
