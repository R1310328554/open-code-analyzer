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

import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;

/**
 * {@link org.redisson.api.RList} 的分布式并发 List 实现。
 * <p>底层 Redis {@code LIST}；具体读写逻辑由 {@link BaseRedissonList} 提供。
 *
 * @author Nikita Koksharov
 * @param <V> 列表元素类型
 */
public class RedissonList<V> extends BaseRedissonList<V> implements RList<V> {

    /** 使用默认 codec 构造。 */
    public RedissonList(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(commandExecutor, name, redisson);
    }

    /** @param codec 元素编解码器 */
    public RedissonList(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(codec, commandExecutor, name, redisson);
    }

}
