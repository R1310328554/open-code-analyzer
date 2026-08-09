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
package org.redisson.liveobject.resolver;

import org.redisson.api.annotation.RId;
import org.redisson.command.CommandAsyncExecutor;

/**
 * {@link org.redisson.api.annotation.RId} 字段值的生成策略。
 * <p>
 * {@link org.redisson.RedissonLiveObjectService} 实例化实体前调用 {@link #resolve} 填充主键。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 * @param <V> Value type
 */
public interface RIdResolver<V> {

    /**
     * {@link org.redisson.RedissonLiveObjectService} 实例化实体类后调用，为 {@link RId} 字段生成主键值。
     * 
     * @param cls the class of the LiveObject.
     * @param annotation the RId annotation used in the class.
     * @param idFieldName field id
     * @param commandAsyncExecutor instance
     * @return resolved RId field value.
     */
    V resolve(Class<?> cls, RId annotation, String idFieldName, CommandAsyncExecutor commandAsyncExecutor);

}
