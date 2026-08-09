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
 * Pub/Sub 订阅连接的目标节点选择模式。
 * <p>
 * 在 {@link MasterSlaveServersConfig} 中配置，决定订阅命令连接主节点还是从节点。
 *
 * @author Nikita Koksharov
 *
 */
public enum SubscriptionMode {

    /** 订阅连接绑定从节点，减轻主节点 Pub/Sub 负载。 */
    SLAVE,

    /** 订阅连接绑定主节点。 */
    MASTER

}
