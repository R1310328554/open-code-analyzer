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

/**
 * 基于指定 {@link ClassLoader} 的 {@link ClassResolver} 实现。
 */
class ClassLoaderClassResolver implements ClassResolver {

    /** 用于加载类的类加载器。 */
    /** 用于加载类的类加载器。 */
    private final ClassLoader classLoader;

    /**
      * @param classLoader 加载反序列化类时使用的类加载器
     */
    ClassLoaderClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Class<?> resolve(String className) throws ClassNotFoundException {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException ignored) {
            // loadClass 失败时回退到 Class.forName
            return Class.forName(className, false, classLoader);
        }
    }

}
