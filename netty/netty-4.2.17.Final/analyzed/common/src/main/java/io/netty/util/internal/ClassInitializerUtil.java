/*
 * Copyright 2021 The Netty Project
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
package io.netty.util.internal;

/**
 * Utility which ensures that classes are loaded by the {@link ClassLoader}.
 * <p>在 Netty 启动或模块初始化阶段预加载指定类，避免运行期首次加载时的类加载器竞争或延迟。</p>
 */
public final class ClassInitializerUtil {

    private ClassInitializerUtil() { }

    /**
     * Preload the given classes and so ensure the {@link ClassLoader} has these loaded after this method call.
     * <p>使用 {@code loadingClass} 所在类加载器加载并初始化（{@code initialize=true}）给定类列表。</p>
     *
     * @param loadingClass      the {@link Class} that wants to load the classes.
     * @param classes           the classes to load.
     */
    public static void tryLoadClasses(Class<?> loadingClass, Class<?>... classes) {
        ClassLoader loader = PlatformDependent.getClassLoader(loadingClass);
        for (Class<?> clazz: classes) {
            tryLoadClass(loader, clazz.getName());
        }
    }

    /** 尝试加载并链接类；{@link ClassNotFoundException} 与 {@link SecurityException} 静默忽略。 */
    private static void tryLoadClass(ClassLoader classLoader, String className) {
        try {
            // initialize=true 确保类完成链接与初始化
            // Load the class and also ensure we init it which means its linked etc.
            Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException | SecurityException ignore) {
            // Ignore
        }
    }
}
