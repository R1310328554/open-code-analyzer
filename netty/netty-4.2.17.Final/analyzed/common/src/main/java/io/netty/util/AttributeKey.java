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
package io.netty.util;

/**
 * Key which can be used to access {@link Attribute} out of the {@link AttributeMap}. Be aware that it is not be
 * possible to have multiple keys with the same name.
 *
 * <p>访问 {@link AttributeMap} 中属性的键，由 {@link ConstantPool} 按名称去重。
 * 同名 {@code valueOf} 始终返回同一实例，可用 {@code ==} 比较。</p>
 *
 * @param <T>   the type of the {@link Attribute} which can be accessed via this {@link AttributeKey}.
 */
@SuppressWarnings("UnusedDeclaration") // 'T' is used only at compile time
public final class AttributeKey<T> extends AbstractConstant<AttributeKey<T>> {

    /** 全局 AttributeKey 常量池。 */
    private static final ConstantPool<AttributeKey<Object>> pool = new ConstantPool<AttributeKey<Object>>() {
        @Override
        protected AttributeKey<Object> newConstant(int id, String name) {
            return new AttributeKey<Object>(id, name);
        }
    };

    /**
     * Returns the singleton instance of the {@link AttributeKey} which has the specified {@code name}.
     *
     * <p>按名称获取或创建单例键；Channel、Handler 上下文等广泛使用。</p>
     */
    @SuppressWarnings("unchecked")
    public static <T> AttributeKey<T> valueOf(String name) {
        return (AttributeKey<T>) pool.valueOf(name);
    }

    /**
     * Returns {@code true} if a {@link AttributeKey} exists for the given {@code name}.
     *
     * <p>池中是否已注册该名称。</p>
     */
    public static boolean exists(String name) {
        return pool.exists(name);
    }

    /**
     * Creates a new {@link AttributeKey} for the given {@code name} or fail with an
     * {@link IllegalArgumentException} if a {@link AttributeKey} for the given {@code name} exists.
     *
     * <p>强制新建；名称已存在则抛异常，用于避免与已有键冲突。</p>
     */
    @SuppressWarnings("unchecked")
    public static <T> AttributeKey<T> newInstance(String name) {
        return (AttributeKey<T>) pool.newInstance(name);
    }

    /**
     * 以 {@code 类名 + '#' + secondNameComponent} 形式构造唯一键名。
     */
    @SuppressWarnings("unchecked")
    public static <T> AttributeKey<T> valueOf(Class<?> firstNameComponent, String secondNameComponent) {
        return (AttributeKey<T>) pool.valueOf(firstNameComponent, secondNameComponent);
    }

    private AttributeKey(int id, String name) {
        super(id, name);
    }
}
