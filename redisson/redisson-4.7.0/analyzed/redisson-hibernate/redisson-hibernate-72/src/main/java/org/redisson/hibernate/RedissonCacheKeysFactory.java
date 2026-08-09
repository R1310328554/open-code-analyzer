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
 * Redisson 专用的 Hibernate 缓存键工厂。
 * <p>生成集合缓存键前临时清空关联字段，使 Redisson 编解码器与 Hibernate 默认键格式对齐。
 *
 * @author Nikita Koksharov
 */
public class RedissonCacheKeysFactory extends DefaultCacheKeysFactory {

    /** 用于序列化/反序列化 Map 键的 Redisson 编解码器。 */
    private final Codec codec;

    /** @param codec Redisson 编解码器，用于键的编码与解码
     */
    public RedissonCacheKeysFactory(Codec codec) {
        this.codec = codec;
    }

    /** 生成集合缓存键：先按编解码器规范化 id，再委托父类默认实现。
     * 若反射找不到关联字段则回退至默认键生成。
     */
    @Override
    public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        try {
            String[] parts = persister.getRole().split("\\.");
            Field f = ReflectHelper.findField(id.getClass(), parts[parts.length - 1]);

            // 临时清空关联引用，避免编解码器序列化多余字段。
            Object prev = f.get(id);
            f.set(id, null);
            ByteBuf state = codec.getMapKeyEncoder().encode(id);
            Object newId = codec.getMapKeyDecoder().decode(state, null);
            state.release();
            f.set(id, prev);
            // 使用规范化后的 id 调用 Hibernate 默认键工厂。
            return super.createCollectionKey(newId, persister, factory, tenantIdentifier);
        } catch (PropertyNotFoundException e) {
            return super.createCollectionKey(id, persister, factory, tenantIdentifier);
        } catch (IllegalAccessException | IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
