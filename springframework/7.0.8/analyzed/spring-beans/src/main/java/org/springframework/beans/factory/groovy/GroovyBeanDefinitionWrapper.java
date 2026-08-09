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

package org.springframework.beans.factory.groovy;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import groovy.lang.GroovyObjectSupport;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Spring {@link BeanDefinition} 的内部包装器，允许在
 * {@link GroovyBeanDefinitionReader} 闭包内以 Groovy 风格访问属性。
 *
 * @author Jeff Brown
 * @author Juergen Hoeller
 * @since 4.0
 */
class GroovyBeanDefinitionWrapper extends GroovyObjectSupport {

	/** 父 Bean 属性名。 */
	private static final String PARENT = "parent";
	/** 自动装配属性名。 */
	private static final String AUTOWIRE = "autowire";
	/** 构造器参数属性名。 */
	private static final String CONSTRUCTOR_ARGS = "constructorArgs";
	/** 工厂 Bean 属性名。 */
	private static final String FACTORY_BEAN = "factoryBean";
	/** 工厂方法属性名。 */
	private static final String FACTORY_METHOD = "factoryMethod";
	/** 初始化方法属性名。 */
	private static final String INIT_METHOD = "initMethod";
	/** 销毁方法属性名。 */
	private static final String DESTROY_METHOD = "destroyMethod";
	/** 单例属性名。 */
	private static final String SINGLETON = "singleton";

	/** 需特殊处理的动态属性名集合。 */
	private static final Set<String> dynamicProperties = Set.of(PARENT, AUTOWIRE, CONSTRUCTOR_ARGS,
			FACTORY_BEAN, FACTORY_METHOD, INIT_METHOD, DESTROY_METHOD, SINGLETON);


	/** Bean 名称。 */
	private @Nullable String beanName;

	/** Bean 类类型。 */
	private final @Nullable Class<?> clazz;

	/** 构造器参数集合。 */
	private final @Nullable Collection<?> constructorArgs;

	/** 底层 Bean 定义。 */
	private @Nullable AbstractBeanDefinition definition;

	/** Bean 定义的 BeanWrapper。 */
	private @Nullable BeanWrapper definitionWrapper;

	/** 父 Bean 名称。 */
	private @Nullable String parentName;


	GroovyBeanDefinitionWrapper(String beanName) {
		this(beanName, null);
	}

	GroovyBeanDefinitionWrapper(@Nullable String beanName, @Nullable Class<?> clazz) {
		this(beanName, clazz, null);
	}

	GroovyBeanDefinitionWrapper(@Nullable String beanName, @Nullable Class<?> clazz, @Nullable Collection<?> constructorArgs) {
		this.beanName = beanName;
		this.clazz = clazz;
		this.constructorArgs = constructorArgs;
	}


	/**
	 * 返回 Bean 名称。
	 */
	public @Nullable String getBeanName() {
		return this.beanName;
	}

	/**
	 * 设置底层 Bean 定义。
	 */
	void setBeanDefinition(AbstractBeanDefinition definition) {
		this.definition = definition;
	}

	/**
	 * 获取 Bean 定义，若尚未创建则懒加载创建。
	 */
	AbstractBeanDefinition getBeanDefinition() {
		if (this.definition == null) {
			this.definition = createBeanDefinition();
		}
		return this.definition;
	}

	/**
	 * 创建新的 GenericBeanDefinition，并填充类、构造器参数及父名称。
	 */
	protected AbstractBeanDefinition createBeanDefinition() {
		AbstractBeanDefinition bd = new GenericBeanDefinition();
		bd.setBeanClass(this.clazz);
		if (!CollectionUtils.isEmpty(this.constructorArgs)) {
			ConstructorArgumentValues cav = new ConstructorArgumentValues();
			for (Object constructorArg : this.constructorArgs) {
				cav.addGenericArgumentValue(constructorArg);
			}
			bd.setConstructorArgumentValues(cav);
		}
		if (this.parentName != null) {
			bd.setParentName(this.parentName);
		}
		this.definitionWrapper = new BeanWrapperImpl(bd);
		return bd;
	}

	/**
	 * 从 BeanDefinitionHolder 设置 Bean 定义与名称。
	 */
	void setBeanDefinitionHolder(BeanDefinitionHolder holder) {
		this.definition = (AbstractBeanDefinition) holder.getBeanDefinition();
		this.beanName = holder.getBeanName();
	}

	/**
	 * 返回封装当前 Bean 定义的 BeanDefinitionHolder。
	 */
	BeanDefinitionHolder getBeanDefinitionHolder() {
		Assert.state(this.beanName != null, "Bean name must be set");
		return new BeanDefinitionHolder(getBeanDefinition(), this.beanName);
	}

	/**
	 * 设置父 Bean，支持字符串、RuntimeBeanReference 或 GroovyBeanDefinitionWrapper。
	 */
	void setParent(@Nullable Object obj) {
		Assert.notNull(obj, "Parent bean cannot be set to a null runtime bean reference");
		if (obj instanceof String name) {
			this.parentName = name;
		}
		else if (obj instanceof RuntimeBeanReference runtimeBeanReference) {
			this.parentName = runtimeBeanReference.getBeanName();
		}
		else if (obj instanceof GroovyBeanDefinitionWrapper wrapper) {
			this.parentName = wrapper.getBeanName();
		}
		getBeanDefinition().setParentName(this.parentName);
		getBeanDefinition().setAbstract(false);
	}

	/**
	 * 向 Bean 定义添加属性值，若值为包装器则提取其 Bean 定义。
	 */
	GroovyBeanDefinitionWrapper addProperty(String propertyName, @Nullable Object propertyValue) {
		if (propertyValue instanceof GroovyBeanDefinitionWrapper wrapper) {
			propertyValue = wrapper.getBeanDefinition();
		}
		getBeanDefinition().getPropertyValues().add(propertyName, propertyValue);
		return this;
	}


	@Override
	public @Nullable Object getProperty(String property) {
		Assert.state(this.definitionWrapper != null, "BeanDefinition wrapper not initialized");
		if (this.definitionWrapper.isReadableProperty(property)) {
			return this.definitionWrapper.getPropertyValue(property);
		}
		else if (dynamicProperties.contains(property)) {
			return null;
		}
		return super.getProperty(property);
	}

	@Override
	public void setProperty(String property, @Nullable Object newValue) {
		if (PARENT.equals(property)) {
			setParent(newValue);
		}
		else {
			AbstractBeanDefinition bd = getBeanDefinition();
			Assert.state(this.definitionWrapper != null, "BeanDefinition wrapper not initialized");
			if (AUTOWIRE.equals(property)) {
				// 根据字符串或布尔值设置自动装配模式
				if ("byName".equals(newValue)) {
					bd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
				}
				else if ("byType".equals(newValue)) {
					bd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
				}
				else if ("constructor".equals(newValue)) {
					bd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
				}
				else if (Boolean.TRUE.equals(newValue)) {
					bd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
				}
			}
			// 构造器参数
			else if (CONSTRUCTOR_ARGS.equals(property) && newValue instanceof List<?> args) {
				ConstructorArgumentValues cav = new ConstructorArgumentValues();
				for (Object arg : args) {
					cav.addGenericArgumentValue(arg);
				}
				bd.setConstructorArgumentValues(cav);
			}
			// 工厂 Bean
			else if (FACTORY_BEAN.equals(property)) {
				if (newValue != null) {
					bd.setFactoryBeanName(newValue.toString());
				}
			}
			// 工厂方法
			else if (FACTORY_METHOD.equals(property)) {
				if (newValue != null) {
					bd.setFactoryMethodName(newValue.toString());
				}
			}
			// 初始化方法
			else if (INIT_METHOD.equals(property)) {
				if (newValue != null) {
					bd.setInitMethodName(newValue.toString());
				}
			}
			// 销毁方法
			else if (DESTROY_METHOD.equals(property)) {
				if (newValue != null) {
					bd.setDestroyMethodName(newValue.toString());
				}
			}
			// 单例作用域
			else if (SINGLETON.equals(property)) {
				bd.setScope(Boolean.TRUE.equals(newValue) ?
						BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
			}
			else if (this.definitionWrapper.isWritableProperty(property)) {
				this.definitionWrapper.setPropertyValue(property, newValue);
			}
			else {
				super.setProperty(property, newValue);
			}
		}
	}

}
