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
 * Redis Cluster 槽位（slot）状态变更命令枚举。
 * <p>
 * 对应 CLUSTER SETSLOT 子命令，用于集群重分片或故障迁移期间标记槽位状态。
 *
 * @author Nikita Koksharov
 *
 */
public enum SetSlotCommand {

    /** 槽位正在迁出（migrating）至目标节点 */
    MIGRATING,
    /** 槽位正在从源节点导入（importing） */
    IMPORTING,
    /** 槽位处于稳定（stable）状态，无迁移进行中 */
    STABLE,
    /** 将槽位绑定到指定节点 */
    NODE

}
