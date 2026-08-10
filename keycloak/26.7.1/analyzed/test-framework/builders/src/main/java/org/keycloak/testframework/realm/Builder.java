package org.keycloak.testframework.realm;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 测试框架领域表示构建器的抽象基类，提供 {@link #build()} 及列表/映射/集合合并辅助方法。
 *
 * @param <T> 被构建的 IDM 表示类型
 */
public abstract class Builder<T> {

    /** 底层表示对象。 */
    T rep;

    /** @param rep 被包装的表示实例 */
    public Builder(T rep) {
        this.rep = rep;
    }

    /** 返回已组装的表示对象。 */
    public T build() {
        return rep;
    }

    /** 值非 null 时返回原值，否则调用 supplier 创建默认值。 */
    protected static <T> T createIfNull(T value, Supplier<T> supplier) {
        return value != null ? value : supplier.get();
    }

    /** 合并两个列表；第一个为 null 时返回第二个的副本。 */
    static <T> List<T> combine(List<T> l1, List<T> l2) {
        if (l1 == null) {
            return new LinkedList<>(l2);
        } else {
            l1.addAll(l2);
            return l1;
        }
    }

    /** 合并两个数组；第一个为 null 时返回第二个。 */
    @SuppressWarnings("unchecked")
    static <T> T[] combine(T[] l1, T[] l2) {
        if (l1 == null) {
            return l2;
        }
        T[] combined = (T[]) Array.newInstance(l1[0].getClass(), l1.length + l2.length);
        System.arraycopy(l1, 0, combined, 0, l1.length);
        System.arraycopy(l2, 0, combined, l1.length, l2.length);
        return combined;
    }

    /** 将可变参数元素追加到列表。 */
    @SafeVarargs
    static <T> List<T> combine(List<T> l1, T... items) {
        return combine(l1, Arrays.asList(items));
    }

    /** 将 Builder 数组的元素 build 后追加到列表。 */
    @SafeVarargs
    static <T> List<T> combine(List<T> l1, Builder<T>... items) {
        return combine(l1, Arrays.stream(items).map(Builder::build).toList());
    }

    /** 通过 mapper 将参数转为 Builder 再 build，合并到列表。 */
    @SafeVarargs
    static <T, P> List<T> combine(Function<P, Builder<T>> mapper, List<T> l1, P... l2) {
        return combine(l1, Arrays.stream(l2).map(mapper).map(Builder::build).toList());
    }

    /** 在映射的指定键下合并由 mapper 构建的值列表。 */
    @SafeVarargs
    static <V, P, K> Map<K, List<V>> combine(Function<P, Builder<V>> mapper, Map<K, List<V>> m1, K key, P... values) {
        return combine(m1, key, Arrays.stream(values).map(mapper).map(Builder::build).toList());
    }

    /** 合并两个「键 → 值列表」映射。 */
    static <K, V> Map<K, List<V>> combine(Map<K, List<V>> m1, Map<K, List<V>> m2) {
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

    /** 合并两个简单键值映射（后者覆盖同键）。 */
    static <K, V> Map<K, V> combineMap(Map<K, V> m1, Map<K, V> m2) {
        if (m1 == null) {
            return m2;
        }
        m1.putAll(m2);
        return m1;
    }

    /** 合并两个集合；第一个为 null 时返回第二个的副本。 */
    static <T> Set<T> combine(Set<T> s1, Set<T> s2) {
        if (s1 == null) {
            return new HashSet<>(s2);
        } else {
            s1.addAll(s2);
            return s1;
        }
    }

    /** 将可变参数元素追加到集合。 */
    @SafeVarargs
    static <T> Set<T> combine(Set<T> s1, T... items) {
        return combine(s1, Set.of(items));
    }

    /** 在映射指定键下合并可变参数值列表。 */
    @SafeVarargs
    static <K, V> Map<K, List<V>> combine(Map<K, List<V>> m1, K key, V... values) {
        return combine(m1, Map.of(key, List.of(values)));
    }

    /** 在映射指定键下合并给定值列表。 */
    static <K, V> Map<K, List<V>> combine(Map<K, List<V>> m1, K key, List<V> values) {
        return combine(m1, Map.of(key, values));
    }

    /** 在映射指定键下合并 Builder 数组 build 后的值列表。 */
    @SafeVarargs
    static <K, V> Map<K, List<V>> combine(Map<K, List<V>> m1, K key, Builder<V>... values) {
        return combine(m1, Map.of(key, Arrays.stream(values).map(Builder::build).toList()));
    }

    /** 从映射中移除指定键。 */
    @SafeVarargs
    static <K, V> Map<K, V> removeKeys(Map<K, V> map, K... keys) {
        if (map != null) {
            for (K key : keys) {
                map.remove(key);
            }
        }
        return map;
    }

    /** 从列表中移除指定值。 */
    @SafeVarargs
    static <V> List<V> removeValues(List<V> list, V... values) {
        if (list != null) {
            list.removeAll(Arrays.stream(values).toList());
        }
        return list;
    }

}
