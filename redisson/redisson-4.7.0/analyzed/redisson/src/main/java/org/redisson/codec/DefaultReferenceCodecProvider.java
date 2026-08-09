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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.redisson.api.RObject;
import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RObjectField;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.redisson.liveobject.misc.ClassUtils;

/**
 * {@link ReferenceCodecProvider} 的默认实现：按 Codec 类型缓存单例，并从注解解析编解码器。
 * <p>
 * Live Object 实体 {@link REntity} 与字段 {@link RObjectField} 可通过注解指定 Codec 类，
 * 未指定时使用 {@link Config#getCodec()} 的全局默认编解码器。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public class DefaultReferenceCodecProvider implements ReferenceCodecProvider {

    /** Codec 类型 → 实例缓存，避免重复反射构造。 */
    private final ConcurrentMap<Class<? extends Codec>, Codec> codecCache = new ConcurrentHashMap<>();

    /**
     * 获取指定 Codec 类的单例实例；缓存未命中时通过无参构造器创建。
     *
     * @param codecClass Codec 类型
     * @return Codec 实例
     */
    @Override
    public <T extends Codec> T getCodec(Class<T> codecClass) {
        Codec codec = codecCache.get(codecClass);
        if (codec == null) {
            try {
                codec = codecClass.getDeclaredConstructor().newInstance();
                codecCache.putIfAbsent(codecClass, codec);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return (T) codec;
    }

    /**
     * 根据 {@link REntity} 注解解析实体级编解码器。
     *
     * @param anno REntity 注解实例
     * @param cls 实体类
     * @param config Redisson 配置
     */
    @Override
    public <T extends Codec> T getCodec(REntity anno, Class<?> cls, Config config) {
        if (!ClassUtils.isAnnotationPresent(cls, anno.annotationType())) {
            throw new IllegalArgumentException("Annotation REntity does not present on type [" + cls.getCanonicalName() + "]");
        }
        
        Class<?> codecClass;
        if (anno.codec() == REntity.DEFAULT.class) {
            codecClass = config.getCodec().getClass();
        } else {
            codecClass = anno.codec();
        }

        return this.getCodec((Class<T>) codecClass);
    }

    /**
     * 根据 {@link RObjectField} 注解解析字段级编解码器。
     *
     * @param anno RObjectField 注解
     * @param cls 声明字段的类
     * @param rObjectClass RObject 具体实现类（不可为接口）
     * @param fieldName 字段名
     * @param config Redisson 配置
     */
    @Override
    public <T extends Codec, K extends RObject> T getCodec(RObjectField anno, Class<?> cls, Class<K> rObjectClass, String fieldName, Config config) {
        if (!ClassUtils.isAnnotationPresent(cls, anno.annotationType())) {
            throw new IllegalArgumentException("Annotation RObjectField does not present on field " + fieldName + " of type [" + cls.getCanonicalName() + "]");
        }
        if (rObjectClass.isInterface()) {
            throw new IllegalArgumentException("Cannot lookup an interface class of RObject [" + rObjectClass.getCanonicalName() + "]. Concrete class only.");
        }
        
        Class<?> codecClass;
        if (anno.codec() == RObjectField.DEFAULT.class) {
            codecClass = config.getCodec().getClass();
        } else {
            codecClass = anno.codec();
        }
        
        return this.getCodec((Class<T>) codecClass);
    }
    
    /**
     * 注册自定义 Codec 实例到缓存（仅当该类型尚未缓存时生效）。
     *
     * @param cls Codec 类型
     * @param codec 要注册的实例
     */
    @Override
    public <T extends Codec> void registerCodec(Class<T> cls, T codec) {
        if (!cls.isInstance(codec)) {
            throw new IllegalArgumentException("codec is not an instance of the class [" + cls.getCanonicalName() + "]");
        }
        codecCache.putIfAbsent(cls, codec);
    }

}
