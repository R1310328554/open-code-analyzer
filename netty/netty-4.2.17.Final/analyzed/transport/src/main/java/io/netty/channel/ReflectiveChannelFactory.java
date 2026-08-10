/*
 * Copyright 2014 The Netty Project
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

package io.netty.channel;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.lang.reflect.Constructor;

/**
 * 通过反射调用 Channel 无参公共构造函数来实例化 {@link Channel} 的 {@link ChannelFactory}。
 * <p>
 * 构造时缓存 {@link Constructor}；若目标类缺少 public 无参构造，则抛出
 * {@link IllegalArgumentException}。
 * </p>
 */
public class ReflectiveChannelFactory<T extends Channel> implements ChannelFactory<T> {

    /** 目标 Channel 类的无参构造函数 */
    private final Constructor<? extends T> constructor;

    /**
     * 解析并缓存 {@code clazz} 的 public 无参构造函数。
     *
     * @param clazz 须实现 {@link Channel} 且具备 public 无参构造的具体类
     */
    public ReflectiveChannelFactory(Class<? extends T> clazz) {
        ObjectUtil.checkNotNull(clazz, "clazz");
        try {
            this.constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + StringUtil.simpleClassName(clazz) +
                    " does not have a public non-arg constructor", e);
        }
    }

    @Override
    public T newChannel() {
        try {
            return constructor.newInstance();
        } catch (Throwable t) {
            throw new ChannelException("Unable to create Channel from class " + constructor.getDeclaringClass(), t);
        }
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(ReflectiveChannelFactory.class) +
                '(' + StringUtil.simpleClassName(constructor.getDeclaringClass()) + ".class)";
    }
}
