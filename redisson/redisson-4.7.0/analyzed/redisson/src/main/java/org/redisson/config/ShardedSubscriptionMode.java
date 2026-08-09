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
package org.redisson.config;

/**
 * 集群模式下分片 Pub/Sub（SPUBLISH/SSUBSCRIBE）的使用策略。
 * <p>Redis 7+ 支持按 slot 分片订阅，减轻单节点订阅压力；
 * 在 {@link org.redisson.config.Config} 或集群子配置中指定。
 *
 * @author Nikita Koksharov
 *
 */
public enum ShardedSubscriptionMode {

    /** 仅在服务端支持时启用分片 Pub/Sub，否则回退经典模式。 */
    AUTO,

    /** 强制使用分片 Pub/Sub（不支持时可能失败）。 */
    ON,

    /** 禁用分片 Pub/Sub，始终使用传统 SUBSCRIBE/PUBLISH。 */
    OFF

}
