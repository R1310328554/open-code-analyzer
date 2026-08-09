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

import java.net.InetSocketAddress;

/**
 * 监听 Valkey 或 Redis 发布的<b>清空数据库</b>（flushdb/flushall）键空间事件。
 * <p>
 * 需要 Redis 6.0 及以上版本。
 *
 * @author Nikita Koksharov
 *
 */
@FunctionalInterface
public interface FlushListener extends ObjectListener {

    /**
     * 当 Redis 节点执行 {@code flushdb} 或 {@code flushall} 命令时触发。
     *
     * @param address 执行清空命令的节点地址
     */
    void onFlush(InetSocketAddress address);

}
