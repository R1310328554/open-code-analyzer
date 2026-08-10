/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 集合操作辅助工具类。
 *
 * @author <a href="mailto:jeroen.rosenberg@gmail.com">Jeroen Rosenberg</a>
 */
public class CollectionUtil {

    /** 以默认分隔符 {@code ", "} 拼接字符串集合。 */
    public static String join(Collection<String> strings) {
        return join(strings, ", ");
    }

    /** 以指定分隔符拼接字符串集合。 */
    public static String join(Collection<String> strings, String separator) {
        return strings.stream().collect(Collectors.joining(String.valueOf(separator)));
    }

    // 判断两个集合是否包含相同元素（忽略顺序，考虑重复次数）
    public static <T> boolean collectionEquals(Collection<T> col1, Collection<T> col2) {
        if (col1.size()!=col2.size()) {
            return false;
        }
        Map<T, Integer> countMap = new HashMap<>();
        for(T o : col1) {
            countMap.merge(o, 1, (v1, v2) -> v1 + v2);
        }
        for(T o : col2) {
            Integer v = countMap.get(o);
            if (v==null) {
                return false;
            }
            if (v == 1) {
                countMap.remove(o);
            } else {
                countMap.put(o, v-1);
            }
        }
        return countMap.isEmpty();
    }

    /** 判断集合是否为 {@code null} 或空。 */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /** 判断集合非空。 */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /** 将集合复制为 {@link HashSet}；{@code null} 输入返回 {@code null}。 */
    public static <T> Set<T> collectionToSet(Collection<T> collection) {
        return collection == null ? null : new HashSet<>(collection);
    }
}
