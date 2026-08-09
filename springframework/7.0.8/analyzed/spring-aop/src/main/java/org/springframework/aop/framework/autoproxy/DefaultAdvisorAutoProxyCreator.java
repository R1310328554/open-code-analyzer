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

package org.springframework.aop.framework.autoproxy;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanNameAware;

/**
 * 基于当前 {@code BeanFactory} 中所有候选 {@code Advisor}
 * 创建 AOP 代理的 {@code BeanPostProcessor} 实现。本类完全通用，
 * 不含处理特定切面（如池化切面）的特殊代码。
 *
 * <p>可通过将 {@code usePrefix} 设为 true 过滤 Advisor——
 * 例如在同一工厂中使用多个此类后处理器——
 * 此时仅使用以 DefaultAdvisorAutoProxyCreator 的 Bean 名称
 * 加点开头（如 "aapc."）的 Advisor。
 * 可通过 {@code advisorBeanNamePrefix} 属性更改默认前缀，
 * 此情况下分隔符（.）同样适用。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 */
@SuppressWarnings("serial")
public class DefaultAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator implements BeanNameAware {

	/** Bean 名称前缀与剩余部分之间的分隔符。 */
	public static final String SEPARATOR = ".";


	private boolean usePrefix = false;

	private @Nullable String advisorBeanNamePrefix;


	/**
	 * 设置是否仅包含 Bean 名称带特定前缀的 Advisor。
	 * <p>默认为 {@code false}，包含所有 {@code Advisor} 类型 Bean。
	 * @see #setAdvisorBeanNamePrefix
	 */
	public void setUsePrefix(boolean usePrefix) {
		this.usePrefix = usePrefix;
	}

	/**
	 * 返回是否仅包含 Bean 名称带特定前缀的 Advisor。
	 */
	public boolean isUsePrefix() {
		return this.usePrefix;
	}

	/**
	 * 设置使 Bean 被本对象纳入自动代理的 Bean 名称前缀。
	 * 应设置此前缀以避免循环引用。默认值为本对象 Bean 名称加点。
	 * @param advisorBeanNamePrefix 排除前缀
	 */
	public void setAdvisorBeanNamePrefix(@Nullable String advisorBeanNamePrefix) {
		this.advisorBeanNamePrefix = advisorBeanNamePrefix;
	}

	/**
	 * 返回使 Bean 被本对象纳入自动代理的 Bean 名称前缀。
	 */
	public @Nullable String getAdvisorBeanNamePrefix() {
		return this.advisorBeanNamePrefix;
	}

	@Override
	public void setBeanName(String name) {
		// 若尚未设置基础设施 Bean 名称前缀，则覆盖它。
		if (this.advisorBeanNamePrefix == null) {
			this.advisorBeanNamePrefix = name + SEPARATOR;
		}
	}


	/**
	 * 若已激活，将带指定前缀的 {@code Advisor} Bean 视为合格。
	 * @see #setUsePrefix
	 * @see #setAdvisorBeanNamePrefix
	 */
	@Override
	protected boolean isEligibleAdvisorBean(String beanName) {
		if (!isUsePrefix()) {
			return true;
		}
		String prefix = getAdvisorBeanNamePrefix();
		return (prefix != null && beanName.startsWith(prefix));
	}

}
