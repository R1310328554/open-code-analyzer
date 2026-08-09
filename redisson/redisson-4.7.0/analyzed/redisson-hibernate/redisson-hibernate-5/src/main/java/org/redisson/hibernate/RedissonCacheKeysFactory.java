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
package org.redisson.hibernate;

import io.netty.buffer.ByteBuf;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.persister.collection.CollectionPersister;
import org.redisson.client.codec.Codec;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Redisson 自定义 {@link DefaultCacheKeysFactory}，用于 Hibernate 5 集合缓存键生成。
 * <p>在生成集合键前临时清空嵌入集合字段，经 Redisson {@link Codec} 编解码后再
 * 恢复字段值，避免集合引用影响键的稳定性。</p>
 *
 * @author Nikita Koksharov
 */
public class RedissonCacheKeysFactory extends DefaultCacheKeysFactory {

    /** Redisson 编解码器，用于集合 ID 的序列化与反序列化。 */
    private final Codec codec;

    /** @param codec Redisson 客户端使用的 {@link Codec} */
    public RedissonCacheKeysFactory(Codec codec) {
        this.codec = codec;
    }

    /** 生成集合缓存键：临时剥离嵌入集合字段后经 Codec 规范化 ID，再委托父类生成键。
     *
     * @param id 实体标识
     * @param persister 集合持久化器
     * @param factory SessionFactory 实现
     * @param tenantIdentifier 租户标识
     * @return Hibernate 集合缓存键
     */
    @Override
    public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        try {
            String[] parts = persister.getRole().split("\\.");
            Field f = ReflectHelper.findField(id.getClass(), parts[parts.length - 1]);

            // 临时清空嵌入集合字段，使 ID 可稳定编码。
            Object prev = f.get(id);
            f.set(id, null);
            ByteBuf state = codec.getMapKeyEncoder().encode(id);
            Object newId = codec.getMapKeyDecoder().decode(state, null);
            state.release();
            // 恢复字段后使用规范化 ID 生成缓存键。
            f.set(id, prev);
            return super.createCollectionKey(newId, persister, factory, tenantIdentifier);
        // 找不到嵌入字段时回退至默认键生成逻辑。
        } catch (PropertyNotFoundException e) {
            return super.createCollectionKey(id, persister, factory, tenantIdentifier);
        } catch (IllegalAccessException | IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
