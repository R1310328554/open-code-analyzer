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
 * Redis 命令响应等待超时异常。
 * <p>
 * 表示已发送命令但在配置时限内未收到完整响应。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisResponseTimeoutException extends RedisTimeoutException {

    private static final long serialVersionUID = 2829224148153662863L;

    /** 使用详细超时说明构造异常。 */
    public RedisResponseTimeoutException(String message) {
        super(message);
    }

}
