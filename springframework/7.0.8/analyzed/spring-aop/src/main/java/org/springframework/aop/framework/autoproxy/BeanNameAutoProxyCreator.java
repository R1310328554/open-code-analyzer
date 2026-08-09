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

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

/**
 * 自动代理创建器，通过名称列表识别要代理的 bean。检查直接匹配、“xxx*”和“*xxx”匹配。
 * <p>配置详细信息请参见父类AbstractAutoProxyCreator的javadoc。通常，您将通过“interceptorNames”属性指定要应用于所有已识别 be
 * an 的拦截器名称列表。
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 10.10.2003
 * @see #setBeanNames
 * @see #isMatch
 * @see #setInterceptorNames
 * @see AbstractAutoProxyCreator
 */
@SuppressWarnings("serial")
public class BeanNameAutoProxyCreator extends AbstractAutoProxyCreator {

	private static final String[] NO_ALIASES = new String[0];

	/** 名称相关状态（`beanNames`）。 */
	private @Nullable List<String> beanNames;


	/**
	 * 设置应自动用代理包装的 Bean
	 * 的名称。名称可以指定以“*”结尾的前缀进行匹配，例如“myBean,tx*”将匹配名为“myBean”的bean以及名称以“tx”开头的所有bean。
	 * <p><b>NOTE:</b> 对于 FactoryBean，只有由 FactoryBean 创建的对象才会被代理。如果您打算代理 FactoryBean
	 * 实例本身（一种罕见的用例），请指定 FactoryBean 的 bean 名称，包括工厂 bean 前缀“&amp;”：例如“&amp;myFactoryBean”。
	 * @see org.springframework.beans.factory.FactoryBean
	 * @see org.springframework.beans.factory.BeanFactory#FACTORY_BEAN_PREFIX
	 */
	public void setBeanNames(String... beanNames) {
		Assert.notEmpty(beanNames, "'beanNames' must not be empty");
		this.beanNames = new ArrayList<>(beanNames.length);
		for (String mappedName : beanNames) {
			this.beanNames.add(mappedName.strip());
		}
	}


	/**
	 * 如果 bean 名称与配置的受支持名称列表中的名称之一匹配，则委托给 {@link
	 * AbstractAutoProxyCreator#getCustomTargetSource(Class, String)}，否则返回 {@code null}。
	 * @since 5.3
	 * @see #setBeanNames(String...)
	 */
	@Override
	protected @Nullable TargetSource getCustomTargetSource(Class<?> beanClass, String beanName) {
		return (isSupportedBeanName(beanClass, beanName) ?
				super.getCustomTargetSource(beanClass, beanName) : null);
	}

	/**
	 * 如果 bean 名称与配置的受支持名称列表中的名称之一匹配，则标识为要代理的 bean。
	 * @see #setBeanNames(String...)
	 */
	@Override
	protected Object @Nullable [] getAdvicesAndAdvisorsForBean(
			Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {

		return (isSupportedBeanName(beanClass, beanName) ?
				PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS : DO_NOT_PROXY);
	}

	/**
	 * 确定给定 bean 类的 bean 名称是否与配置的受支持名称列表中的名称之一匹配。
	 * @param beanClass 要建议的 bean 的类别
	 * @param beanName 豆子的名字
	 * @return true} 如果支持给定的 bean 名称
	 * @see #setBeanNames(String...)
	 */
	private boolean isSupportedBeanName(Class<?> beanClass, String beanName) {
		if (this.beanNames != null) {
			boolean isFactoryBean = FactoryBean.class.isAssignableFrom(beanClass);
			for (String mappedName : this.beanNames) {
				if (isFactoryBean) {
					if (mappedName.isEmpty() || mappedName.charAt(0) != BeanFactory.FACTORY_BEAN_PREFIX_CHAR) {
						continue;
					}
					mappedName = mappedName.substring(1);  // length of '&'
				}
				if (isMatch(beanName, mappedName)) {
					return true;
				}
			}

			BeanFactory beanFactory = getBeanFactory();
			String[] aliases = (beanFactory != null ? beanFactory.getAliases(beanName) : NO_ALIASES);
			for (String alias : aliases) {
				for (String mappedName : this.beanNames) {
					if (isMatch(alias, mappedName)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 确定给定的 bean 名称是否与映射名称匹配。 <p>默认实现检查“xxx*”、“*xxx”和“*xxx*”匹配以及直接相等。可以在子类中重写。
	 * @param beanName 要检查的 bean 名称
	 * @param mappedName 配置的名称列表中的名称
	 * @return 名字匹配
	 * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)
	 */
	protected boolean isMatch(String beanName, String mappedName) {
		return PatternMatchUtils.simpleMatch(mappedName, beanName);
	}

}
