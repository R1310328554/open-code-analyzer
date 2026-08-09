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

import java.util.Map;

/**
 * 带类名缓存的 {@link ClassResolver} 装饰器。
 * <p>
 * 先查缓存，未命中时委托给底层 {@link ClassResolver} 加载并写入缓存。
 */
class CachingClassResolver implements ClassResolver {

    /** 类名到 {@link Class} 的缓存映射。 */
    /** 类名到 {@link Class} 的缓存映射。 */
    private final Map<String, Class<?>> classCache;
    /** 实际执行类加载的委托解析器。 */
    /** 实际执行类加载的委托解析器。 */
    private final ClassResolver delegate;

    /**
      * @param delegate   底层类解析器
      * @param classCache 共享的类名缓存
     */
    CachingClassResolver(ClassResolver delegate, Map<String, Class<?>> classCache) {
        this.delegate = delegate;
        this.classCache = classCache;
    }

    @Override
    public Class<?> resolve(String className) throws ClassNotFoundException {
        // 优先查询缓存
        Class<?> clazz;
        clazz = classCache.get(className);
        if (clazz != null) {
            return clazz;
        }

        // 缓存未命中，委托加载并回填
        clazz = delegate.resolve(className);

        classCache.put(className, clazz);
        return clazz;
    }

}
