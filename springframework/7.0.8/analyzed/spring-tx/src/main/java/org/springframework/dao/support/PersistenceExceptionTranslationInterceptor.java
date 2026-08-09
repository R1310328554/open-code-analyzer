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

package org.springframework.dao.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 基于给定 PersistenceExceptionTranslator 提供持久化异常转换的
 * AOP Alliance MethodInterceptor。
 *
 * <p>委托给定 {@link PersistenceExceptionTranslator} 将抛出的 RuntimeException
 * 转换为 Spring 的 DataAccessException 层次结构（若适用）。
 * 若所涉 RuntimeException 在目标方法上声明，则始终原样传播（不应用转换）。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see PersistenceExceptionTranslator
 */
public class PersistenceExceptionTranslationInterceptor
		implements MethodInterceptor, BeanFactoryAware, InitializingBean {

	private volatile @Nullable PersistenceExceptionTranslator persistenceExceptionTranslator;

	private boolean alwaysTranslate = false;

	private @Nullable ListableBeanFactory beanFactory;


	/**
	 * 创建新的 PersistenceExceptionTranslationInterceptor。
	 * 之后须配置 PersistenceExceptionTranslator。
	 * @see #setPersistenceExceptionTranslator
	 */
	public PersistenceExceptionTranslationInterceptor() {
	}

	/**
	 * 为给定 PersistenceExceptionTranslator 创建新的
	 * PersistenceExceptionTranslationInterceptor。
	 * @param pet 要使用的 PersistenceExceptionTranslator
	 */
	public PersistenceExceptionTranslationInterceptor(PersistenceExceptionTranslator pet) {
		Assert.notNull(pet, "PersistenceExceptionTranslator must not be null");
		this.persistenceExceptionTranslator = pet;
	}

	/**
	 * 创建新的 PersistenceExceptionTranslationInterceptor，
	 * 自动检测给定 BeanFactory 中的 PersistenceExceptionTranslator。
	 * @param beanFactory 用于获取所有 PersistenceExceptionTranslator 的 ListableBeanFactory
	 */
	public PersistenceExceptionTranslationInterceptor(ListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
		this.beanFactory = beanFactory;
	}


	/**
	 * 指定要使用的 PersistenceExceptionTranslator。
	 * <p>默认为自动检测所在 BeanFactory 中所有 PersistenceExceptionTranslator，
	 * 以链式方式使用。
	 * @see #detectPersistenceExceptionTranslators
	 */
	public void setPersistenceExceptionTranslator(PersistenceExceptionTranslator pet) {
		this.persistenceExceptionTranslator = pet;
	}

	/**
	 * 指定是否始终转换异常（"true"），或在已声明时抛出原始异常（"false"），
	 * 即源方法签名的异常声明允许抛出原始异常时。
	 * <p>默认为 "false"。设为 "true" 可始终转换适用异常，
	 * 不受源方法签名影响。
	 * <p>注意，源方法不必声明特定异常。
	 * 任何基类均可，甚至 {@code throws Exception}：只要源方法显式声明兼容异常，
	 * 原始异常将被重新抛出。若希望任何情况下都避免抛出原始异常，
	 * 请将本标志设为 "true"。
	 */
	public void setAlwaysTranslate(boolean alwaysTranslate) {
		this.alwaysTranslate = alwaysTranslate;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (this.persistenceExceptionTranslator == null) {
			// No explicit exception translator specified - perform autodetection.
			if (!(beanFactory instanceof ListableBeanFactory lbf)) {
				throw new IllegalArgumentException(
						"Cannot use PersistenceExceptionTranslator autodetection without ListableBeanFactory");
			}
			this.beanFactory = lbf;
		}
	}

	@Override
	public void afterPropertiesSet() {
		if (this.persistenceExceptionTranslator == null && this.beanFactory == null) {
			throw new IllegalArgumentException("Property 'persistenceExceptionTranslator' is required");
		}
	}


	@Override
	public @Nullable Object invoke(MethodInvocation mi) throws Throwable {
		try {
			return mi.proceed();
		}
		catch (RuntimeException ex) {
			// Let it throw raw if the type of the exception is on the throws clause of the method.
			if (!this.alwaysTranslate && ReflectionUtils.declaresException(mi.getMethod(), ex.getClass())) {
				throw ex;
			}
			else {
				PersistenceExceptionTranslator translator = this.persistenceExceptionTranslator;
				if (translator == null) {
					Assert.state(this.beanFactory != null,
							"Cannot use PersistenceExceptionTranslator autodetection without ListableBeanFactory");
					try {
						translator = detectPersistenceExceptionTranslators(this.beanFactory);
					}
					catch (BeanCreationNotAllowedException ex2) {
						// Cannot create PersistenceExceptionTranslator bean on shutdown:
						// fall back to rethrowing original exception without translation
						throw ex;
					}
					this.persistenceExceptionTranslator = translator;
				}
				throw DataAccessUtils.translateIfNecessary(ex, translator);
			}
		}
	}

	/**
	 * 检测给定 BeanFactory 中所有 PersistenceExceptionTranslator。
	 * @param bf 用于获取 PersistenceExceptionTranslator 的 ListableBeanFactory
	 * @return 链式 PersistenceExceptionTranslator，组合给定 Bean 工厂中找到的所有
	 * PersistenceExceptionTranslator
	 * @see ChainedPersistenceExceptionTranslator
	 */
	protected PersistenceExceptionTranslator detectPersistenceExceptionTranslators(ListableBeanFactory bf) {
		// Find all translators, being careful not to activate FactoryBeans.
		ChainedPersistenceExceptionTranslator cpet = new ChainedPersistenceExceptionTranslator();
		bf.getBeanProvider(PersistenceExceptionTranslator.class, false).orderedStream().forEach(cpet::addDelegate);
		return cpet;
	}

}
