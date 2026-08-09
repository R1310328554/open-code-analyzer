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

import java.util.UUID;

/**
 * 为 {@link RId} 字段生成随机 UUID 字符串主键。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public class UUIDGenerator implements RIdResolver<String>{

    /** 单例实例。 */
    public static final UUIDGenerator INSTANCE = new UUIDGenerator();
    
    /** 返回 {@link UUID#randomUUID()} 的字符串形式。 */
    @Override
    public String resolve(Class<?> value, RId id, String idFieldName, CommandAsyncExecutor commandAsyncExecutor) {
        return UUID.randomUUID().toString();
    }
    
}
