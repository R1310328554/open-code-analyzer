/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction;

/**
 * 进行中的 {@link ReactiveTransactionManager} 事务的表示。
 * 当前为扩展 {@link TransactionExecution} 的标记接口，
 * 未来版本可能增加更多方法。
 *
 * <p>事务代码可用其获取状态信息，
 * 并以编程方式请求回滚（而非抛出导致隐式回滚的异常）。
 *
 * @author Mark Paluch
 * @author Juergen Hoeller
 * @since 5.2
 * @see #setRollbackOnly()
 * @see ReactiveTransactionManager#getReactiveTransaction
 * @see org.springframework.transaction.reactive.TransactionCallback#doInTransaction
 */
public interface ReactiveTransaction extends TransactionExecution {

}
