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
 * 可重试的 Redis 操作异常基类。
 * <p>
 * 表示临时性故障，上层可依据策略延迟后重试。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisRetryException extends RedisException {

    /** 使用错误消息构造可重试异常。 */
    public RedisRetryException(String message) {
        super(message);
    }
}
