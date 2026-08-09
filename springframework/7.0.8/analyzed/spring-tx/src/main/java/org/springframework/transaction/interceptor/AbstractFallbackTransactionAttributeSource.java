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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.MethodClassKey;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringValueResolver;

/**
 * {@link TransactionAttributeSource} 的抽象实现，缓存方法属性
 * 并实现回退策略：1. 特定目标方法；2. 目标类；3. 声明方法；4. 声明类/接口。
 *
 * <p>若目标方法未关联事务属性，默认使用目标类的事务属性。
 * 目标方法关联的任何事务属性完全覆盖类级事务属性。
 * 若目标类上未找到，将检查调用方法所经过的接口（JDK 代理情况下）。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class AbstractFallbackTransactionAttributeSource
		implements TransactionAttributeSource, EmbeddedValueResolverAware {

	/**
	 * 缓存中持有的规范值，表示未找到此方法的事务属性，
	 * 且无需再次查找。
	 */
	@SuppressWarnings("serial")
	private static final TransactionAttribute NULL_TRANSACTION_ATTRIBUTE = new DefaultTransactionAttribute() {
		@Override
		public String toString() {
			return "null";
		}
	};


	/**
	 * 供子类使用的日志记录器。
	 * <p>由于此基类未标记 Serializable，序列化后日志记录器将重新创建
	 * ——前提是具体子类可序列化。
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	private transient @Nullable StringValueResolver embeddedValueResolver;

	/**
	 * TransactionAttribute 缓存，以特定目标类上的方法为键。
	 * <p>由于此基类未标记 Serializable，序列化后缓存将重新创建
	 * ——前提是具体子类可序列化。
	 */
	private final Map<Object, TransactionAttribute> attributeCache = new ConcurrentHashMap<>(1024);


	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}


	@Override
	public boolean hasTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		return (getTransactionAttribute(method, targetClass, false) != null);
	}

	@Override
	public @Nullable TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		return getTransactionAttribute(method, targetClass, true);
	}

	/**
	 * 确定此方法调用的事务属性。
	 * <p>若未找到方法属性，默认使用类的事务属性。
	 * @param method 当前调用的方法（永不为 {@code null}）
	 * @param targetClass 此调用的目标类（可为 {@code null}）
	 * @param cacheNull 是否也应缓存 {@code null} 结果
	 * @return 此方法的事务属性，若方法非事务性则为 {@code null}
	 */
	private @Nullable TransactionAttribute getTransactionAttribute(
			Method method, @Nullable Class<?> targetClass, boolean cacheNull) {

		if (ReflectionUtils.isObjectMethod(method)) {
			return null;
		}

		Object cacheKey = getCacheKey(method, targetClass);
		TransactionAttribute cached = this.attributeCache.get(cacheKey);

		if (cached != null) {
			return (cached != NULL_TRANSACTION_ATTRIBUTE ? cached : null);
		}
		else {
			TransactionAttribute txAttr = computeTransactionAttribute(method, targetClass);
			if (txAttr != null) {
				String methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
				if (txAttr instanceof DefaultTransactionAttribute dta) {
					dta.setDescriptor(methodIdentification);
					dta.resolveAttributeStrings(this.embeddedValueResolver);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Adding transactional method '" + methodIdentification + "' with attribute: " + txAttr);
				}
				this.attributeCache.put(cacheKey, txAttr);
			}
			else if (cacheNull) {
				this.attributeCache.put(cacheKey, NULL_TRANSACTION_ATTRIBUTE);
			}
			return txAttr;
		}
	}

	/**
	 * 为给定方法和目标类确定缓存键。
	 * <p>不得为重载方法产生相同键。
	 * 必须为同一方法的不同实例产生相同键。
	 * @param method 方法（永不为 {@code null}）
	 * @param targetClass 目标类（可为 {@code null}）
	 * @return 缓存键（永不为 {@code null}）
	 */
	protected Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
		return new MethodClassKey(method, targetClass);
	}

	/**
	 * 与 {@link #getTransactionAttribute} 签名相同，但不缓存结果。
	 * {@link #getTransactionAttribute} 实际上是此方法带缓存的装饰器。
	 * <p>自 4.1.8 起，此方法可被覆盖。
	 * @since 4.1.8
	 * @see #getTransactionAttribute
	 */
	protected @Nullable TransactionAttribute computeTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		// 按配置不允许非 public 方法。
		if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
			return null;
		}
		// 跳过 BeanFactoryAware 上的 setBeanFactory 方法。
		if (method.getDeclaringClass() == BeanFactoryAware.class) {
			return null;
		}

		// 方法可能在接口上，但我们需要来自目标类的属性。
		// 若目标类为 null，方法将保持不变。
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

		// 首先尝试目标类中的方法。
		TransactionAttribute txAttr = findTransactionAttribute(specificMethod);
		if (txAttr != null) {
			return txAttr;
		}

		// 其次尝试目标类上的事务属性。
		txAttr = findTransactionAttribute(specificMethod.getDeclaringClass());
		if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
			return txAttr;
		}

		if (specificMethod != method) {
			// 回退为查看原始方法。
			txAttr = findTransactionAttribute(method);
			if (txAttr != null) {
				return txAttr;
			}
			// 最后回退为原始方法的类。
			txAttr = findTransactionAttribute(method.getDeclaringClass());
			if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
				return txAttr;
			}
		}

		return null;
	}


	/**
	 * 子类需实现此方法以返回给定类的事务属性（若有）。
	 * @param clazz 要检索属性的类
	 * @return 与此类关联的全部事务属性，若无则为 {@code null}
	 */
	protected abstract @Nullable TransactionAttribute findTransactionAttribute(Class<?> clazz);

	/**
	 * 子类需实现此方法以返回给定方法的事务属性（若有）。
	 * @param method 要检索属性的方法
	 * @return 与此方法关联的全部事务属性，若无则为 {@code null}
	 */
	protected abstract @Nullable TransactionAttribute findTransactionAttribute(Method method);

	/**
	 * 是否仅允许 public 方法具有事务语义？
	 * <p>默认实现返回 {@code false}。
	 */
	protected boolean allowPublicMethodsOnly() {
		return false;
	}

}
