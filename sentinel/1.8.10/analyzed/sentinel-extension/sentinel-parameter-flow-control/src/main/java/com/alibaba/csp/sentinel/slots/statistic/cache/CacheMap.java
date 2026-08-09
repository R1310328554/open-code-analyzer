/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.statistic.cache;

import java.util.Set;

/**
 * 热点参数统计通用缓存映射接口，支持 LRU 淘汰与有序键遍历。
 *
 * @param <K> type of the key
 * @param <V> type of the value
 * @author Eric Zhao
 * @since 0.2.0
 */
public interface CacheMap<K, V> {

    /** 是否包含指定键。 */
    boolean containsKey(K key);

    /** 按键取值，不存在时返回 null。 */
    V get(K key);

    /** 移除并返回旧值。 */
    V remove(K key);

    /** 写入键值对并返回旧值。 */
    V put(K key, V value);

    /** 键不存在时写入，返回已有值或 null。 */
    V putIfAbsent(K key, V value);

    /** 当前缓存条目数（加权容量）。 */
    long size();

    /** 清空全部条目。 */
    void clear();

    /**
     * 按访问顺序返回键集合。
     * @param ascending true 升序，false 降序
     */
    Set<K> keySet(boolean ascending);
}
