package com.taobao.arthas.common;


import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * 通过反射获取 {@link sun.misc.Unsafe} 与 {@code MethodHandles.Lookup.IMPL_LOOKUP}，
 * 供 {@link ReflectUtils#defineClass} 等在模块限制下动态定义类。
 *
 * @author hengyunabc 2023-09-21
 */
public class UnsafeUtils {
    /** 全局 Unsafe 实例，获取失败时为 null */
    public static final Unsafe UNSAFE;
    /** JDK 内部特权 Lookup，用于 unreflect 受保护方法 */
    private static MethodHandles.Lookup IMPL_LOOKUP;

    static {
        Unsafe unsafe = null;
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = (Unsafe) theUnsafeField.get(null);
        } catch (Throwable ignored) {
            // ignored
        }
        UNSAFE = unsafe;
    }

    /**
     * 返回 {@code IMPL_LOOKUP} 单例；首次调用时通过 Unsafe 读取静态字段。
     */
    public static MethodHandles.Lookup implLookup() {
        if (IMPL_LOOKUP == null) {
            Class<MethodHandles.Lookup> lookupClass = MethodHandles.Lookup.class;

            try {
                Field implLookupField = lookupClass.getDeclaredField("IMPL_LOOKUP");
                long offset = UNSAFE.staticFieldOffset(implLookupField);
                IMPL_LOOKUP = (MethodHandles.Lookup) UNSAFE.getObject(UNSAFE.staticFieldBase(implLookupField), offset);
            } catch (Throwable e) {
                // ignored
            }
        }
        return IMPL_LOOKUP;
    }
}
