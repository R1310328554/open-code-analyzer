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

package org.springframework.jdbc.datasource.lookup;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * 根据当前事务隔离级别路由到多个目标 DataSource 之一的 DataSource。
 * 目标 DataSource 须以 {@link org.springframework.transaction.TransactionDefinition TransactionDefinition}
 * 接口定义的隔离级别名称作为键进行配置。
 *
 * <p>与 JTA 事务管理（通常通过 Spring 的
 * {@link org.springframework.transaction.jta.JtaTransactionManager}）结合时尤其有用。
 * 标准 JTA 不支持事务级隔离级别。部分 JTA 提供者以厂商扩展支持隔离级别（如 WebLogic），
 * 这是首选方案。另一种替代（如在 WebSphere 上）是通过多个 JNDI DataSource
 * 表示目标数据库，每个 DataSource 配置不同隔离级别（作用于整个 DataSource）。
 * {@code IsolationLevelDataSourceRouter} 可根据当前事务隔离级别透明切换到合适的 DataSource。
 *
 * <p>例如，假设目标 DataSource 定义为名为
 * "myRepeatableReadDataSource"、"mySerializableDataSource" 和 "myDefaultDataSource" 的 Spring Bean，
 * 配置可如下：
 *
 * <pre class="code">
 * &lt;bean id="dataSourceRouter" class="org.springframework.jdbc.datasource.lookup.IsolationLevelDataSourceRouter"&gt;
 *   &lt;property name="targetDataSources"&gt;
 *     &lt;map&gt;
 *       &lt;entry key="ISOLATION_REPEATABLE_READ" value-ref="myRepeatableReadDataSource"/&gt;
 *       &lt;entry key="ISOLATION_SERIALIZABLE" value-ref="mySerializableDataSource"/&gt;
 *     &lt;/map&gt;
 *   &lt;/property&gt;
 *   &lt;property name="defaultTargetDataSource" ref="myDefaultDataSource"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * 或者，键控值也可以是数据源名称，通过 {@link #setDataSourceLookup DataSourceLookup}
 * 解析：默认使用标准 JNDI 查找的 JNDI 名称。这样可简洁定义，无需单独的 DataSource Bean。
 *
 * <pre class="code">
 * &lt;bean id="dataSourceRouter" class="org.springframework.jdbc.datasource.lookup.IsolationLevelDataSourceRouter"&gt;
 *   &lt;property name="targetDataSources"&gt;
 *     &lt;map&gt;
 *       &lt;entry key="ISOLATION_REPEATABLE_READ" value="java:comp/env/jdbc/myrrds"/&gt;
 *       &lt;entry key="ISOLATION_SERIALIZABLE" value="java:comp/env/jdbc/myserds"/&gt;
 *     &lt;/map&gt;
 *   &lt;/property&gt;
 *   &lt;property name="defaultTargetDataSource" value="java:comp/env/jdbc/mydefds"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * 注意：若与 Spring 的 {@link org.springframework.transaction.jta.JtaTransactionManager}
 * 结合使用本路由器，请将 "allowCustomIsolationLevels" 标志设为 "true"。
 * （默认情况下，JtaTransactionManager 仅接受默认隔离级别，
 * 因为标准 JTA 本身不支持隔离级别。）
 *
 * <pre class="code">
 * &lt;bean id="transactionManager" class="org.springframework.transaction.jta.JtaTransactionManager"&gt;
 *   &lt;property name="allowCustomIsolationLevels" value="true"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0.1
 * @see #setTargetDataSources
 * @see #setDefaultTargetDataSource
 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
public class IsolationLevelDataSourceRouter extends AbstractRoutingDataSource {

	/**
	 * {@link TransactionDefinition} 中定义的隔离级别常量名到常量值的映射。
	 */
	static final Map<String, Integer> constants = Map.of(
			"ISOLATION_DEFAULT", TransactionDefinition.ISOLATION_DEFAULT,
			"ISOLATION_READ_UNCOMMITTED", TransactionDefinition.ISOLATION_READ_UNCOMMITTED,
			"ISOLATION_READ_COMMITTED", TransactionDefinition.ISOLATION_READ_COMMITTED,
			"ISOLATION_REPEATABLE_READ", TransactionDefinition.ISOLATION_REPEATABLE_READ,
			"ISOLATION_SERIALIZABLE", TransactionDefinition.ISOLATION_SERIALIZABLE
		);


	/**
	 * 支持隔离级别常量的 Integer 值，
	 * 以及 {@link org.springframework.transaction.TransactionDefinition TransactionDefinition 接口}
	 * 定义的隔离级别名称。
	 */
	@Override
	protected Object resolveSpecifiedLookupKey(Object lookupKey) {
		if (lookupKey instanceof Integer isolationLevel) {
			Assert.isTrue(constants.containsValue(isolationLevel),
					"Only values of isolation constants allowed");
			return isolationLevel;
		}
		else if (lookupKey instanceof String constantName) {
			Assert.hasText(constantName, "'lookupKey' must not be null or blank");
			Integer isolationLevel = constants.get(constantName);
			Assert.notNull(isolationLevel, "Only isolation constants allowed");
			return isolationLevel;
		}
		else {
			throw new IllegalArgumentException(
					"Invalid lookup key - needs to be isolation level Integer or isolation level name String: " + lookupKey);
		}
	}

	@Override
	protected @Nullable Object determineCurrentLookupKey() {
		return TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
	}

}
