/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 多值映射：每个键对应一个 {@link List}，常用于 HTTP 头、表单参数等。
 *
 * <p>扩展 {@link Map}{@code <K, List<V>>}，提供追加、取首值等便捷默认方法。</p>
 */
public interface MultivaluedMap<K, V> extends Map<K, List<V>> {

    /** 用单元素列表覆盖该键。 */
    public default void putSingle(K key, V value) {
        List<V> list = createListInstance();
        list.add(value);
        put(key, list); // Just override with new List instance
    }

    /** 向该键追加多个值。 */
    public default void addAll(K key, V... newValues) {
        for (V value : newValues) {
            add(key, value);
        }
    }

    /** 向该键追加列表中的全部值。 */
    public default void addAll(K key, List<V> valueList) {
        for (V value : valueList) {
            add(key, value);
        }
    }

    /** 在该键列表首部插入一个值。 */
    public default void addFirst(K key, V value) {
        getList(key).add(0, value);
    }

    /** 向该键列表末尾追加一个值。 */
    public default void add(K key, V value) {
        getList(key).add(value);
    }

    public default void addMultiple(K key, Collection<V> values) {
        getList(key).addAll(values);
    }

    /** 返回该键的第一个值，无值或空列表时返回 null。 */
    public default V getFirst(K key) {
        return Optional.ofNullable(get(key)).filter(l -> !l.isEmpty()).map(l -> l.get(0)).orElse(null);
    }

    default V getFirstOrDefault(K key, V defaultValue) {
        return Optional.ofNullable(getFirst(key)).orElse(defaultValue);
    }

    public default List<V> getList(K key) {
        return compute(key, (k, v) -> v != null ? v : createListInstance());
    }

    /** 合并另一个多值映射：同键的值列表追加而非覆盖。 */
    public default void addAll(MultivaluedMap<K, V> other) {
        for (Entry<K, List<V>> entry : other.entrySet()) {
            getList(entry.getKey()).addAll(entry.getValue());
        }
    }

    /** 比较键集与各键下值列表（忽略列表内元素顺序）。 */
    public default boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> omap) {
       if (this == omap) {
          return true;
       }
       if (!keySet().equals(omap.keySet())) {
          return false;
       }
       for (Map.Entry<K, List<V>> e : entrySet()) {
           List<V> list = e.getValue();
           List<V> olist = omap.get(e.getKey());
           if (!CollectionUtil.collectionEquals(list, olist)) {
               return false;
           }
       }
       return true;
    }

    public default List<V> createListInstance() {
        return new ArrayList<>();
    }

}
