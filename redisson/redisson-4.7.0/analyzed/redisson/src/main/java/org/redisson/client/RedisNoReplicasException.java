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
package org.redisson.client;

/**
 * 主节点可用从节点不足（NOREPLICAS）时抛出的可重试异常。
 * <p>
 * 常见于 {@code min-replicas-to-write} 等配置阻止写入时。
 */
public class RedisNoReplicasException extends RedisRetryException {

    private static final long serialVersionUID = -5658453331593029252L;

    /** 使用错误消息构造 NOREPLICAS 异常。 */
    public RedisNoReplicasException(String message) {
        super(message);
    }
}
