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

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;

/**
 * {@link FactoryBean}，其返回值为静态或实例方法调用的结果。
 * 对大多数用例而言，直接使用容器内置的工厂方法支持更为合适，因其参数转换更智能。
 * 但当需要调用无返回值的方法时（例如用于触发某种初始化的静态类方法），
 * 本工厂 Bean 仍然有用——工厂方法不支持此场景，因为需要返回值来获取 Bean 实例。
 *
 * <p>由于预期主要用于访问工厂方法，本工厂默认以<b>单例</b>方式运行。
 * 所属 Bean 工厂首次请求 {@link #getObject} 时会触发方法调用，
 * 其返回值会被缓存以供后续请求使用。可将内部 {@link #setSingleton singleton}
 * 属性设为 "false"，使本工厂在每次请求对象时都调用目标方法。
 *
 * <p><b>注意：若目标方法不产生可暴露的结果，请考虑使用 {@link MethodInvokingBean}，
 * 以避免本 {@link MethodInvokingFactoryBean} 带来的类型判定与生命周期限制。</b>
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
 * <p>在基于 XML 的 Bean 工厂定义中，使用本类调用静态工厂方法的示例：
 *
 * <pre class="code">
 * &lt;bean id="myObject" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"&gt;
 *   &lt;property name="staticMethod" value="com.whatever.MyClassFactory.getInstance"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * <p>先调用静态方法再调用实例方法以获取 Java 系统属性的示例（略显冗长，但可行）：
 *
 * <pre class="code">
 * &lt;bean id="sysProps" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"&gt;
 *   &lt;property name="targetClass" value="java.lang.System"/&gt;
 *   &lt;property name="targetMethod" value="getProperties"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="javaVersion" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"&gt;
 *   &lt;property name="targetObject" ref="sysProps"/&gt;
 *   &lt;property name="targetMethod" value="getProperty"/&gt;
 *   &lt;property name="arguments" value="java.version"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 21.11.2003
 * @see MethodInvokingBean
 * @see org.springframework.util.MethodInvoker
 */
public class MethodInvokingFactoryBean extends MethodInvokingBean implements FactoryBean<Object> {

	/** 是否以单例方式运行。 */
	private boolean singleton = true;

	/** 是否已完成初始化。 */
	private boolean initialized = false;

	/** 单例情形下的方法调用结果。 */
	private @Nullable Object singletonObject;


	/**
	 * 设置是否创建单例；否则每次 {@link #getObject()} 请求都创建新对象。默认为 "true"。
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		prepare();
		if (this.singleton) {
			this.initialized = true;
			this.singletonObject = invokeWithTargetException();
		}
	}


	/**
	 * 若 singleton 属性为 "true"，则每次返回相同值；
	 * 否则即时调用指定方法并返回其结果。
	 */
	@Override
	public @Nullable Object getObject() throws Exception {
		if (this.singleton) {
			if (!this.initialized) {
				throw new FactoryBeanNotInitializedException();
			}
			// 单例：返回共享对象
			return this.singletonObject;
		}
		else {
			// 原型：每次调用创建新对象
			return invokeWithTargetException();
		}
	}

	/**
	 * 返回本 FactoryBean 创建的对象类型，若事先未知则为 {@code null}。
	 */
	@Override
	public @Nullable Class<?> getObjectType() {
		if (!isPrepared()) {
			// 尚未完全初始化 → 返回 null 表示"尚不可知"
			return null;
		}
		return getPreparedMethod().getReturnType();
	}

	@Override
	public boolean isSingleton() {
		return this.singleton;
	}

}
