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
package org.redisson;

import org.redisson.api.RObject;
import org.redisson.api.RObjectReactive;
import org.redisson.api.RObjectRx;
import org.redisson.api.annotation.REntity;
import org.redisson.client.codec.Codec;
import org.redisson.liveobject.misc.ClassUtils;

import java.io.Serializable;

/**
 * 可序列化的 Redisson 对象引用，保存类型名、Redis 键名与可选 {@link org.redisson.client.codec.Codec}。
 * <p>支持 {@link org.redisson.api.RObject}、Reactive/Rx 变体及 {@link org.redisson.api.annotation.REntity} 类型；
 * Reactive/Rx 接口会映射到对应的同步实现类名。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 * @author Nikita Koksharov
 */
public class RedissonReference implements Serializable {

    private static final long serialVersionUID = -2378564460151709127L;
    
    private String type;
    private String keyName;
    private String codec;

    /** 无参构造，供序列化框架使用。 */
    public RedissonReference() {
    }

    /** @param type Redisson 对象接口或 LiveObject 类型
     *  @param keyName Redis 键名 */
    public RedissonReference(Class<?> type, String keyName) {
        this(type, keyName, null);
    }

    /** @param codec 可选编解码器；非空时保存其类全名 */
    public RedissonReference(Class<?> type, String keyName, Codec codec) {
        if (!ClassUtils.isAnnotationPresent(type, REntity.class)
                && !RObject.class.isAssignableFrom(type)
                    && !RObjectReactive.class.isAssignableFrom(type)
                        && !RObjectRx.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Class reference has to be a type of either RObject/RLiveObject/RObjectReactive/RObjectRx");
        }
        if (RObjectReactive.class.isAssignableFrom(type)) {
            String t = type.getName().replaceFirst("Reactive", "");
            if (!isAvailable(t)) {
                throw new IllegalArgumentException("There is no compatible type for " + type);
            }
            this.type = t;
        } else if (RObjectRx.class.isAssignableFrom(type)) {
            String t = type.getName().replaceFirst("Rx", "");
            if (!isAvailable(t)) {
                throw new IllegalArgumentException("There is no compatible type for " + type);
            }
            this.type = t;
        } else {
            this.type = type.getName();
        }
        this.keyName = keyName;
        if (codec != null) {
            this.codec = codec.getClass().getName();
        }
    }

    /** @return 同步 {@link org.redisson.api.RObject} 实现类
     *  @throws java.lang.ClassNotFoundException 类不在 classpath 时 */

    public Class<?> getType() throws ClassNotFoundException {
        return Class.forName(type);
    }

    /** 返回 {@code type + "Rx"} 对应的 RxJava 接口类；LiveObject 不支持。 */
    public Class<?> getRxJavaType() throws ClassNotFoundException {
        String rxName = type + "Rx";
        if (isAvailable(rxName)) {
            return Class.forName(rxName); //live object is not supported in reactive client
        }
        throw new ClassNotFoundException("There is no RxJava compatible type for " + type);
    }

    /** @return Reactive 变体类（{@code type + "Reactive"}）
     *  @throws java.lang.ClassNotFoundException 无对应 Reactive 类型时 */

    public Class<?> getReactiveType() throws ClassNotFoundException {
        String reactiveName = type + "Reactive";
        if (isAvailable(reactiveName)) {
            return Class.forName(reactiveName); //live object is not supported in reactive client
        }
        throw new ClassNotFoundException("There is no Reactive compatible type for " + type);
    }

    /** @return 已解析的同步实现类全名字符串 */

    public String getTypeName() {
        return type;
    }

    /** @return Redis 键名 */

    public String getKeyName() {
        return keyName;
    }

    /** @param keyName 要设置的 Redis 键名 */

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
    
    public String getCodec() {
        return codec;
    }

    /** @return 编解码器类；未指定时返回 {@code null}
     *  @throws java.lang.ClassNotFoundException codec 类无法加载时 */

    public Class<? extends Codec> getCodecType() throws ClassNotFoundException {
        if (codec != null) {
            return (Class<? extends Codec>) Class.forName(codec);
        }
        return null;
    }

    /** 探测给定类名是否可通过 {@link Class#forName} 加载。 */
    private boolean isAvailable(String type) {
        try {
            Class.forName(type);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
