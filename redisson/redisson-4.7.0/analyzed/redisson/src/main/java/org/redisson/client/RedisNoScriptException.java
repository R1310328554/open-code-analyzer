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
 * EVAL 脚本缓存中未找到指定脚本（NOSCRIPT）时抛出。
 * <p>
 * 通常需使用 EVALSHA 前先 SCRIPT LOAD，或回退 EVAL。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisNoScriptException extends RedisException {

    private static final long serialVersionUID = -2565335188503354660L;

    /** 使用错误消息构造 NOSCRIPT 异常。 */
    public RedisNoScriptException(String message) {
        super(message);
    }

}
