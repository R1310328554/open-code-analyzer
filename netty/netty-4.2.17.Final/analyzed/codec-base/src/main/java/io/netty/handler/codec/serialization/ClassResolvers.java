/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.serialization;

import io.netty.util.internal.PlatformDependent;

import java.lang.ref.Reference;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建 {@link ClassResolver} 实例的工厂方法集合。
 * <p>
 * <strong>安全提示：</strong>Java 序列化存在安全风险，使用前应通过
 * {@code jdk.serialFilter} 等机制限制允许反序列化的类。
 * 详见 <a href="https://docs.oracle.com/en/java/javase/17/core/serialization-filtering1.html">
 * serialization filtering</a>。
 *
 * @deprecated 因序列化存在安全风险，本类已弃用且无替代方案
 */
@Deprecated
public final class ClassResolvers {

    /**
     * 创建禁用缓存的解析器。
     *
      * @param classLoader 指定类加载器；为 {@code null} 时使用默认类加载器
     * @return 新的 {@link ClassResolver} 实例
     */
    public static ClassResolver cacheDisabled(ClassLoader classLoader) {
        return new ClassLoaderClassResolver(defaultClassLoader(classLoader));
    }

    /**
     * 创建非激进、非并发的弱引用缓存解析器。
     * <p>
     * 适用于非共享的默认缓存场景。
     *
      * @param classLoader 指定类加载器；为 {@code null} 时使用默认类加载器
     * @return 新的 {@link ClassResolver} 实例
     */
    public static ClassResolver weakCachingResolver(ClassLoader classLoader) {
        return new CachingClassResolver(
                new ClassLoaderClassResolver(defaultClassLoader(classLoader)),
                new WeakReferenceMap<String, Class<?>>(new HashMap<String, Reference<Class<?>>>()));
    }

    /**
     * 创建非激进、非并发的软引用缓存解析器。
     * <p>
     * 适用于非共享缓存且不担心类卸载的场景。
     *
      * @param classLoader 指定类加载器；为 {@code null} 时使用默认类加载器
     * @return 新的 {@link ClassResolver} 实例
     */
    public static ClassResolver softCachingResolver(ClassLoader classLoader) {
        return new CachingClassResolver(
                new ClassLoaderClassResolver(defaultClassLoader(classLoader)),
                new SoftReferenceMap<String, Class<?>>(new HashMap<String, Reference<Class<?>>>()));
    }

    /**
     * 创建非激进、并发的弱引用缓存解析器。
     * <p>
     * 适用于共享缓存且需考虑类卸载的场景。
     *
      * @param classLoader 指定类加载器；为 {@code null} 时使用默认类加载器
     * @return 新的 {@link ClassResolver} 实例
     */
    public static ClassResolver weakCachingConcurrentResolver(ClassLoader classLoader) {
        return new CachingClassResolver(
                new ClassLoaderClassResolver(defaultClassLoader(classLoader)),
                new WeakReferenceMap<String, Class<?>>(new ConcurrentHashMap<>()));
    }

    /**
     * 创建激进、并发的软引用缓存解析器。
     * <p>
     * 适用于共享缓存且不担心类卸载的场景。
     *
      * @param classLoader 指定类加载器；为 {@code null} 时使用默认类加载器
     * @return 新的 {@link ClassResolver} 实例
     */
    public static ClassResolver softCachingConcurrentResolver(ClassLoader classLoader) {
        return new CachingClassResolver(
                new ClassLoaderClassResolver(defaultClassLoader(classLoader)),
                new SoftReferenceMap<String, Class<?>>(new ConcurrentHashMap<>()));
    }

    /** 解析实际使用的类加载器：优先入参，其次线程上下文类加载器，最后本类所在加载器。 */
    /** 解析实际使用的类加载器：优先入参，其次线程上下文类加载器，最后本类所在加载器。 */
    static ClassLoader defaultClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            return classLoader;
        }

        final ClassLoader contextClassLoader = PlatformDependent.getContextClassLoader();
        if (contextClassLoader != null) {
            return contextClassLoader;
        }

        return PlatformDependent.getClassLoader(ClassResolvers.class);
    }

    private ClassResolvers() {
        // Unused
    }
}
