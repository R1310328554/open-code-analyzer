/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.utils;

import org.springframework.core.ResolvableType;

import java.util.Objects;

/**
 * 类与泛型解析工具类，基于 Spring {@link ResolvableType} 与反射。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public final class ClassUtils {
    
    /** 解析声明类父类上的第一个泛型参数类型。 */
    public static <T> Class<T> resolveGenericType(Class<?> declaredClass) {
        return (Class<T>) ResolvableType.forClass(declaredClass).getSuperType().resolveGeneric(0);
    }
    
    /** 解析声明类第一个接口上的第一个泛型参数类型。 */
    public static <T> Class<T> resolveGenericTypeByInterface(Class<?> declaredClass) {
        return (Class<T>) ResolvableType.forClass(declaredClass).getInterfaces()[0]
            .resolveGeneric(0);
    }
    
    /** 按全限定类名加载 Class，失败时抛出 {@link RuntimeException}。 */
    public static Class findClassByName(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new RuntimeException("this class name not found");
        }
    }
    
    /** 返回对象的 {@link Class#getName()}。 */
    public static String getName(Object obj) {
        Objects.requireNonNull(obj, "obj");
        return obj.getClass().getName();
    }
    
    /** 返回对象的 {@link Class#getCanonicalName()}。 */
    public static String getCanonicalName(Object obj) {
        Objects.requireNonNull(obj, "obj");
        return obj.getClass().getCanonicalName();
    }
    
    /** 返回对象的 {@link Class#getSimpleName()}（方法名拼写保持与历史一致）。 */
    public static String getSimplaName(Object obj) {
        Objects.requireNonNull(obj, "obj");
        return obj.getClass().getSimpleName();
    }
    
    /** 返回给定 Class 的 {@link Class#getName()}。 */
    public static String getName(Class cls) {
        Objects.requireNonNull(cls, "cls");
        return cls.getName();
    }
    
    /** 返回给定 Class 的 {@link Class#getCanonicalName()}。 */
    public static String getCanonicalName(Class cls) {
        Objects.requireNonNull(cls, "cls");
        return cls.getCanonicalName();
    }
    
    /** 返回给定 Class 的 {@link Class#getSimpleName()}。 */
    public static String getSimplaName(Class cls) {
        Objects.requireNonNull(cls, "cls");
        return cls.getSimpleName();
    }
    
}
