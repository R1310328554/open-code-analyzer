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

import java.lang.reflect.InvocationTargetException;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.util.ClassUtils;

/**
 * 简单的方法调用 Bean：仅调用目标方法，不向容器暴露返回值
 *（与 {@link MethodInvokingFactoryBean} 不同）。
 *
 * <p>本调用器支持任意目标方法。可通过将 {@link #setTargetMethod targetMethod} 属性
 * 设置为表示静态方法名的字符串，并用 {@link #setTargetClass targetClass} 指定
 * 定义该静态方法的 Class，来指定静态方法。或者，通过将
 * {@link #setTargetObject targetObject} 属性设为目标对象，并将
 * {@link #setTargetMethod targetMethod} 属性设为目标对象上要调用的方法名，
 * 来指定实例方法。可通过 {@link #setArguments arguments} 属性指定方法调用的参数。
 *
 * <p>本类依赖 {@link #afterPropertiesSet()} 在所有属性设置完成后被调用，
 * 符合 InitializingBean 契约。
 *
 * <p>在基于 XML 的 Bean 工厂定义中，使用本类调用静态初始化方法的示例：
 *
 * <pre class="code">
 * &lt;bean id="myObject" class="org.springframework.beans.factory.config.MethodInvokingBean"&gt;
 *   &lt;property name="staticMethod" value="com.whatever.MyClass.init"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * <p>调用实例方法以启动某服务器 Bean 的示例：
 *
 * <pre class="code">
 * &lt;bean id="myStarter" class="org.springframework.beans.factory.config.MethodInvokingBean"&gt;
 *   &lt;property name="targetObject" ref="myServer"/&gt;
 *   &lt;property name="targetMethod" value="start"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 4.0.3
 * @see MethodInvokingFactoryBean
 * @see org.springframework.util.MethodInvoker
 */
public class MethodInvokingBean extends ArgumentConvertingMethodInvoker
		implements BeanClassLoaderAware, BeanFactoryAware, InitializingBean {

	/** Bean 类加载器。 */
	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/** 可配置的 Bean 工厂。 */
	private @Nullable ConfigurableBeanFactory beanFactory;


	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	@Override
	protected Class<?> resolveClassName(String className) throws ClassNotFoundException {
		return ClassUtils.forName(className, this.beanClassLoader);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (beanFactory instanceof ConfigurableBeanFactory cbf) {
			this.beanFactory = cbf;
		}
	}

	/**
	 * 从本 Bean 运行的 BeanFactory 获取 TypeConverter（若可能）。
	 * @see ConfigurableBeanFactory#getTypeConverter()
	 */
	@Override
	protected TypeConverter getDefaultTypeConverter() {
		if (this.beanFactory != null) {
			return this.beanFactory.getTypeConverter();
		}
		else {
			return super.getDefaultTypeConverter();
		}
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		prepare();
		invokeWithTargetException();
	}

	/**
	 * 执行调用，并将 InvocationTargetException 转换为底层目标异常。
	 */
	protected @Nullable Object invokeWithTargetException() throws Exception {
		try {
			return invoke();
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof Exception exception) {
				throw exception;
			}
			if (ex.getTargetException() instanceof Error error) {
				throw error;
			}
			throw ex;
		}
	}

}
