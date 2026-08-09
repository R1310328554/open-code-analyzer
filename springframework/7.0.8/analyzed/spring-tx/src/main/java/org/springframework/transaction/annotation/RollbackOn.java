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

package org.springframework.transaction.annotation;

/**
 * 全局回滚行为的枚举。
 *
 * <p>默认行为与 EJB CMT 和 JTA 的传统行为一致，
 * 后者回滚规则与 Spring 类似。
 * 全局切换为任意异常触发回滚会影响 Spring 的
 * {@link Transactional} 以及 {@link jakarta.transaction.Transactional}，
 * 但不改变基于非规则的 {@link jakarta.ejb.TransactionAttribute}。
 *
 * @author Juergen Hoeller
 * @since 6.2
 * @see EnableTransactionManagement#rollbackOn()
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 */
public enum RollbackOn {

	/**
	 * 默认回滚行为：对 {@link RuntimeException RuntimeExceptions}
	 * 以及 {@link Error Errors} 回滚。
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#ROLLBACK_ON_RUNTIME_EXCEPTIONS
	 */
	RUNTIME_EXCEPTIONS,

	/**
	 * 替代模式：对所有异常（包括任何受检 {@link Exception}）回滚。
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#ROLLBACK_ON_ALL_EXCEPTIONS
	 */
	ALL_EXCEPTIONS

}
