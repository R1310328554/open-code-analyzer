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
package org.apache.rocketmq.common.utils;

import org.apache.commons.lang3.SerializationException;

/**
 * 对象与字节数组互转的序列化 SPI。
 */
public interface Serializer {

    /**
     * 将对象序列化为 byte[]。
     *
     * @param t 待序列化对象
     */
    <T> byte[] serialize(T t) throws SerializationException;

    /**
     * 将字节数组反序列化为指定类型实例。
     *
     * @param bytes 序列化数据
     * @param type  目标类型
     */
    <T> T deserialize(byte[] bytes, Class<T> type) throws SerializationException;
}
