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

/**
 * 事务等待或执行超时时抛出，是 {@link TransactionException} 的子类。
 * <p>
 * 通常在 {@link org.redisson.api.TransactionOptions} 设定的超时到期后触发。
 *
 * @author Nikita Koksharov
 *
 */
public class TransactionTimeoutException extends TransactionException {

    private static final long serialVersionUID = 7126673140273327142L;

    /** @param message 超时说明 */
    public TransactionTimeoutException(String message) {
        super(message);
    }

}
