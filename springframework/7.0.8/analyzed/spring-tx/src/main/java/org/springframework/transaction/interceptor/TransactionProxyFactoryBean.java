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

package org.springframework.transaction.interceptor;

import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 用于简化声明式事务处理的代理工厂 Bean。
 * 这是标准 AOP {@link org.springframework.aop.framework.ProxyFactoryBean}
 * 配合独立 {@link TransactionInterceptor} 定义的便捷替代方案。
 *
 * <p><strong>历史说明：</strong>本类最初用于典型声明式事务划分场景：
 * 即用事务代理包装单例目标对象，代理目标实现的所有接口。
 * 但在 Spring 2.0 及之后，此处功能已被更便捷的 {@code tx:} XML 命名空间取代。
 * 请参阅 Spring 参考文档中的
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#transaction-declarative">声明式事务管理</a>
 * 章节以了解现代 Spring 应用中的事务管理选项。因此，<strong>用户应优先使用
 * {@code tx:} XML 命名空间以及
 * @{@link org.springframework.transaction.annotation.Transactional Transactional}
 * 与 @{@link org.springframework.transaction.annotation.EnableTransactionManagement
 * EnableTransactionManagement} 注解。</strong>
 *
 * <p>须指定三个主要属性：
 * <ul>
 * <li>"transactionManager"：要使用的 {@link PlatformTransactionManager} 实现
 * （例如 {@link org.springframework.transaction.jta.JtaTransactionManager} 实例）
 * <li>"target"：要为其创建事务代理的目标对象
 * <li>"transactionAttributes"：按目标方法名（或方法名模式）配置的事务属性
 * （例如传播行为与 "readOnly" 标志）
 * </ul>
 *
 * <p>若未显式设置 "transactionManager" 且本 {@link FactoryBean}
 * 运行于 {@link ListableBeanFactory} 中，将从 {@link BeanFactory} 获取
 * 唯一匹配的 {@link PlatformTransactionManager} Bean。
 *
 * <p>与 {@link TransactionInterceptor} 不同，事务属性以 Properties 形式指定，
 * 方法名为键、事务属性描述符为值。方法名始终应用于目标类。
 *
 * <p>内部使用 {@link TransactionInterceptor} 实例，但本类用户无需关心。
 * 可选指定方法切点以条件性调用底层 {@link TransactionInterceptor}。
 *
 * <p>可设置 "preInterceptors" 与 "postInterceptors" 属性以添加额外拦截器，
 * 例如 {@link org.springframework.aop.interceptor.PerformanceMonitorInterceptor}。
 *
 * <p><b>提示：</b>本类常与父子 Bean 定义配合使用。
 * 通常在抽象父 Bean 定义中配置事务管理器与默认事务属性（方法名模式），
 * 再为具体目标对象派生子 Bean 定义，从而将每个 Bean 的定义工作量降至最低。
 *
 * <pre class="code">
 * &lt;bean id="baseTransactionProxy" class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean"
 *     abstract="true"&gt;
 *   &lt;property name="transactionManager" ref="transactionManager"/&gt;
 *   &lt;property name="transactionAttributes"&gt;
 *     &lt;props&gt;
 *       &lt;prop key="insert*"&gt;PROPAGATION_REQUIRED&lt;/prop&gt;
 *       &lt;prop key="update*"&gt;PROPAGATION_REQUIRED&lt;/prop&gt;
 *       &lt;prop key="*"&gt;PROPAGATION_REQUIRED,readOnly&lt;/prop&gt;
 *     &lt;/props&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="myProxy" parent="baseTransactionProxy"&gt;
 *   &lt;property name="target" ref="myTarget"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="yourProxy" parent="baseTransactionProxy"&gt;
 *   &lt;property name="target" ref="yourTarget"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Juergen Hoeller
 * @author Dmitriy Kopylenko
 * @author Rod Johnson
 * @author Chris Beams
 * @since 21.08.2003
 * @see #setTransactionManager
 * @see #setTarget
 * @see #setTransactionAttributes
 * @see TransactionInterceptor
 * @see org.springframework.aop.framework.ProxyFactoryBean
 */
@SuppressWarnings("serial")
public class TransactionProxyFactoryBean extends AbstractSingletonProxyFactoryBean
		implements BeanFactoryAware {

	private final TransactionInterceptor transactionInterceptor = new TransactionInterceptor();

	private @Nullable Pointcut pointcut;


	/**
	 * 设置默认事务管理器。它将执行实际事务管理：
	 * 本类只是调用它的方式。
	 * @see TransactionInterceptor#setTransactionManager
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionInterceptor.setTransactionManager(transactionManager);
	}

	/**
	 * 设置 Properties，以方法名为键、事务属性描述符
	 * （通过 TransactionAttributeEditor 解析）为值：
	 * 例如 key = "myMethod"，value = "PROPAGATION_REQUIRED,readOnly"。
	 * <p>注意：方法名始终应用于目标类，无论定义在接口还是类本身。
	 * <p>内部将根据给定 Properties 创建 NameMatchTransactionAttributeSource。
	 * @see #setTransactionAttributeSource
	 * @see TransactionInterceptor#setTransactionAttributes
	 * @see TransactionAttributeEditor
	 * @see NameMatchTransactionAttributeSource
	 */
	public void setTransactionAttributes(Properties transactionAttributes) {
		this.transactionInterceptor.setTransactionAttributes(transactionAttributes);
	}

	/**
	 * 设置用于查找事务属性的事务属性源。
	 * 若指定 String 属性值，PropertyEditor 将从该值创建 MethodMapTransactionAttributeSource。
	 * @see #setTransactionAttributes
	 * @see TransactionInterceptor#setTransactionAttributeSource
	 * @see TransactionAttributeSourceEditor
	 * @see MethodMapTransactionAttributeSource
	 * @see NameMatchTransactionAttributeSource
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
	 */
	public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
		this.transactionInterceptor.setTransactionAttributeSource(transactionAttributeSource);
	}

	/**
	 * 设置切点，即可根据传入的方法与属性条件性调用 TransactionInterceptor 的 Bean。
	 * 注意：额外拦截器始终会被调用。
	 * @see #setPreInterceptors
	 * @see #setPostInterceptors
	 */
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	/**
	 * 本回调可选：若在 BeanFactory 中运行且未显式设置事务管理器，
	 * 将从 BeanFactory 获取唯一匹配的 {@link PlatformTransactionManager} Bean。
	 * @see org.springframework.beans.factory.BeanFactory#getBean(Class)
	 * @see org.springframework.transaction.PlatformTransactionManager
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.transactionInterceptor.setBeanFactory(beanFactory);
	}


	/**
	 * 为本 FactoryBean 的 TransactionInterceptor 创建 Advisor。
	 */
	@Override
	protected Object createMainInterceptor() {
		this.transactionInterceptor.afterPropertiesSet();
		if (this.pointcut != null) {
			return new DefaultPointcutAdvisor(this.pointcut, this.transactionInterceptor);
		}
		else {
			// 依赖默认切点。
			return new TransactionAttributeSourceAdvisor(this.transactionInterceptor);
		}
	}

	/**
	 * 自 4.2 起，本方法将 {@link TransactionalProxy} 加入代理接口集合，
	 * 以避免重复处理事务元数据。
	 */
	@Override
	protected void postProcessProxyFactory(ProxyFactory proxyFactory) {
		proxyFactory.addInterface(TransactionalProxy.class);
	}

}
