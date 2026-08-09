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

import java.lang.reflect.Field;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 获取静态或非静态字段值的 {@link FactoryBean}。
 *
 * <p>通常用于获取 public static final 常量。用法示例：
 *
 * <pre class="code">
 * // 标准定义：通过 "staticField" 属性暴露静态字段
 * &lt;bean id="myField" class="org.springframework.beans.factory.config.FieldRetrievingFactoryBean"&gt;
 *   &lt;property name="staticField" value="java.sql.Connection.TRANSACTION_SERIALIZABLE"/&gt;
 * &lt;/bean&gt;
 *
 * // 便捷写法：以静态字段模式作为 Bean 名称
 * &lt;bean id="java.sql.Connection.TRANSACTION_SERIALIZABLE"
 *       class="org.springframework.beans.factory.config.FieldRetrievingFactoryBean"/&gt;
 * </pre>
 *
 * <p>若使用 Spring 2.0，也可使用以下方式为 public static 字段配置：
 *
 * <pre class="code">&lt;util:constant static-field="java.sql.Connection.TRANSACTION_SERIALIZABLE"/&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setStaticField
 */
public class FieldRetrievingFactoryBean
		implements FactoryBean<Object>, BeanNameAware, BeanClassLoaderAware, InitializingBean {

	/** 定义字段的目标类。 */
	private @Nullable Class<?> targetClass;

	/** 定义字段的目标对象（实例字段时使用）。 */
	private @Nullable Object targetObject;

	/** 要获取的字段名称。 */
	private @Nullable String targetField;

	/** 完全限定的静态字段名（类名.字段名）。 */
	private @Nullable String staticField;

	/** Bean 名称。 */
	private @Nullable String beanName;

	/** Bean 类加载器。 */
	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/** 将要获取的字段反射对象。 */
	private @Nullable Field fieldObject;


	/**
	 * 设置定义字段的目标类。
	 * 仅当目标字段为静态时才需要；否则必须指定目标对象。
	 * @see #setTargetObject
	 * @see #setTargetField
	 */
	public void setTargetClass(@Nullable Class<?> targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * 返回定义字段的目标类。
	 */
	public @Nullable Class<?> getTargetClass() {
		return this.targetClass;
	}

	/**
	 * 设置定义字段的目标对象。
	 * 仅当目标字段非静态时才需要；否则指定目标类即可。
	 * @see #setTargetClass
	 * @see #setTargetField
	 */
	public void setTargetObject(@Nullable Object targetObject) {
		this.targetObject = targetObject;
	}

	/**
	 * 返回定义字段的目标对象。
	 */
	public @Nullable Object getTargetObject() {
		return this.targetObject;
	}

	/**
	 * 设置要获取的字段名称。
	 * 根据是否设置了目标对象，可指静态字段或实例字段。
	 * @see #setTargetClass
	 * @see #setTargetObject
	 */
	public void setTargetField(@Nullable String targetField) {
		this.targetField = (targetField != null ? StringUtils.trimAllWhitespace(targetField) : null);
	}

	/**
	 * 返回要获取的字段名称。
	 */
	public @Nullable String getTargetField() {
		return this.targetField;
	}

	/**
	 * 设置要获取的完全限定静态字段名，
	 * 例如 "example.MyExampleClass.MY_EXAMPLE_FIELD"。
	 * 是指定 targetClass 与 targetField 的便捷替代方式。
	 * @see #setTargetClass
	 * @see #setTargetField
	 */
	public void setStaticField(String staticField) {
		this.staticField = StringUtils.trimAllWhitespace(staticField);
	}

	/**
	 * 若未指定 "targetClass"、"targetObject" 或 "targetField"，
	 * 则本 FieldRetrievingFactoryBean 的 Bean 名称将被解释为 "staticField" 模式。
	 * 这样只需 id/name 即可写出简洁的 Bean 定义。
	 */
	@Override
	public void setBeanName(String beanName) {
		this.beanName = StringUtils.trimAllWhitespace(BeanFactoryUtils.originalBeanName(beanName));
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}


	@Override
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	public void afterPropertiesSet() throws ClassNotFoundException, NoSuchFieldException {
		if (this.targetClass != null && this.targetObject != null) {
			throw new IllegalArgumentException("Specify either targetClass or targetObject, not both");
		}

		if (this.targetClass == null && this.targetObject == null) {
			if (this.targetField != null) {
				throw new IllegalArgumentException(
						"Specify targetClass or targetObject in combination with targetField");
			}

			// 若未指定其他属性，则将 Bean 名称视为静态字段表达式
			if (this.staticField == null) {
				this.staticField = this.beanName;
				Assert.state(this.staticField != null, "No target field specified");
			}

			// 尝试将静态字段解析为类名与字段名
			int lastDotIndex = this.staticField.lastIndexOf('.');
			if (lastDotIndex == -1 || lastDotIndex == this.staticField.length()) {
				throw new IllegalArgumentException(
						"staticField must be a fully qualified class plus static field name: " +
						"for example, 'example.MyExampleClass.MY_EXAMPLE_FIELD'");
			}
			String className = this.staticField.substring(0, lastDotIndex);
			String fieldName = this.staticField.substring(lastDotIndex + 1);
			this.targetClass = ClassUtils.forName(className, this.beanClassLoader);
			this.targetField = fieldName;
		}

		else if (this.targetField == null) {
			// 已指定 targetClass 或 targetObject
			throw new IllegalArgumentException("targetField is required");
		}

		// 获取字段反射对象
		Class<?> targetClass = (this.targetObject != null ? this.targetObject.getClass() : this.targetClass);
		this.fieldObject = targetClass.getField(this.targetField);
	}


	@Override
	public @Nullable Object getObject() throws IllegalAccessException {
		if (this.fieldObject == null) {
			throw new FactoryBeanNotInitializedException();
		}
		ReflectionUtils.makeAccessible(this.fieldObject);
		if (this.targetObject != null) {
			// 实例字段
			return this.fieldObject.get(this.targetObject);
		}
		else {
			// 静态字段
			return this.fieldObject.get(null);
		}
	}

	@Override
	public @Nullable Class<?> getObjectType() {
		return (this.fieldObject != null ? this.fieldObject.getType() : null);
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
