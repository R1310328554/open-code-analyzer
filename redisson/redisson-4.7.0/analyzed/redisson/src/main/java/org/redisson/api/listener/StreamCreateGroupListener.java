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
package org.redisson.api.listener;

import org.redisson.api.ObjectListener;

/**
 * 监听 Valkey 或 Redis 发布的流<b>创建消费组</b>（xgroup-create）键空间事件。
 * <p>
 * 当 Stream 上新建消费组时触发；需在 Redis 配置 {@code notify-keyspace-events} 中包含 {@code E} 与 {@code t} 字母。
 *
 * @author Nikita Koksharov
 */
@FunctionalInterface
public interface StreamCreateGroupListener extends ObjectListener {

    /**
     * 当 Stream 上创建新消费组时触发。
     *
     * @param name 对象名称（键名）
     */
    void onCreateGroup(String name);

}
