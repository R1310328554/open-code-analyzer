package com.taobao.arthas.core.util;

/**
 * 数组相关小工具。
 *
 * @author ralf0131 2016-12-28 14:57.
 */
public class ArrayUtils {

    /**
     * 空的不可变 {@code long} 数组常量。
     */
    public static final long[] EMPTY_LONG_ARRAY = new long[0];

    /**
     * 将 {@code Long[]} 转为 {@code long[]} 基本类型数组。
     *
     * <p>输入为 {@code null} 时返回 {@code null}；空数组返回 {@link #EMPTY_LONG_ARRAY}。</p>
     *
     * @param array 包装类型数组，可为 {@code null}
     * @return 基本类型数组；null 输入则 null
     * @throws NullPointerException 数组元素含 {@code null}
     */
    public static long[] toPrimitive(final Long[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_LONG_ARRAY;
        }
        final long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].longValue();
        }
        return result;
    }
}
