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
package org.redisson.spring.data.connection;

import org.redisson.reactive.CommandReactiveExecutor;
import org.springframework.data.redis.connection.ReactiveClusterHashCommands;

/**
 * 集群模式下 Spring Data Redis 响应式 Hash 命令适配器。
 * <p>继承 {@link RedissonReactiveHashCommands} 并实现 {@link ReactiveClusterHashCommands}，
 * 在集群拓扑下复用单机响应式命令实现。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReactiveClusterHashCommands extends RedissonReactiveHashCommands implements ReactiveClusterHashCommands {

    /** 注入响应式命令执行器。 */
    RedissonReactiveClusterHashCommands(CommandReactiveExecutor executorService) {
        super(executorService);
    }

}
