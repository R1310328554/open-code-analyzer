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

package org.springframework.aop.aspectj.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.reflect.PerClauseKind;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.Assert;

/**
 * 用于从 BeanFactory 检索 @AspectJ beans 并基于它们构建 Spring Advisor 的帮助程序，以与自动代理一起使用。
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see AnnotationAwareAspectJAutoProxyCreator
 */
public class BeanFactoryAspectJAdvisorsBuilder {

	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(BeanFactoryAspectJAdvisorsBuilder.class);

	/** 底层 BeanFactory 引用。 */
	private final ListableBeanFactory beanFactory;

	/** 工厂相关状态（`advisorFactory`）。 */
	private final AspectJAdvisorFactory advisorFactory;

	/** 名称相关状态（`aspectBeanNames`）。 */
	private volatile @Nullable List<String> aspectBeanNames;

	private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();

	private final Map<String, MetadataAwareAspectInstanceFactory> aspectFactoryCache = new ConcurrentHashMap<>();


	/**
	 * 为给定的 BeanFactory 创建一个新的 BeanFactoryAspectJAdvisorsBuilder。
	 * @param beanFactory 要扫描的 ListableBeanFactory
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory) {
		this(beanFactory, new ReflectiveAspectJAdvisorFactory(beanFactory));
	}

	/**
	 * 为给定的 BeanFactory 创建一个新的 BeanFactoryAspectJAdvisorsBuilder。
	 * @param beanFactory 要扫描的 ListableBeanFactory
	 * @param advisorFactory 用于构建每个 Advisor 的 AspectJAdvisorFactory
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
		Assert.notNull(advisorFactory, "AspectJAdvisorFactory must not be null");
		this.beanFactory = beanFactory;
		this.advisorFactory = advisorFactory;
	}


	/**
	 * 在当前 bean 工厂中查找带有 AspectJ 注释的方面 bean，并返回到代表它们的 Spring AOP Advisor 列表。 <p>为每个 AspectJ
	 * 建议方法创建一个 Spring Advisor。
	 * @return {@link org.springframework.aop.Advisor} bean 列表
	 * @see #isEligibleBean
	 */
	public List<Advisor> buildAspectJAdvisors() {
		List<String> aspectNames = this.aspectBeanNames;

		if (aspectNames == null) {
			synchronized (this) {
				aspectNames = this.aspectBeanNames;
				if (aspectNames == null) {
					List<Advisor> advisors = new ArrayList<>();
					aspectNames = new ArrayList<>();
					String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
							this.beanFactory, Object.class, true, false);
					for (String beanName : beanNames) {
						if (!isEligibleBean(beanName)) {
							continue;
						}
						// 我们必须小心，不要急于实例化 bean，因为在本例中它们
						// 将被 Spring 容器缓存，但不会被编织。
						Class<?> beanType = this.beanFactory.getType(beanName, false);
						if (beanType == null) {
							continue;
						}
						if (this.advisorFactory.isAspect(beanType)) {
							try {
								AspectMetadata amd = new AspectMetadata(beanType, beanName);
								if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
									MetadataAwareAspectInstanceFactory factory =
											new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
									List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
									if (this.beanFactory.isSingleton(beanName)) {
										this.advisorsCache.put(beanName, classAdvisors);
									}
									else {
										this.aspectFactoryCache.put(beanName, factory);
									}
									advisors.addAll(classAdvisors);
								}
								else {
									// 每个目标或每个这个。
									if (this.beanFactory.isSingleton(beanName)) {
										throw new IllegalArgumentException("Bean with name '" + beanName +
												"' is a singleton, but aspect instantiation model is not singleton");
									}
									MetadataAwareAspectInstanceFactory factory =
											new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
									this.aspectFactoryCache.put(beanName, factory);
									advisors.addAll(this.advisorFactory.getAdvisors(factory));
								}
								aspectNames.add(beanName);
							}
							catch (IllegalArgumentException | IllegalStateException | AopConfigException ex) {
								if (logger.isDebugEnabled()) {
									logger.debug("Ignoring incompatible aspect [" + beanType.getName() + "]: " + ex);
								}
							}
						}
					}
					this.aspectBeanNames = aspectNames;
					return advisors;
				}
			}
		}

		if (aspectNames.isEmpty()) {
			return Collections.emptyList();
		}
		List<Advisor> advisors = new ArrayList<>();
		for (String aspectName : aspectNames) {
			List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
			if (cachedAdvisors != null) {
				advisors.addAll(cachedAdvisors);
			}
			else {
				MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
				Assert.state(factory != null, "Factory must not be null");
				advisors.addAll(this.advisorFactory.getAdvisors(factory));
			}
		}
		return advisors;
	}

	/**
	 * 返回具有给定名称的方面 bean 是否符合条件。
	 * @param beanName 方面 bean 的名称
	 * @return 该豆子符合条件
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

}
