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
 * 主从/哨兵/集群等拓扑下的读命令路由模式。
 * <p>在 {@link BaseMasterSlaveServersConfig#setReadMode(ReadMode)} 中配置，
 * 与 {@link org.redisson.connection.balancer.LoadBalancer} 配合选择具体节点。
 *
 * @author Nikita Koksharov
 *
 */
public enum ReadMode {

    /** 优先从从节点读；无从节点时回退主节点，由 loadBalancer 选具体从节点。 */
    SLAVE,

    /** 所有读命令走主节点（强一致读，增加主节点负载）。 */
    MASTER,

    /** 主从节点均可读，由 loadBalancer 在全部可读节点间均衡。 */
    MASTER_SLAVE,

}
