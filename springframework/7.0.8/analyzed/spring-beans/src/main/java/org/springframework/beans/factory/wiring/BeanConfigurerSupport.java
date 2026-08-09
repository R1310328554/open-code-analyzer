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

package org.springframework.beans.factory.wiring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Bean 配置器的便捷基类：可对对象（无论其如何创建）执行依赖注入。
 * 通常由 AspectJ 切面子类化使用。
 *
 * <p>子类可能还需要自定义元数据解析策略，即 {@link BeanWiringInfoResolver} 接口。
 * 默认实现查找与全限定类名同名的 Bean（这也是 Spring XML 中未指定 {@code id} 属性时的默认 Bean 名）。
 *
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @since 2.0
 * @see #setBeanWiringInfoResolver
 * @see ClassNameBeanWiringInfoResolver
 */
public class BeanConfigurerSupport implements BeanFactoryAware, InitializingBean, DisposableBean {

	/** 子类可用的日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 用于解析 Bean 装配元数据的解析器。 */
	private volatile @Nullable BeanWiringInfoResolver beanWiringInfoResolver;

	/** 执行装配时使用的可配置 Bean 工厂。 */
	private volatile @Nullable ConfigurableListableBeanFactory beanFactory;


	/**
	 * 设置要使用的 {@link BeanWiringInfoResolver}。
	 * <p>默认行为是查找与类名同名的 Bean。也可考虑使用注解驱动的 Bean 装配。
	 * @see ClassNameBeanWiringInfoResolver
	 * @see org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver
	 */
	public void setBeanWiringInfoResolver(BeanWiringInfoResolver beanWiringInfoResolver) {
		Assert.notNull(beanWiringInfoResolver, "BeanWiringInfoResolver must not be null");
		this.beanWiringInfoResolver = beanWiringInfoResolver;
	}

	/**
	 * 设置本切面执行 Bean 配置所依赖的 {@link BeanFactory}。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory clbf)) {
			throw new IllegalArgumentException(
				"Bean configurer aspect needs to run in a ConfigurableListableBeanFactory: " + beanFactory);
		}
		this.beanFactory = clbf;
		if (this.beanWiringInfoResolver == null) {
			this.beanWiringInfoResolver = createDefaultBeanWiringInfoResolver();
		}
	}

	/**
	 * 创建未显式指定时使用的默认 {@link BeanWiringInfoResolver}。
	 * <p>默认实现构造 {@link ClassNameBeanWiringInfoResolver}。
	 * @return 默认 BeanWiringInfoResolver（永不为 {@code null}）
	 */
	protected @Nullable BeanWiringInfoResolver createDefaultBeanWiringInfoResolver() {
		return new ClassNameBeanWiringInfoResolver();
	}

	/**
	 * 校验 {@link BeanFactory} 是否已设置。
	 */
	@Override
	public void afterPropertiesSet() {
		Assert.notNull(this.beanFactory, "BeanFactory must be set");
	}

	/**
	 * 容器销毁时释放对 {@link BeanFactory} 和 {@link BeanWiringInfoResolver} 的引用。
	 */
	@Override
	public void destroy() {
		this.beanFactory = null;
		this.beanWiringInfoResolver = null;
	}


	/**
	 * 配置 Bean 实例。
	 * <p>子类可覆盖以提供自定义配置逻辑。通常由切面在切点匹配到的所有 Bean 实例上调用。
	 * @param beanInstance 待配置的 Bean 实例（<b>不得</b>为 {@code null}）
	 */
	public void configureBean(Object beanInstance) {
		if (this.beanFactory == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("BeanFactory has not been set on " + ClassUtils.getShortName(getClass()) + ": " +
						"Make sure this configurer runs in a Spring container. Unable to configure bean of type [" +
						ClassUtils.getDescriptiveType(beanInstance) + "]. Proceeding without injection.");
			}
			return;
		}

		BeanWiringInfoResolver bwiResolver = this.beanWiringInfoResolver;
		Assert.state(bwiResolver != null, "No BeanWiringInfoResolver available");
		BeanWiringInfo bwi = bwiResolver.resolveWiringInfo(beanInstance);
		if (bwi == null) {
			// 无装配元数据则跳过
			return;
		}


		ConfigurableListableBeanFactory beanFactory = this.beanFactory;
		Assert.state(beanFactory != null, "No BeanFactory available");
		try {
			String beanName = bwi.getBeanName();
			if (bwi.indicatesAutowiring() || (bwi.isDefaultBeanName() && beanName != null &&
					!beanFactory.containsBean(beanName))) {
				// 执行自动装配（同时应用标准工厂/后处理器回调）
				beanFactory.autowireBeanProperties(beanInstance, bwi.getAutowireMode(), bwi.getDependencyCheck());
				beanFactory.initializeBean(beanInstance, (beanName != null ? beanName : ""));
			}
			else {
				// 按指定 Bean 定义执行显式装配
				beanFactory.configureBean(beanInstance, (beanName != null ? beanName : ""));
			}
		}
		catch (BeanCreationException ex) {
			Throwable rootCause = ex.getMostSpecificCause();
			if (rootCause instanceof BeanCurrentlyInCreationException bce) {
				String bceBeanName = bce.getBeanName();
				if (bceBeanName != null && beanFactory.isCurrentlyInCreation(bceBeanName)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Failed to create target bean '" + bce.getBeanName() +
								"' while configuring object of type [" + beanInstance.getClass().getName() +
								"] - probably due to a circular reference. This is a common startup situation " +
								"and usually not fatal. Proceeding without injection. Original exception: " + ex);
					}
					return;
				}
			}
			throw ex;
		}
	}

}
