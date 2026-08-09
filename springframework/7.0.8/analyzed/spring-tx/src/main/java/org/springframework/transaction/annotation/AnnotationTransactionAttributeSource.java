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

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * {@link org.springframework.transaction.interceptor.TransactionAttributeSource}
 * 接口的实现，用于处理注解来源的事务元数据。
 *
 * <p>本类读取 Spring {@link Transactional @Transactional} 注解，
 * 并向 Spring 事务基础设施暴露对应事务属性。
 * 也支持 JTA {@link jakarta.transaction.Transactional}
 * 和 EJB {@link jakarta.ejb.TransactionAttribute} 注解（若存在）。
 *
 * <p>本类也可作为自定义 TransactionAttributeSource 的基类，
 * 或通过 {@link TransactionAnnotationParser} 策略定制。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 * @see Transactional
 * @see TransactionAnnotationParser
 * @see SpringTransactionAnnotationParser
 * @see Ejb3TransactionAnnotationParser
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#setTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean#setTransactionAttributeSource
 */
@SuppressWarnings("serial")
public class AnnotationTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource
		implements Serializable {

	private static final boolean JTA_PRESENT;

	private static final boolean EJB_3_PRESENT;

	static {
		ClassLoader classLoader = AnnotationTransactionAttributeSource.class.getClassLoader();
		JTA_PRESENT = ClassUtils.isPresent("jakarta.transaction.Transactional", classLoader);
		EJB_3_PRESENT = ClassUtils.isPresent("jakarta.ejb.TransactionAttribute", classLoader);
	}

	private final Set<TransactionAnnotationParser> annotationParsers;

	private boolean publicMethodsOnly = true;

	private @Nullable Set<RollbackRuleAttribute> defaultRollbackRules;


	/**
	 * 创建默认 AnnotationTransactionAttributeSource，
	 * 支持带 {@code Transactional} 注解或
	 * EJB3 {@link jakarta.ejb.TransactionAttribute} 注解的 public 方法。
	 */
	public AnnotationTransactionAttributeSource() {
		if (JTA_PRESENT || EJB_3_PRESENT) {
			this.annotationParsers = CollectionUtils.newLinkedHashSet(3);
			this.annotationParsers.add(new SpringTransactionAnnotationParser());
			if (JTA_PRESENT) {
				this.annotationParsers.add(new JtaTransactionAnnotationParser());
			}
			if (EJB_3_PRESENT) {
				this.annotationParsers.add(new Ejb3TransactionAnnotationParser());
			}
		}
		else {
			this.annotationParsers = Collections.singleton(new SpringTransactionAnnotationParser());
		}
	}

	/**
	 * 创建自定义 AnnotationTransactionAttributeSource，
	 * 支持带 {@code Transactional} 或
	 * EJB3 {@link jakarta.ejb.TransactionAttribute} 注解的方法。
	 * @param publicMethodsOnly 是否仅支持带 {@code Transactional} 的 public 方法
	 *（通常用于基于代理的 AOP），或也支持 protected/private 方法
	 *（通常用于 AspectJ 类织入）
	 * @see #setPublicMethodsOnly
	 */
	public AnnotationTransactionAttributeSource(boolean publicMethodsOnly) {
		this();
		this.publicMethodsOnly = publicMethodsOnly;
	}

	/**
	 * 创建自定义 AnnotationTransactionAttributeSource。
	 * @param annotationParser 使用的 TransactionAnnotationParser
	 */
	public AnnotationTransactionAttributeSource(TransactionAnnotationParser annotationParser) {
		Assert.notNull(annotationParser, "TransactionAnnotationParser must not be null");
		this.annotationParsers = Collections.singleton(annotationParser);
	}

	/**
	 * 创建自定义 AnnotationTransactionAttributeSource。
	 * @param annotationParsers 使用的 TransactionAnnotationParser 集合
	 */
	public AnnotationTransactionAttributeSource(TransactionAnnotationParser... annotationParsers) {
		Assert.notEmpty(annotationParsers, "At least one TransactionAnnotationParser needs to be specified");
		this.annotationParsers = Set.of(annotationParsers);
	}


	/**
	 * 设置事务方法是否必须为 public。
	 * <p>默认为 {@code true}。
	 * @since 6.2
	 * @see #AnnotationTransactionAttributeSource(boolean)
	 */
	public void setPublicMethodsOnly(boolean publicMethodsOnly) {
		this.publicMethodsOnly = publicMethodsOnly;
	}

	/**
	 * 添加默认回滚规则，应用于本源返回的所有基于规则的事务属性。
	 * <p>默认情况下，非受检异常触发回滚，受检异常不触发。
	 * 默认规则可覆盖此行为，同时仍尊重事务属性中的自定义规则。
	 * @param rollbackRule 覆盖默认行为的回滚规则，
	 * 例如 {@link RollbackRuleAttribute#ROLLBACK_ON_ALL_EXCEPTIONS}
	 * @since 6.2
	 * @see RuleBasedTransactionAttribute#getRollbackRules()
	 * @see EnableTransactionManagement#rollbackOn()
	 * @see Transactional#rollbackFor()
	 * @see Transactional#noRollbackFor()
	 */
	public void addDefaultRollbackRule(RollbackRuleAttribute rollbackRule) {
		if (this.defaultRollbackRules == null) {
			this.defaultRollbackRules = new LinkedHashSet<>();
		}
		this.defaultRollbackRules.add(rollbackRule);
	}


	@Override
	public boolean isCandidateClass(Class<?> targetClass) {
		for (TransactionAnnotationParser parser : this.annotationParsers) {
			if (parser.isCandidateClass(targetClass)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected @Nullable TransactionAttribute findTransactionAttribute(Class<?> clazz) {
		return determineTransactionAttribute(clazz);
	}

	@Override
	protected @Nullable TransactionAttribute findTransactionAttribute(Method method) {
		return determineTransactionAttribute(method);
	}

	/**
	 * 确定给定方法或类的事务属性。
	 * <p>本实现委托已配置的
	 * {@link TransactionAnnotationParser TransactionAnnotationParsers}
	 * 将已知注解解析为 Spring 元数据属性类。
	 * 若非事务性则返回 {@code null}。
	 * <p>可覆盖以支持携带事务元数据的自定义注解。
	 * @param element 带注解的方法或类
	 * @return 配置的事务属性，未找到则 {@code null}
	 */
	protected @Nullable TransactionAttribute determineTransactionAttribute(AnnotatedElement element) {
		for (TransactionAnnotationParser parser : this.annotationParsers) {
			TransactionAttribute attr = parser.parseTransactionAnnotation(element);
			if (attr != null) {
				if (this.defaultRollbackRules != null && attr instanceof RuleBasedTransactionAttribute ruleAttr) {
					ruleAttr.getRollbackRules().addAll(this.defaultRollbackRules);
				}
				return attr;
			}
		}
		return null;
	}

	/**
	 * 默认情况下，仅 public 方法可声明为事务性。
	 * @see #setPublicMethodsOnly
	 */
	@Override
	protected boolean allowPublicMethodsOnly() {
		return this.publicMethodsOnly;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof AnnotationTransactionAttributeSource otherTas &&
				this.annotationParsers.equals(otherTas.annotationParsers) &&
				this.publicMethodsOnly == otherTas.publicMethodsOnly));
	}

	@Override
	public int hashCode() {
		return this.annotationParsers.hashCode();
	}

}
