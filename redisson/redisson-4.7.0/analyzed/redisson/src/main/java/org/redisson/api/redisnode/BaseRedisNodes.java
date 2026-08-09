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

import java.util.concurrent.TimeUnit;

/**
 * Redis 节点 API 的基接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface BaseRedisNodes {

    /**
     * 对所有 Redis 节点执行 PING。
     * 每个节点的默认超时为 1000 毫秒。
     *
     * @return 若所有节点均回复 "PONG" 则返回 <code>true</code>，否则返回 <code>false</code>。
     */
    boolean pingAll();

    /**
     * 对所有 Redis 节点执行 PING，并指定每个节点的超时。
     *
     * @return 若所有节点均回复 "PONG" 则返回 <code>true</code>，否则返回 <code>false</code>。
     */
    boolean pingAll(long timeout, TimeUnit timeUnit);

}
