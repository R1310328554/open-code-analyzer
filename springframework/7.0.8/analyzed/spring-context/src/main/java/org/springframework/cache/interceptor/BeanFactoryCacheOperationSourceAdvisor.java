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

package org.springframework.cache.interceptor;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * 由 {@link CacheOperationSource} 驱动的 Advisor，为可缓存方法织入
 * {@link CacheInterceptor} 通知。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 * @see #setAdviceBeanName
 * @see CacheInterceptor
 */
@SuppressWarnings("serial")
public class BeanFactoryCacheOperationSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {

	/** 根据缓存操作元数据匹配目标方法的切点。 */
	private final CacheOperationSourcePointcut pointcut = new CacheOperationSourcePointcut();


	/**
	 * 设置用于查找缓存操作属性的来源，通常应与
	 * {@link CacheInterceptor} 上配置的来源一致。
	 * @see CacheInterceptor#setCacheOperationSource
	 */
	public void setCacheOperationSource(CacheOperationSource cacheOperationSource) {
		this.pointcut.setCacheOperationSource(cacheOperationSource);
	}

	/**
	 * 设置此切点使用的 {@link ClassFilter}。
	 * 默认为 {@link ClassFilter#TRUE}（匹配所有类）。
	 */
	public void setClassFilter(ClassFilter classFilter) {
		this.pointcut.setClassFilter(classFilter);
	}

	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

}
