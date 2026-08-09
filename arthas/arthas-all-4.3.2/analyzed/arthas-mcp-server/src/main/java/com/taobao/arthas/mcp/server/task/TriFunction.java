/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

/**
 * 三参数函数式接口，用于 Task 感知工具的 callHandler 等场景。
 *
 * @param <T> 第一个参数类型
 * @param <U> 第二个参数类型
 * @param <V> 第三个参数类型
 * @param <R> 返回值类型
 * @author Yeaury
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);
}
