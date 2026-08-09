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
 * Redis 连接上无法执行写操作时抛出的异常。
 * <p>
 * 常见于连接已关闭、只读副本或网络写入失败等场景。
 *
 * @author Nikita Koksharov
 *
 */
public class WriteRedisConnectionException extends RedisConnectionException {

    private static final long serialVersionUID = -4756928186967834601L;

    /** 使用错误消息构造写连接异常。 */
    public WriteRedisConnectionException(String msg) {
        super(msg);
    }
    
    /** 使用错误消息与根因构造写连接异常。 */
    public WriteRedisConnectionException(String msg, Throwable e) {
        super(msg, e);
    }

}
