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
 * Redis 服务器繁忙时抛出的异常（{@code WAIT} 相关场景）。
 * <p>
 * 表示复制同步或阻塞操作未能在预期时间内完成，可稍后重试。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisWaitException extends RedisRetryException {

    private static final long serialVersionUID = -5658453331593019251L;

    /** 使用错误消息构造 WAIT 异常。 */
    public RedisWaitException(String message) {
        super(message);
    }
}
