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
package org.redisson.transaction;

import org.redisson.client.RedisException;

/**
 * Redisson 事务执行失败时抛出的异常，继承 {@link org.redisson.client.RedisException}。
 * <p>
 * 常见原因：WATCH 键被修改、commit 冲突、业务校验失败等。
 *
 * @author Nikita Koksharov
 *
 */
public class TransactionException extends RedisException {

    private static final long serialVersionUID = 7126673140273327142L;

    /** @param message 错误描述 */
    public TransactionException(String message) {
        super(message);
    }

    /** @param message 错误描述 @param cause 根因 */
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }


}
