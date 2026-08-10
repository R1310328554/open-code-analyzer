package org.keycloak.common.util;

/**
 * 接受三个参数且不含返回值的操作接口（三元 {@link java.util.function.Consumer}）。
 *
 * @param <T> 第一个参数类型
 * @param <U> 第二个参数类型
 * @param <V> 第三个参数类型
 */
public interface TriConsumer<T, U, V> {

    /**
     * 对给定参数执行操作。
     *
     * @param t 第一个参数
     * @param u 第二个参数
     * @param v 第三个参数
     */
    void accept(T t, U u, V v);
}
