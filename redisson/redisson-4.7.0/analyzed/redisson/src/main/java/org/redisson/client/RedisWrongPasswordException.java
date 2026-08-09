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
 * Redis 用户名或密码不正确时抛出的异常。
 * <p>
 * 通常在 AUTH/HELLO 握手阶段因凭据校验失败而触发。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisWrongPasswordException extends RedisException {

    private static final long serialVersionUID = -2565335188503354660L;

    /** 使用服务器返回的错误消息构造异常。 */
    public RedisWrongPasswordException(String message) {
        super(message);
    }

}
