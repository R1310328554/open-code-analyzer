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
 * 连接快速重连过程中，先前未完成命令被中断时抛出。
 * <p>
 * 提示调用方在重连完成后重新发送命令。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisReconnectedException extends RedisException {

    /** 使用错误消息构造重连中断异常。 */
    public RedisReconnectedException(String message) {
        super(message);
    }
}
