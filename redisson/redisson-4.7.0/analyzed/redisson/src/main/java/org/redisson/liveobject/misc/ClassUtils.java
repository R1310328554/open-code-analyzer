/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright (c) 2006, Paul Speed
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3) Neither the names "Progeeks", "Meta-JB", nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.redisson.liveobject.misc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.redisson.api.RLiveObject;
import org.redisson.cache.LRUCacheMap;

/**
 * Live Object 反射辅助：字段读写、注解查找、方法匹配与类层次遍历。
 * <p>
 * 字段与注解结果带 LRU 缓存；实现 {@link RLiveObject} 的类不向上遍历父类层次。
 *
 * @author Rui Gu (https://github.com/jackygurui) Modified
 */
public class ClassUtils {
    
    /** 按名称反射写入字段（必要时 setAccessible）。 */
    public static void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = getDeclaredField(obj.getClass(), fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(obj, value);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    /** 读取指定字段上的注解，字段不存在时返回 null。 */
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, String fieldName, Class<T> annotationClass) {
        try {
            Field field = getDeclaredField(clazz, fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.getAnnotation(annotationClass);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /** 在类层次上查找第一个出现的类级注解。 */
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClass) {
        for (Class<?> c : getClassHierarchy(clazz)) {
            if (c.getAnnotation(annotationClass) != null) {
                return c.getAnnotation(annotationClass);
            }
        }
        return null;
    }

    /** 按名称反射读取字段值。 */
    public static <T> T getField(Object obj, String fieldName) {
        try {
            Field field = getDeclaredField(obj.getClass(), fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return (T) field.get(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** 字段缓存哨兵：表示已确认不存在该字段。 */
    private static final Object NO_FIELD = new Object();
    /** 类名:字段名 → Field 或 NO_FIELD 的 LRU 缓存。 */
    private static final Map<String, Object> FIELD_CACHE = new LRUCacheMap<>(1000, 0, 0);

    /** 在类层次中查找声明字段，结果缓存以避免重复反射。 */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Object field = FIELD_CACHE.get(clazz.getName() + ":" + fieldName);
        if (field == null) {
            for (Class<?> c : getClassHierarchy(clazz)) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.getName().equals(fieldName)) {
                        FIELD_CACHE.put(clazz.getName() + ":" + fieldName, f);
                        return f;
                    }
                }
            }
        }
        if (field instanceof Field) {
            return (Field) field;
        }
        if (field == null) {
            FIELD_CACHE.put(clazz.getName() + ":" + fieldName, NO_FIELD);
        }
        throw new NoSuchFieldException("No such field: " + fieldName);
    }
    
    /** 类 → 是否带有某注解的缓存（由 isAnnotationPresent 使用）。 */
    private static final Map<Class<?>, Boolean> ANNOTATED_CLASSES = new LRUCacheMap<>(500, 0, 0);

    /** 判断类或其父类/接口是否带有指定注解（java.* 包直接返回 false）。 */
    public static boolean isAnnotationPresent(Class<?> clazz, Class<? extends Annotation> annotation) {
        if (clazz.getName().startsWith("java.")) {
            return false;
        }
        
        Boolean isAnnotated = ANNOTATED_CLASSES.get(clazz);
        if (isAnnotated == null) {
            for (Class<?> c : getClassHierarchy(clazz)) {
                if (c.isAnnotationPresent(annotation)) {
                    ANNOTATED_CLASSES.put(clazz, true);
                    return true;
                }
            }
            ANNOTATED_CLASSES.put(clazz, false);
            return false;
        }
        return isAnnotated;
    }

    /** 返回从当前类到 Object 的父类链；RLiveObject 仅返回自身。 */
    private static Iterable<Class<?>> getClassHierarchy(Class<?> clazz) {
        // RLiveObject 代理类不向上遍历父类，避免误读实体注解
        if (Arrays.asList(clazz.getInterfaces()).contains(RLiveObject.class)) {
            return Collections.<Class<?>>singleton(clazz);
        }
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            classes.add(c);
        }
        return classes;
    }
    
    /**
     * 按方法名与参数个数查找兼容的方法（参数类型可更宽）。
     * <p>
     * 先尝试 {@link Class#getMethod}，失败则线性扫描 {@link Class#getMethods()}；
     * 不做完整的 Java 变长/协变 widening 搜索。
     * 
     * @param type param
     * @param name of class
     * @param parms classes
     * 
     * @return Method object
     */
    /** 在 type 上查找名称与参数兼容的 {@link Method}，找不到返回 null。 */
    public static Method searchForMethod(Class<?> type, String name, Class<?>[] parms) {
        try {
            return type.getMethod(name, parms);
        } catch (NoSuchMethodException e) {}
        Method[] methods = type.getMethods();
        for (int i = 0; i < methods.length; i++) {
            // 方法名必须一致
            if (!methods[i].getName().equals(name)) {
                continue;
            }

            Class<?>[] types = methods[i].getParameterTypes();
            // 参数个数必须相同
            if (types.length != parms.length) {
                continue;
            }

            // 逐参数检查类型是否 assignable
            if (areTypesCompatible(types, parms)) {
                return methods[i];
            }
        }
        return null;
    }

    /** 比较形式参数类型与调用方传入类型是否兼容（含基本类型包装转换）。 */
    private static boolean areTypesCompatible(Class<?>[] targets, Class<?>[] sources) {
        if (targets.length != sources.length) {
            return false;
        }

        for (int i = 0; i < targets.length; i++) {
            if (sources[i] == null) {
                continue;
            }

            if (!translateFromPrimitive(targets[i]).isAssignableFrom(sources[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 若参数为基本类型则返回对应包装类，否则原样返回。
     * 
     * @param primitive class
     * @return class
     */
    /** 基本类型 → 包装类型的映射表查找。 */
    private static Class<?> translateFromPrimitive(Class<?> primitive) {
        if (!primitive.isPrimitive()) {
            return primitive;
        }

        if (Boolean.TYPE.equals(primitive)) {
            return Boolean.class;
        }
        if (Character.TYPE.equals(primitive)) {
            return Character.class;
        }
        if (Byte.TYPE.equals(primitive)) {
            return Byte.class;
        }
        if (Short.TYPE.equals(primitive)) {
            return Short.class;
        }
        if (Integer.TYPE.equals(primitive)) {
            return Integer.class;
        }
        if (Long.TYPE.equals(primitive)) {
            return Long.class;
        }
        if (Float.TYPE.equals(primitive)) {
            return Float.class;
        }
        if (Double.TYPE.equals(primitive)) {
            return Double.class;
        }

        throw new RuntimeException("Error translating type:" + primitive);
    }
}
