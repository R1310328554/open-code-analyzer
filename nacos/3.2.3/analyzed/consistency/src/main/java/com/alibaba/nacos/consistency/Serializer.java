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

package com.alibaba.nacos.consistency;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化抽象接口：定义对象与字节数组之间的双向转换，供一致性协议与 RPC 复用。
 * 内置 {@link #CLASS_CACHE} 缓存类全名到 Class 的映射，避免重复反射加载。
 *
 * Serialization interface.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface Serializer {
    
    /** 类全名到 {@link Class} 的并发缓存，供 {@link #deserialize(byte[], String)} 使用。 */
    Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>(8);
    
    /**
     * 将字节数组反序列化为对象（实现类需自行推断类型）。
     * Deserialize the data.
     *
     * @param data byte[]
     * @param <T>  class type
     * @return target object instance
     */
    <T> T deserialize(byte[] data);
    
    /**
     * 按指定 {@link Class} 将字节数组反序列化为目标类型实例。
     * Deserialize the data.
     *
     * @param data byte[]
     * @param cls  class
     * @param <T>  class type
     * @return target object instance
     */
    <T> T deserialize(byte[] data, Class<T> cls);
    
    /**
     * 按 {@link Type}（含泛型）将字节数组反序列化为目标实例。
     * Deserialize the data.
     *
     * @param data byte[]
     * @param type data type
     * @param <T>  class type
     * @return target object instance
     */
    <T> T deserialize(byte[] data, Type type);
    
    /**
     * 按类全名字符串反序列化；类名经 {@link #CLASS_CACHE} 缓存，失败时返回 null。
     * Deserialize the data.
     *
     * @param data          byte[]
     * @param classFullName class full name
     * @param <T>           class type
     * @return target object instance
     */
    default <T> T deserialize(byte[] data, String classFullName) {
        try {
            Class<?> cls = CLASS_CACHE.computeIfAbsent(classFullName, name -> {
                try {
                    return Class.forName(classFullName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
            return (T) deserialize(data, cls);
        } catch (Exception ignore) {
            return null;
        }
    }
    
    /**
     * 将对象序列化为字节数组。
     * Serialize the object.
     *
     * @param obj target obj
     * @return byte[]
     */
    <T> byte[] serialize(T obj);
    
    /**
     * 返回序列化实现标识名（如 Hessian、JSON），用于 SPI 选择与日志。
     * The name of the serializer implementer.
     *
     * @return name
     */
    String name();
    
}
