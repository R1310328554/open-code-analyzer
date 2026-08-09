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

package org.springframework.aop.aspectj;

import org.aopalliance.aop.Advice;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.support.ClassFilters;
import org.springframework.aop.support.DelegatePerTargetObjectIntroductionInterceptor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * 介绍顾问委派给给定的对象。为 DeclareParents 注释实现 AspectJ 注释样式行为。
 * @author Rod Johnson
 * @author Ramnivas Laddad
 * @since 2.0
 */
public class DeclareParentsAdvisor implements IntroductionAdvisor {

	/** 通知相关状态（`advice`）。 */
	private final Advice advice;

	/** `introducedInterface`：该类的成员状态。 */
	private final Class<?> introducedInterface;

	/** 类型相关状态（`typePatternClassFilter`）。 */
	private final ClassFilter typePatternClassFilter;


	/**
	 * 为此 DeclareParents 字段创建一个新顾问。
	 * @param interfaceType 定义介绍的静态字段
	 * @param typePattern 类型模式的介绍仅限于
	 * @param defaultImpl 默认实现类
	 */
	public DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, Class<?> defaultImpl) {
		this(interfaceType, typePattern,
				new DelegatePerTargetObjectIntroductionInterceptor(defaultImpl, interfaceType));
	}

	/**
	 * 为此 DeclareParents 字段创建一个新顾问。
	 * @param interfaceType 定义介绍的静态字段
	 * @param typePattern 类型模式的介绍仅限于
	 * @param delegateRef 委托实现对象
	 */
	public DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, Object delegateRef) {
		this(interfaceType, typePattern, new DelegatingIntroductionInterceptor(delegateRef));
	}

	/**
	 * 私有构造函数在基于 impl 的委托和基于引用的委托之间共享公共代码（由于使用了 Final 字段，因此不能使用 init() 等方法来共享公共代码）。
	 * @param interfaceType 定义介绍的静态字段
	 * @param typePattern 类型模式的介绍仅限于
	 * @param interceptor 代表团建议为{@link IntroductionInterceptor}
	 */
	private DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, IntroductionInterceptor interceptor) {
		this.advice = interceptor;
		this.introducedInterface = interfaceType;

		// 不包括实施的方法。
		ClassFilter typePatternFilter = new TypePatternClassFilter(typePattern);
		ClassFilter exclusion = (clazz -> !this.introducedInterface.isAssignableFrom(clazz));
		this.typePatternClassFilter = ClassFilters.intersection(typePatternFilter, exclusion);
	}


	/**
	 * 获取 Class Filter（`ClassFilter`）。
	 */
	@Override
	public ClassFilter getClassFilter() {
		return this.typePatternClassFilter;
	}

	/**
	 * 校验：Interfaces（方法 `validateInterfaces`）。
	 */
	@Override
	public void validateInterfaces() throws IllegalArgumentException {
		// 什么都不做
	}

	/**
	 * 获取 Advice（`Advice`）。
	 */
	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	/**
	 * 获取 Interfaces（`Interfaces`）。
	 */
	@Override
	public Class<?>[] getInterfaces() {
		return new Class<?>[] {this.introducedInterface};
	}

}
