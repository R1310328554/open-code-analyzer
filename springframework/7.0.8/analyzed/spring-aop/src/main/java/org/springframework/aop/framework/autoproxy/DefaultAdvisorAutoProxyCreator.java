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
 * {@code BeanPostProcessor} 实现，根据当前 {@code BeanFactory} 中的所有候选 {@code Advisor} 创建 AOP
 * 代理。这个类是完全通用的；它不包含处理任何特定方面（例如池方面）的特殊代码。
 * <p> 可以过滤掉顾问程序 - 例如，通过将 {@code usePrefix} 属性设置为 true，在同一工厂中使用这种类型的多个后处理器，在这种情况下，仅使用以 Defa
 * ultAdvisorAutoProxyCreator 的 bean 名称开头，后跟一个点（如“aapc.”）的顾问程序。可以通过设置 {@code advisorBeanNam
 * ePrefix} 属性从 bean 名称更改此默认前缀。在这种情况下也将使用分隔符 (.)。
 * @author Rod Johnson
 * @author Rob Harrop
 */
@SuppressWarnings("serial")
public class DefaultAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator implements BeanNameAware {

	/**
	 */
	public static final String SEPARATOR = ".";


	/** `false`：该类的成员状态。 */
	private boolean usePrefix = false;

	/** 名称相关状态（`advisorBeanNamePrefix`）。 */
	private @Nullable String advisorBeanNamePrefix;


	/**
	 * 设置是否仅在 bean 名称中包含具有特定前缀的顾问程序。 <p>Default 是 {@code false}，包括 {@code Advisor} 类型的所有 bean。
	 * @see #setAdvisorBeanNamePrefix
	 */
	public void setUsePrefix(boolean usePrefix) {
		this.usePrefix = usePrefix;
	}

	/**
	 * 返回是否仅在 bean 名称中包含具有特定前缀的顾问程序。
	 */
	public boolean isUsePrefix() {
		return this.usePrefix;
	}

	/**
	 * 设置 bean 名称的前缀，这将使它们包含在该对象的自动代理中。应设置此前缀以避免循环引用。默认值是该对象的bean名称+一个点。
	 * @param advisorBeanNamePrefix 排除前缀
	 */
	public void setAdvisorBeanNamePrefix(@Nullable String advisorBeanNamePrefix) {
		this.advisorBeanNamePrefix = advisorBeanNamePrefix;
	}

	/**
	 * 返回 bean 名称的前缀，该前缀将导致该对象将其包含在内以进行自动代理。
	 */
	public @Nullable String getAdvisorBeanNamePrefix() {
		return this.advisorBeanNamePrefix;
	}

	/**
	 * 设置 Bean Name（`BeanName`）。
	 */
	@Override
	public void setBeanName(String name) {
		// 如果未设置基础设施 bean 名称前缀，则覆盖它。
		if (this.advisorBeanNamePrefix == null) {
			this.advisorBeanNamePrefix = name + SEPARATOR;
		}
	}


	/**
	 * 如果激活，则将具有指定前缀的 {@code Advisor} bean 视为合格。
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
