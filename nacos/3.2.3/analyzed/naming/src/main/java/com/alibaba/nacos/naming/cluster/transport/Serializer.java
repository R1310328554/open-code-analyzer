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

package com.alibaba.nacos.naming.cluster.transport;

/**
 * 命名模块大数据量序列化接口。
 *
 * <p>主要用于 Distro 同步、客户端快照等场景的字节编解码。</p>
 *
 * @author nkorange
 * @since 1.0.0
 */
public interface Serializer {
    
    /**
     * 按约定协议将对象序列化为字节数组。
     *
     * @param data 待序列化对象
     * @param <T>  对象类型
     * @return 序列化后的字节数组
     */
    <T> byte[] serialize(T data);
    
    /**
     * 将字节数组反序列化为目标类型实例。
     *
     * @param data  待反序列化字节
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 反序列化结果
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
}
