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
package org.redisson.codec;

import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.redisson.api.RObject;
import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RObjectField;

/**
 * 实体引用场景下的编解码器提供者接口。
 * <p>
 * 按 {@link REntity}、{@link RObjectField} 注解或 Codec 类型查找/注册
 * 缓存的 {@link Codec} 实例，供 Redisson 实体映射层复用。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public interface ReferenceCodecProvider {

    /**
     * 按 Codec 类型获取已缓存的编解码器实例。
     * 
     * @param <T> the expected codec type.
     * @param codecClass the codec class used to lookup the codec.
     * @return the cached codec instance.
     */
    <T extends Codec> T getCodec(Class<T> codecClass);
    
    /**
     * 根据类上的 {@link REntity} 注解及 {@link Config} 解析并返回编解码器。
     * 
     * @param <T> the expected codec type.
     * @param anno REntity annotation used on the class.
     * @param cls The class that has the REntity annotation.
     * @param config Redisson config object
     * 
     * @return the cached codec instance.
     */
    <T extends Codec> T getCodec(REntity anno, Class<?> cls, Config config);
    
    /**
     * 根据字段级 {@link RObjectField} 注解、实体类、RObject 实现类型与字段名
     * 解析该引用字段应使用的编解码器。
     * 
     * @param <T> the expected codec type.
     * @param <K> the type of the RObject.
     * @param anno RObjectField annotation used on the field.
     * @param cls The class that has the REntity annotation.
     * @param rObjectClass the implementation class of RObject the field is going
     * to be transformed into.
     * @param fieldName the name of the field with this RObjectField annotation.
     * @param config Redisson config object
     * 
     * @return the cached codec instance.
     */
    <T extends Codec, K extends RObject> T getCodec(RObjectField anno, Class<?> cls, Class<K> rObjectClass, String fieldName, Config config);

    /**
     * 按 Codec 类或其父类注册编解码器实例到缓存。
     * 
     * @param <T> the codec type to register.
     * @param codecClass the codec Class to register it can be a super class of 
     * the instance.
     * @param codec the codec instance.
     */
    <T extends Codec> void registerCodec(Class<T> codecClass, T codec);
    
}
