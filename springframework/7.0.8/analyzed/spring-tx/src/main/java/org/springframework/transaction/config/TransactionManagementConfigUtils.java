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

package org.springframework.transaction.config;

/**
 * 供子包内部共享的配置常量。
 *
 * @author Chris Beams
 * @author Stephane Nicoll
 * @since 3.1
 */
public abstract class TransactionManagementConfigUtils {

	/**
	 * 内部管理的事务 Advisor 的 Bean 名称（mode == PROXY 时使用）。
	 */
	public static final String TRANSACTION_ADVISOR_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAdvisor";

	/**
	 * 内部管理的事务 Aspect 的 Bean 名称（mode == ASPECTJ 时使用）。
	 */
	public static final String TRANSACTION_ASPECT_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAspect";

	/**
	 * AspectJ 事务管理切面的类名。
	 */
	public static final String TRANSACTION_ASPECT_CLASS_NAME =
			"org.springframework.transaction.aspectj.AnnotationTransactionAspect";

	/**
	 * AspectJ 事务管理 @{@code Configuration} 类的名称。
	 */
	public static final String TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME =
			"org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration";

	/**
	 * 内部管理的 JTA 事务 Aspect 的 Bean 名称（mode == ASPECTJ 时使用）。
	 * @since 5.1
	 */
	public static final String JTA_TRANSACTION_ASPECT_BEAN_NAME =
			"org.springframework.transaction.config.internalJtaTransactionAspect";

	/**
	 * AspectJ 事务管理切面的类名。
	 * @since 5.1
	 */
	public static final String JTA_TRANSACTION_ASPECT_CLASS_NAME =
			"org.springframework.transaction.aspectj.JtaAnnotationTransactionAspect";

	/**
	 * JTA 的 AspectJ 事务管理 @{@code Configuration} 类名称。
	 * @since 5.1
	 */
	public static final String JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME =
			"org.springframework.transaction.aspectj.AspectJJtaTransactionManagementConfiguration";

	/**
	 * 内部管理的 TransactionalEventListenerFactory 的 Bean 名称。
	 */
	public static final String TRANSACTIONAL_EVENT_LISTENER_FACTORY_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionalEventListenerFactory";

}
