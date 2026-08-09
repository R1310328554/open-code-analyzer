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
package org.redisson.api.bloomfilter;

import java.util.Collection;

/**
 * {@code BF.INSERT} 命令插入参数构建器入口；以待插入元素集合开始链式配置。
 *
 * @author Su Ko
 * @param <V> 元素类型
 */
public interface BloomFilterInsertArgs<V> {

    /**
     * 以给定元素集合创建 {@code BF.INSERT} 参数构建器。
     *
     * @param elements 待插入元素集合
     * @return 可继续配置容量、误判率等选项的构建器
     */
    static <V> OptionalBloomFilterInsertArgs<V> elements(Collection<V> elements) {
        return new BloomFilterInsertParams<>(elements);
    }
}
