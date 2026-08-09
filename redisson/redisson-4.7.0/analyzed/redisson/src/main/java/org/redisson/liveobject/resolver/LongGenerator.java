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

import org.redisson.RedissonAtomicLong;
import org.redisson.api.annotation.RId;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandBatchService;

/**
 * {@link RId} 自增 Long 主键生成器。
 * <p>
 * 每个实体类+字段名对应一个 {@link RedissonAtomicLong} key，{@link #resolve} 时 incrementAndGet。
 * 不支持在 {@link CommandBatchService} 批处理中使用。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public class LongGenerator implements RIdResolver<Long> {

    /** 单例实例，供 {@code @RId} 默认引用。 */
    public static final LongGenerator INSTANCE = new LongGenerator();

    /** 在 Redis 原子长整型计数器上自增并返回新 id。 */
    @Override
    public Long resolve(Class<?> value, RId id, String idFieldName, CommandAsyncExecutor commandAsyncExecutor) {
        if (commandAsyncExecutor instanceof CommandBatchService) {
            throw new IllegalStateException("this generator couldn't be used in batch");
        }

        return new RedissonAtomicLong(commandAsyncExecutor, this.getClass().getCanonicalName()
                + "{" + value.getCanonicalName() + "}:" + idFieldName).incrementAndGet();
    }

}
