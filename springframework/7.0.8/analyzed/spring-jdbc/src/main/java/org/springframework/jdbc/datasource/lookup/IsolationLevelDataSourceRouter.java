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
 * 根据当前事务隔离级别路由到各种目标数据源之一的数据源。目标数据源需要使用隔离级别名称作为键进行配置，如 {@link org.springframework.transacti
 * on.TransactionDefinition TransactionDefinition} 接口上所定义。
 * <p> 这在与 JTA 事务管理（通常通过 Spring 的 {@link org.springframework.transaction.jta.JtaTransaction
 * Manager}）结合时特别有用。标准 JTA 不支持特定于事务的隔离级别。一些 JTA 提供程序支持隔离级别作为特定于供应商的扩展（例如 WebLogic），这是解决此问题的
 * 首选方法。作为替代方案（例如，在 WebSphere 上），目标数据库可以通过多个 JNDI 数据源表示，每个数据源配置不同的隔离级别（针对整个数据源）。 {@code Iso
 * lationLevelDataSourceRouter} 允许根据当前事务的隔离级别透明地切换到适当的数据源。
 * <p>例如，配置如下所示，假设目标 DataSource
 * 被定义为名称为“myRepeatableReadDataSource”、“mySerializedDataSource”和“myDefaultDataSource”的单独
 * Spring beans：
 * <pre class="code"> <bean id="dataSourceRouter"
 * class="org.springframework.jdbc.datasource.lookup.IsolationLevelDataSourceRouter">;
 * <属性名称=“targetDataSources”> <地图> <entry key="ISOLATION_REPEATABLE_READ"
 * value-ref="myRepeatableReadDataSource"/>> <entry key="ISOLATION_SERIALIZABLE"
 * value-ref="mySerializedDataSource"/>> &lt;/地图&gt; &lt;/属性&gt;
 * &lt;属性名称=“defaultTargetDataSource”ref=“myDefaultDataSource”/> </bean></pre>
 * 或者，键值也可以是数据源名称，通过 {@link #setDataSourceLookup DataSourceLookup} 解析：默认情况下，标准 JNDI 查找的
 * JNDI 名称。这允许单个简洁的定义，而不需要单独的 DataSource bean 定义。
 * <pre class="code"> <bean id="dataSourceRouter"
 * class="org.springframework.jdbc.datasource.lookup.IsolationLevelDataSourceRouter">;
 * <属性名称=“targetDataSources”> <地图> <entry key="ISOLATION_REPEATABLE_READ"
 * value="java:comp/env/jdbc/myrrds"/>> <entry key="ISOLATION_SERIALIZABLE"
 * value="java:comp/env/jdbc/myserds"/>> &lt;/地图&gt; &lt;/属性&gt;
 * &lt;属性名称=“defaultTargetDataSource”值=“java:comp/env/jdbc/mydefds”/&gt; </bean></pre>
 * 注意：如果您将此路由器与 Spring 的 {@link org.springframework.transaction.jta.JtaTransactionManager}
 * 结合使用，请不要忘记将“allowCustomIsolationLevels”标志切换为“true”。 （默认情况下，JtaTransactionManager
 * 只接受默认的隔离级别，因为标准 JTA 本身缺乏隔离级别支持。）
 * <pre class="code"> <bean id="transactionManager"
 * class="org.springframework.transaction.jta.JtaTransactionManager">
 * <属性名称=“allowCustomIsolationLevels”值=“true”/> </bean></pre>
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
	 * {@link TransactionDefinition} 中定义的隔离常量的常量名称到常量值的映射。
	 */
	static final Map<String, Integer> constants = Map.of(
			"ISOLATION_DEFAULT", TransactionDefinition.ISOLATION_DEFAULT,
			"ISOLATION_READ_UNCOMMITTED", TransactionDefinition.ISOLATION_READ_UNCOMMITTED,
			"ISOLATION_READ_COMMITTED", TransactionDefinition.ISOLATION_READ_COMMITTED,
			"ISOLATION_REPEATABLE_READ", TransactionDefinition.ISOLATION_REPEATABLE_READ,
			"ISOLATION_SERIALIZABLE", TransactionDefinition.ISOLATION_SERIALIZABLE
		);


	/**
	 * 支持隔离级别常量的整数值以及 {@link org.springframework.transaction.TransactionDefinition
	 * TransactionDefinition interface} 上定义的隔离级别名称。
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

	/**
	 * 方法 `determineCurrentLookupKey`：完成本类中与「determine Current Lookup Key」相关的职责。
	 */
	@Override
	protected @Nullable Object determineCurrentLookupKey() {
		return TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
	}

}
