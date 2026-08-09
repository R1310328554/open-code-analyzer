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
 * Redis 连接建立或通信失败时抛出的异常。
 * <p>
 * 区别于命令级 {@link RedisException}，表示传输层或握手问题。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisConnectionException extends RedisException {

    private static final long serialVersionUID = -4756928186967834601L;

    /** 以根因构造连接异常。 */
    public RedisConnectionException(Throwable cause) {
        super(cause);
    }

    /** 以消息构造连接异常。 */
    public RedisConnectionException(String msg) {
        super(msg);
    }

    /** 以消息与根因构造连接异常。 */
    public RedisConnectionException(String msg, Throwable e) {
        super(msg, e);
    }

}
