package org.keycloak.testframework.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 测试框架内部使用的集合合并工具。
 * <p>
 * 在首个集合为 {@code null} 时创建可变副本，否则就地追加元素。
 */
public class Collections {

    private Collections() {
    }

    /**
     * 合并两个列表；{@code l1} 为 {@code null} 时返回 {@code l2} 的可变副本。
     *
     * @param l1 目标列表，可为 {@code null}
     * @param l2 要追加的元素
     * @return 合并后的列表
     */
    public static <T> List<T> combine(List<T> l1, List<T> l2) {
        if (l1 == null) {
            return new LinkedList<>(l2);
        } else {
            l1.addAll(l2);
            return l1;
        }
    }

    /**
     * 将可变参数元素合并到列表。
     *
     * @param l1 目标列表
     * @param items 要追加的元素
     * @return 合并后的列表
     */
    @SafeVarargs
    public static <T> List<T> combine(List<T> l1, T... items) {
        return combine(l1, Arrays.asList(items));
    }

    /** 将流中的元素合并到列表。 */
    public static <T> List<T> combine(List<T> l1, Stream<T> items) {
        return combine(l1, items.toList());
    }


    /**
     * 合并两个集合；{@code s1} 为 {@code null} 时返回 {@code s2} 的可变副本。
     *
     * @param s1 目标集合
     * @param s2 要追加的元素
     * @return 合并后的集合
     */
    public static <T> Set<T> combine(Set<T> s1, Set<T> s2) {
        if (s1 == null) {
            return new HashSet<>(s2);
        } else {
            s1.addAll(s2);
            return s1;
        }
    }

    /** 将可变参数元素合并到集合。 */
    @SafeVarargs
    public static <T> Set<T> combine(Set<T> s1, T... items) {
        return combine(s1, Set.of(items));
    }

    /** 将流中的元素合并到集合。 */
    public static <T> Set<T> combine(Set<T> s1, Stream<T> items) {
        return combine(s1, items.collect(Collectors.toSet()));
    }


    /**
     * 按键合并两个「键 → 列表」映射，同键列表通过 {@link #combine(List, List)} 拼接。
     *
     * @param m1 目标映射，可为 {@code null}
     * @param m2 要合并的映射
     * @return 合并后的映射
     */
    public static <K, V> Map<K, List<V>> combine(Map<K, List<V>> m1, Map<K, List<V>> m2) {
        if (m1 == null) {
            m1 = new HashMap<>();
        }
        for (Map.Entry<K, List<V>> entry : m2.entrySet()) {
            K k = entry.getKey();
            List<V> v = entry.getValue();
            m1.put(k, combine(m1.get(k), v));
        }
        return m1;
    }

    /** 向映射的单个键追加多个值。 */
    @SafeVarargs
    public static <K, V> Map<K, List<V>> combine(Map<K, List<V>> m1, K key, V... values) {
        return combine(m1, Map.of(key, List.of(values)));
    }

    /** 向映射的单个键追加流中的值。 */
    public static <K, V> Map<K, List<V>> combine(Map<K, List<V>> m1, K key, Stream<V> values) {
        return combine(m1, Map.of(key, values.toList()));
    }

}
