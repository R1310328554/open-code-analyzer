/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.rocketmq.utils;

import io.openmessaging.KeyValue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * JavaBean 属性填充工具：将 {@link KeyValue} 或 {@link Properties} 映射到 setter 方法。
 */
public final class BeanUtils {
    private static final Logger log = LoggerFactory.getLogger(BeanUtils.class);

    /** 基本类型到包装类型的映射表。 */
    private static Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();

    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    private static Map<Class<?>, Class<?>> wrapperMap = new HashMap<>();

    static {
        for (Entry<Class<?>, Class<?>> primitiveClass : primitiveWrapperMap.entrySet()) {
            final Class<?> wrapperClass = primitiveClass.getValue();
            if (!primitiveClass.getKey().equals(wrapperClass)) {
                wrapperMap.put(wrapperClass, primitiveClass.getKey());
            }
        }
        wrapperMap.put(String.class, String.class);
    }

    /**
     * 根据键值对填充 JavaBean 属性，通过反射调用对应 setter。
     * 支持 String、boolean、int、long、float、double 等参数类型。
     *
     * @param clazz 待填充的 JavaBean 类型
     * @param properties 属性名到值的映射
     * @param <T> 目标类型
     * @return 填充后的实例，失败时返回 null
     */
    /** 从 {@link Properties} 创建并填充 JavaBean。 */
    public static <T> T populate(final Properties properties, final Class<T> clazz) {
        T obj = null;
        try {
            obj = clazz.getDeclaredConstructor().newInstance();
            return populate(properties, obj);
        } catch (Throwable e) {
            log.warn("Error occurs !", e);
        }
        return obj;
    }

    /** 从 OMS {@link KeyValue} 创建并填充 JavaBean。 */
    public static <T> T populate(final KeyValue properties, final Class<T> clazz) {
        T obj = null;
        try {
            obj = clazz.getDeclaredConstructor().newInstance();
            return populate(properties, obj);
        } catch (Throwable e) {
            log.warn("Error occurs !", e);
        }
        return obj;
    }

    /** 查找 setter 方法的第一个参数类型。 */
    public static Class<?> getMethodClass(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase(methodName)) {
                return method.getParameterTypes()[0];
            }
        }
        return null;
    }

    /** 按参数类型转换后调用指定 setter。 */
    public static void setProperties(Class<?> clazz, Object obj, String methodName,
        Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> parameterClass = getMethodClass(clazz, methodName);
        Method setterMethod = clazz.getMethod(methodName, parameterClass);
        if (parameterClass == Boolean.TYPE) {
            setterMethod.invoke(obj, Boolean.valueOf(value.toString()));
        } else if (parameterClass == Integer.TYPE) {
            setterMethod.invoke(obj, Integer.valueOf(value.toString()));
        } else if (parameterClass == Double.TYPE) {
            setterMethod.invoke(obj, Double.valueOf(value.toString()));
        } else if (parameterClass == Float.TYPE) {
            setterMethod.invoke(obj, Float.valueOf(value.toString()));
        } else if (parameterClass == Long.TYPE) {
            setterMethod.invoke(obj, Long.valueOf(value.toString()));
        } else
            setterMethod.invoke(obj, value);
    }

    /** 将 Properties 键（支持点分）映射到已有对象的 setter。 */
    public static <T> T populate(final Properties properties, final T obj) {
        Class<?> clazz = obj.getClass();
        try {

            Set<Map.Entry<Object, Object>> entries = properties.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                String entryKey = entry.getKey().toString();
                String[] keyGroup = entryKey.split("\\.");
                for (int i = 0; i < keyGroup.length; i++) {
                    keyGroup[i] = keyGroup[i].toLowerCase();
                    keyGroup[i] = StringUtils.capitalize(keyGroup[i]);
                }
                String beanFieldNameWithCapitalization = StringUtils.join(keyGroup);
                try {
                    setProperties(clazz, obj, "set" + beanFieldNameWithCapitalization, entry.getValue());
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                    // 无对应 setter 时忽略
                }
            }
        } catch (RuntimeException e) {
            log.warn("Error occurs !", e);
        }
        return obj;
    }

    /** 将 KeyValue 键（支持 ._ 分隔）映射到已有对象的 setter。 */
    public static <T> T populate(final KeyValue properties, final T obj) {
        Class<?> clazz = obj.getClass();
        try {

            final Set<String> keySet = properties.keySet();
            for (String key : keySet) {
                String[] keyGroup = key.split("[\\._]");
                for (int i = 0; i < keyGroup.length; i++) {
                    keyGroup[i] = keyGroup[i].toLowerCase();
                    keyGroup[i] = StringUtils.capitalize(keyGroup[i]);
                }
                String beanFieldNameWithCapitalization = StringUtils.join(keyGroup);
                try {
                    setProperties(clazz, obj, "set" + beanFieldNameWithCapitalization, properties.getString(key));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                    //ignored...
                }
            }
        } catch (RuntimeException e) {
            log.warn("Error occurs !", e);
        }
        return obj;
    }
}

