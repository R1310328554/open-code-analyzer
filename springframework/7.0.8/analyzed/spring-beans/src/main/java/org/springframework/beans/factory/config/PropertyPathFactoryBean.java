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

package org.springframework.beans.factory.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 在给定目标对象上求值属性路径的 {@link FactoryBean}。
 *
 * <p>目标对象可直接指定，也可通过 Bean 名称指定。
 *
 * <p>用法示例：
 *
 * <pre class="code">&lt;!-- target bean to be referenced by name --&gt;
 * &lt;bean id="tb" class="org.springframework.beans.TestBean" singleton="false"&gt;
 *   &lt;property name="age" value="10"/&gt;
 *   &lt;property name="spouse"&gt;
 *     &lt;bean class="org.springframework.beans.TestBean"&gt;
 *       &lt;property name="age" value="11"/&gt;
 *     &lt;/bean&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;!-- will result in 12, which is the value of property 'age' of the inner bean --&gt;
 * &lt;bean id="propertyPath1" class="org.springframework.beans.factory.config.PropertyPathFactoryBean"&gt;
 *   &lt;property name="targetObject"&gt;
 *     &lt;bean class="org.springframework.beans.TestBean"&gt;
 *       &lt;property name="age" value="12"/&gt;
 *     &lt;/bean&gt;
 *   &lt;/property&gt;
 *   &lt;property name="propertyPath" value="age"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;!-- will result in 11, which is the value of property 'spouse.age' of bean 'tb' --&gt;
 * &lt;bean id="propertyPath2" class="org.springframework.beans.factory.config.PropertyPathFactoryBean"&gt;
 *   &lt;property name="targetBeanName" value="tb"/&gt;
 *   &lt;property name="propertyPath" value="spouse.age"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;!-- will result in 10, which is the value of property 'age' of bean 'tb' --&gt;
 * &lt;bean id="tb.age" class="org.springframework.beans.factory.config.PropertyPathFactoryBean"/&gt;</pre>
 *
 * <p>若使用 Spring 2.0 及配置文件中的 XML Schema 支持，也可采用以下风格的属性路径访问配置。
 * （更多示例请参阅 Spring 参考手册附录「基于 XML Schema 的配置」。）
 *
 * <pre class="code"> &lt;!-- will result in 10, which is the value of property 'age' of bean 'tb' --&gt;
 * &lt;util:property-path id="name" path="testBean.age"/&gt;</pre>
 *
 * Thanks to Matthias Ernst for the suggestion and initial prototype!
 *
 * @author Juergen Hoeller
 * @since 1.1.2
 * @see #setTargetObject
 * @see #setTargetBeanName
 * @see #setPropertyPath
 */
public class PropertyPathFactoryBean implements FactoryBean<Object>, BeanNameAware, BeanFactoryAware {

	private static final Log logger = LogFactory.getLog(PropertyPathFactoryBean.class);

	/** 目标对象的 BeanWrapper，直接指定目标对象时使用 */
	private @Nullable BeanWrapper targetBeanWrapper;

	/** 目标 Bean 的名称 */
	@SuppressWarnings("NullAway.Init")
	private String targetBeanName;

	/** 要访问的属性路径（可嵌套，如 "spouse.age"） */
	private @Nullable String propertyPath;

	/** 属性路径求值结果的类型 */
	private @Nullable Class<?> resultType;

	/** 本 FactoryBean 自身的 Bean 名称 */
	@SuppressWarnings("NullAway.Init")
	private String beanName;

	/** 所属 BeanFactory */
	private @Nullable BeanFactory beanFactory;


	/**
	 * 指定要应用属性路径的目标对象。
	 * 也可指定目标 Bean 名称。
	 * @param targetObject 目标对象，例如 Bean 引用或内部 Bean
	 * @see #setTargetBeanName
	 */
	public void setTargetObject(Object targetObject) {
		this.targetBeanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(targetObject);
	}

	/**
	 * 指定要应用属性路径的目标 Bean 名称。
	 * 也可直接指定目标对象。
	 * @param targetBeanName 在所属 BeanFactory 中查找的 Bean 名称（例如 "testBean"）
	 * @see #setTargetObject
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = StringUtils.trimAllWhitespace(targetBeanName);
	}

	/**
	 * 指定要应用于目标的属性路径。
	 * @param propertyPath 属性路径，可嵌套（例如 "age" 或 "spouse.age"）
	 */
	public void setPropertyPath(String propertyPath) {
		this.propertyPath = StringUtils.trimAllWhitespace(propertyPath);
	}

	/**
	 * 指定属性路径求值结果的类型。
	 * <p>注意：对于直接指定的目标对象或单例目标 Bean，可通过内省确定类型，无需指定。
	 * 仅在原型目标 Bean 且需要按类型匹配（例如自动装配）时才需指定。
	 * @param resultType 结果类型，例如 "java.lang.Integer"
	 */
	public void setResultType(Class<?> resultType) {
		this.resultType = resultType;
	}

	/**
	 * 若未指定 "targetObject"、"targetBeanName" 和 "propertyPath"，
	 * 则将本 PropertyPathFactoryBean 的 Bean 名称解释为 "beanName.property" 模式。
	 * 这样只需 id/name 即可定义简洁的 Bean。
	 */
	@Override
	public void setBeanName(String beanName) {
		this.beanName = StringUtils.trimAllWhitespace(BeanFactoryUtils.originalBeanName(beanName));
	}


	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;

		if (this.targetBeanWrapper != null && this.targetBeanName != null) {
			throw new IllegalArgumentException("Specify either 'targetObject' or 'targetBeanName', not both");
		}

		if (this.targetBeanWrapper == null && this.targetBeanName == null) {
			if (this.propertyPath != null) {
				throw new IllegalArgumentException(
						"Specify 'targetObject' or 'targetBeanName' in combination with 'propertyPath'");
			}

			// 未指定其他属性：从 Bean 名称解析
			int dotIndex = (this.beanName != null ? this.beanName.indexOf('.') : -1);
			if (dotIndex == -1) {
				throw new IllegalArgumentException(
						"Neither 'targetObject' nor 'targetBeanName' specified, and PropertyPathFactoryBean " +
						"bean name '" + this.beanName + "' does not follow 'beanName.property' syntax");
			}
			this.targetBeanName = this.beanName.substring(0, dotIndex);
			this.propertyPath = this.beanName.substring(dotIndex + 1);
		}

		else if (this.propertyPath == null) {
			// 已指定 targetObject 或 targetBeanName
			throw new IllegalArgumentException("'propertyPath' is required");
		}

		if (this.targetBeanWrapper == null && this.beanFactory.isSingleton(this.targetBeanName)) {
			// 急切获取单例目标 Bean，并确定结果类型
			Object bean = this.beanFactory.getBean(this.targetBeanName);
			this.targetBeanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
			this.resultType = this.targetBeanWrapper.getPropertyType(this.propertyPath);
		}
	}


	@Override
	public @Nullable Object getObject() throws BeansException {
		BeanWrapper target = this.targetBeanWrapper;
		if (target != null) {
			if (logger.isWarnEnabled() && this.targetBeanName != null &&
					this.beanFactory instanceof ConfigurableBeanFactory cbf &&
					cbf.isCurrentlyInCreation(this.targetBeanName)) {
				logger.warn("Target bean '" + this.targetBeanName + "' is still in creation due to a circular " +
						"reference - obtained value for property '" + this.propertyPath + "' may be outdated!");
			}
		}
		else {
			// 获取原型目标 Bean
			Assert.state(this.beanFactory != null, "No BeanFactory available");
			Assert.state(this.targetBeanName != null, "No target bean name specified");
			Object bean = this.beanFactory.getBean(this.targetBeanName);
			target = PropertyAccessorFactory.forBeanPropertyAccess(bean);
		}
		Assert.state(this.propertyPath != null, "No property path specified");
		return target.getPropertyValue(this.propertyPath);
	}

	@Override
	public @Nullable Class<?> getObjectType() {
		return this.resultType;
	}

	/**
	 * 虽然本 FactoryBean 常用于单例目标，但属性路径所调用的 getter
	 * 每次调用可能返回新对象，因此假定每次 {@link #getObject()} 调用
	 * 不会返回同一对象。
	 */
	@Override
	public boolean isSingleton() {
		return false;
	}

}
