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

package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * 持有 AOP <b>advice</b>（在连接点处执行的操作）以及判定 advice 适用性的过滤器
 * （例如切入点）的基础接口。<i>本接口不供 Spring 用户直接使用，
 * 而是为了在不同类型的 advice 支持之间提供共性。</i>
 *
 * <p>Spring AOP 基于通过方法 <b>interception</b> 传递的 <b>around advice</b>，
 * 符合 AOP Alliance 拦截 API。Advisor 接口也支持 before、after 等
 * 无需通过 interception 实现的 advice 类型。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface Advisor {

	/**
	 * 当尚未配置有效 advice 时，{@link #getAdvice()} 返回的空 {@code Advice} 占位符。
	 * @since 5.0
	 */
	Advice EMPTY_ADVICE = new Advice() {};


	/**
	 * 返回该切面的 advice 部分。advice 可以是拦截器、前置 advice、抛出 advice 等。
	 * @return 若切入点匹配则应生效的 advice
	 * @see org.aopalliance.intercept.MethodInterceptor
	 * @see BeforeAdvice
	 * @see ThrowsAdvice
	 * @see AfterReturningAdvice
	 */
	Advice getAdvice();

	/**
	 * 返回该 advice 是否与特定实例关联（例如创建 mixin），
	 * 还是与同一 Spring Bean 工厂产出的被通知类的所有实例共享。
	 * <p><b>注意：框架当前未使用本方法。</b>
	 * 典型 Advisor 实现始终返回 {@code true}。
	 * 请通过 singleton/prototype Bean 定义或合适的编程式代理创建，
	 * 确保 Advisor 具有正确的生命周期模型。
	 * <p>自 6.0.10 起，默认实现返回 {@code true}。
	 * @return 该 advice 是否与特定目标实例关联
	 */
	default boolean isPerInstance() {
		return true;
	}

}
