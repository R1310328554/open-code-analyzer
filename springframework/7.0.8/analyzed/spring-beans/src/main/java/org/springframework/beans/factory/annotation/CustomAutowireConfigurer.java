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

package org.springframework.beans.factory.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} 实现，
 * 便于注册自定义的自动装配限定符注解类型。
 *
 * <pre class="code">
 * &lt;bean id="customAutowireConfigurer" class="org.springframework.beans.factory.annotation.CustomAutowireConfigurer"&gt;
 *   &lt;property name="customQualifierTypes"&gt;
 *     &lt;set&gt;
 *       &lt;value&gt;mypackage.MyQualifier&lt;/value&gt;
 *     &lt;/set&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.beans.factory.annotation.Qualifier
 */
public class CustomAutowireConfigurer implements BeanFactoryPostProcessor, BeanClassLoaderAware, Ordered {

	/** 后置处理器排序值（默认与未实现 Ordered 的处理器相同） */
	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered

	/** 要注册的自定义限定符注解类型集合 */
	private @Nullable Set<?> customQualifierTypes;

	/** 用于解析限定符类名的 ClassLoader */
	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	/**
	 * 设置本后置处理器的排序值。
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	/**
	 * 注册在自动装配 Bean 时需要考虑的自定义限定符注解类型。
	 * 集合中每个元素可以是 {@code Class} 实例，也可以是自定义注解全限定类名的字符串。
	 * <p>注意：本身已标注 Spring {@link org.springframework.beans.factory.annotation.Qualifier}
	 * 的注解无需再显式注册。
	 * @param customQualifierTypes 要注册的自定义类型
	 */
	public void setCustomQualifierTypes(Set<?> customQualifierTypes) {
		this.customQualifierTypes = customQualifierTypes;
	}


	@Override
	@SuppressWarnings("unchecked")
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.customQualifierTypes != null) {
			if (!(beanFactory instanceof DefaultListableBeanFactory dlbf)) {
				throw new IllegalStateException(
						"CustomAutowireConfigurer needs to operate on a DefaultListableBeanFactory");
			}
			if (!(dlbf.getAutowireCandidateResolver() instanceof QualifierAnnotationAutowireCandidateResolver)) {
				dlbf.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
			}
			QualifierAnnotationAutowireCandidateResolver resolver =
					(QualifierAnnotationAutowireCandidateResolver) dlbf.getAutowireCandidateResolver();
			for (Object value : this.customQualifierTypes) {
				Class<? extends Annotation> customType = null;
				if (value instanceof Class) {
					customType = (Class<? extends Annotation>) value;
				}
				else if (value instanceof String className) {
					customType = (Class<? extends Annotation>) ClassUtils.resolveClassName(className, this.beanClassLoader);
				}
				else {
					throw new IllegalArgumentException(
							"Invalid value [" + value + "] for custom qualifier type: needs to be Class or String.");
				}
				if (!Annotation.class.isAssignableFrom(customType)) {
					throw new IllegalArgumentException(
							"Qualifier type [" + customType.getName() + "] needs to be annotation type");
				}
				resolver.addQualifierType(customType);
			}
		}
	}

}
