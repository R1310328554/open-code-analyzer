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

package org.springframework.beans.factory.aot;

import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.MethodReference;
import org.springframework.javapoet.ClassName;

/**
 * 用于配置将生成的、执行 Bean 工厂初始化代码的接口。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @since 6.0
 * @see BeanFactoryInitializationAotContribution
 */
public interface BeanFactoryInitializationCode {

	/**
	 * 引用 Bean 工厂时推荐使用的变量名。
	 */
	String BEAN_FACTORY_VARIABLE = "beanFactory";

	/**
	 * 获取初始化代码所使用的 {@link GeneratedMethods}。
	 * @return 生成的方法集合
	 */
	GeneratedMethods getMethods();

	/**
	 * 返回初始化代码所使用的类名。
	 * @return 生成类的名称
	 * @since 7.0.2
	 */
	ClassName getClassName();

	/**
	 * 添加初始化器方法调用。初始化器可使用灵活的签名，支持以下任意参数：
	 * <ul>
	 * <li>{@code DefaultListableBeanFactory} 或 {@code ConfigurableListableBeanFactory}
	 * 以使用 Bean 工厂。</li>
	 * <li>{@code ConfigurableEnvironment} 或 {@code Environment} 以访问环境。</li>
	 * <li>{@code ResourceLoader} 以加载资源。</li>
	 * </ul>
	 * @param methodReference 待调用的初始化方法引用
	 */
	void addInitializer(MethodReference methodReference);

}
