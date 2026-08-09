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
 * Redis 操作超时异常基类。
 * <p>
 * 涵盖连接、命令或响应等待等超时场景。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisTimeoutException extends RedisException {

    private static final long serialVersionUID = -8418769175260962404L;

    /** 构造无附加消息的默认超时异常。 */
    public RedisTimeoutException() {
    }

    /** 使用详细超时说明构造异常。 */
    public RedisTimeoutException(String message) {
        super(message);
    }

}
