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

package org.springframework.aop.framework;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.objenesis.SpringObjenesis;
import org.springframework.util.ReflectionUtils;

/**
 * {@link CglibAopProxy} 基于 Objenesis 的扩展，可在不调用类的构造函数的情况下创建代理实例。默认使用。
 * @author Oliver Gierke
 * @author Juergen Hoeller
 * @since 4.0
 */
@SuppressWarnings("serial")
class ObjenesisCglibAopProxy extends CglibAopProxy {

	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(ObjenesisCglibAopProxy.class);

	/**
	 * 方法 `SpringObjenesis`：完成本类中与「Spring Objenesis」相关的职责。
	 */
	private static final SpringObjenesis objenesis = new SpringObjenesis();


	/**
	 * 为给定的 AOP 配置创建一个新的 ObjenesisCglibAopProxy。
	 * @param config AOP 配置为 AdvisedSupport 对象
	 */
	public ObjenesisCglibAopProxy(AdvisedSupport config) {
		super(config);
	}


	/**
	 * 创建：Proxy Class（方法 `createProxyClass`）。
	 */
	@Override
	protected Class<?> createProxyClass(Enhancer enhancer) {
		return enhancer.createClass();
	}

	/**
	 * 创建：Proxy Class And Instance（方法 `createProxyClassAndInstance`）。
	 */
	@Override
	protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
		Class<?> proxyClass = enhancer.createClass();
		Object proxyInstance = null;

		if (objenesis.isWorthTrying()) {
			try {
				proxyInstance = objenesis.newInstance(proxyClass, enhancer.getUseCache());
			}
			catch (Throwable ex) {
				logger.debug("Unable to instantiate proxy using Objenesis, " +
						"falling back to regular proxy construction", ex);
			}
		}

		if (proxyInstance == null) {
			// 通过默认构造函数定期实例化...
			try {
				Constructor<?> ctor = (this.constructorArgs != null ?
						proxyClass.getDeclaredConstructor(this.constructorArgTypes) :
						proxyClass.getDeclaredConstructor());
				ReflectionUtils.makeAccessible(ctor);
				proxyInstance = (this.constructorArgs != null ?
						ctor.newInstance(this.constructorArgs) : ctor.newInstance());
			}
			catch (Throwable ex) {
				throw new AopConfigException("Unable to instantiate proxy using Objenesis, " +
						"and regular proxy instantiation via default constructor fails as well", ex);
			}
		}

		((Factory) proxyInstance).setCallbacks(callbacks);
		return proxyInstance;
	}

}
