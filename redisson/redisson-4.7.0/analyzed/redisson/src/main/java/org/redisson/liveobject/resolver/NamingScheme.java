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

import org.redisson.client.codec.Codec;

/**
 * Live Object 在 Redis 中的 key 命名与 id 编解码策略。
 * <p>
 * 由 {@code @REntity#namingScheme()} 指定实现类；{@link DefaultNamingScheme} 为默认。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 * @author Nikita Koksharov
 */
public interface NamingScheme {

    /** 实体 key 的 SCAN 通配模式。 */
    String getNamePattern(Class<?> entityClass);

    /** 由实体类与 id 生成唯一 Redis key。 */
    String getName(Class<?> entityClass, Object idValue);
    
    /** 索引结构使用的 Redis key。 */
    String getIndexName(Class<?> entityClass, String fieldName);
    
    /** 嵌套 RObject 字段的引用 key。 */
    String getFieldReferenceName(Class<?> entityClass, Object idValue, Class<?> fieldClass, String fieldName);

    /** 从 Redis key 反解出 id 值。 */
    Object resolveId(String name);
    
    /** 命名方案绑定的 Codec（与 key 中 id 编码一致）。 */
    Codec getCodec();
    
}
