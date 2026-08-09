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
package org.redisson.api.redisnode;

/**
 * Valkey 或 Redis 单节点部署的访问 API。
 * <p>
 * 用于获取单机模式下的唯一 Redis 主节点实例。
 *
 * @author Nikita Koksharov
 *
 */
public interface RedisSingle extends BaseRedisNodes {

    /**
     * 返回 Valkey 或 Redis 单节点实例。
     *
     * @return Redis 主节点
     */
    RedisMaster getInstance();

}
