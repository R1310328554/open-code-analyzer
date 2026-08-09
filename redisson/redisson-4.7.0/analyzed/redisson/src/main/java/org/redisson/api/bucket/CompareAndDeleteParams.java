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
package org.redisson.api.bucket;

/**
 * {@link CompareAndDeleteArgs} 的默认实现，保存比较条件类型与比较值/摘要。
 * <p>
 * 由静态工厂方法创建，供 {@link org.redisson.api.RBucket} 内部解析。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 值类型
 */
public final class CompareAndDeleteParams<V> implements CompareAndDeleteArgs<V> {

    private final ConditionType conditionType;
    private V value;
    private String digest;

    /** 按值比较条件构造参数对象。 */
    CompareAndDeleteParams(ConditionType conditionType, V object) {
        this.conditionType = conditionType;
        this.value = object;
    }

    /** 按摘要比较条件构造参数对象。 */
    CompareAndDeleteParams(ConditionType conditionType, String digest) {
        this.conditionType = conditionType;
        this.digest = digest;
    }

    /** 返回比较条件类型。 */
    public ConditionType getConditionType() {
        return conditionType;
    }

    /** 返回待比较的对象值。 */
    public V getValue() {
        return value;
    }

    /** 返回待比较的摘要值。 */
    public String getDigest() {
        return digest;
    }
}