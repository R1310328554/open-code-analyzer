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
 * 通过名称列表识别要代理 Bean 的自动代理创建器。
 * 支持直接匹配、"xxx*" 和 "*xxx" 模式。
 *
 * <p>配置细节请参阅父类 AbstractAutoProxyCreator 的 JavaDoc。
 * 通常通过 "interceptorNames" 属性为所有识别到的 Bean 指定拦截器名称列表。
 *
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

	private @Nullable List<String> beanNames;


	/**
	 * 设置应自动被代理包装的 Bean 名称。
	 * 名称可通过以 "*" 结尾指定前缀匹配，例如 "myBean,tx*"
	 * 将匹配名为 "myBean" 的 Bean 及所有以 "tx" 开头的 Bean。
	 * <p><b>注意：</b>对于 FactoryBean，仅 FactoryBean 创建的对象会被代理。
	 * 若要代理 FactoryBean 实例本身（罕见场景），
	 * 需指定含工厂 Bean 前缀 "&amp;" 的名称，例如 "&amp;myFactoryBean"。
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
	 * 若 Bean 名称匹配配置的支持名称列表之一，
	 * 则委托 {@link AbstractAutoProxyCreator#getCustomTargetSource(Class, String)}，
	 * 否则返回 {@code null}。
	 * @since 5.3
	 * @see #setBeanNames(String...)
	 */
	@Override
	protected @Nullable TargetSource getCustomTargetSource(Class<?> beanClass, String beanName) {
		return (isSupportedBeanName(beanClass, beanName) ?
				super.getCustomTargetSource(beanClass, beanName) : null);
	}

	/**
	 * 若 Bean 名称匹配配置的支持名称列表之一，则识别为需代理的 Bean。
	 * @see #setBeanNames(String...)
	 */
	@Override
	protected Object @Nullable [] getAdvicesAndAdvisorsForBean(
			Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {

		return (isSupportedBeanName(beanClass, beanName) ?
				PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS : DO_NOT_PROXY);
	}

	/**
	 * 判断给定 Bean 类的 Bean 名称是否匹配配置的支持名称列表之一。
	 * @param beanClass 要增强的 Bean 类
	 * @param beanName Bean 名称
	 * @return 若给定 Bean 名称被支持则为 {@code true}
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
					mappedName = mappedName.substring(1);  // '&' 的长度
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
	 * 判断给定 Bean 名称是否与映射名称匹配。
	 * <p>默认实现检查 "xxx*"、"*xxx" 和 "*xxx*" 匹配及直接相等。
	 * 子类可覆盖。
	 * @param beanName 要检查的 Bean 名称
	 * @param mappedName 配置名称列表中的名称
	 * @return 名称是否匹配
	 * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)
	 */
	protected boolean isMatch(String beanName, String mappedName) {
		return PatternMatchUtils.simpleMatch(mappedName, beanName);
	}

}
