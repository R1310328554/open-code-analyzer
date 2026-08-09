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
 * Redis 命令执行或协议处理相关的运行时异常基类。
 * <p>
 * 多数 Redisson 客户端错误均继承此类或其子类。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisException extends RuntimeException {

    private static final long serialVersionUID = 3389820652701696154L;

    /** 构造无消息的 Redis 异常。 */
    public RedisException() {
    }

    /** 以根因构造 Redis 异常。 */
    public RedisException(Throwable cause) {
        super(cause);
    }

    /** 以消息与根因构造 Redis 异常。 */
    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 以消息构造 Redis 异常。 */
    public RedisException(String message) {
        super(message);
    }

}
