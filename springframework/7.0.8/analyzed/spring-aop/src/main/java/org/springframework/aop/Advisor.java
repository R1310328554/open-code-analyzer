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
 * 包含 AOP OCAJAVA0DOAdvice</b>（在连接点处执行的操作）和确定建议适用性的过滤器（例如切入点）的基本接口。 <i>该接口不供Spring用户使用，而是为了
 *  支持不同类型的建议提供通用性。</i> <p>Spring AOP 基于 <b> 周围的建议 </b> 通过方法 <b>interception</b> 传递，符合 AOP 
 * 联盟拦截 API。 Advisor 接口允许支持不同类型的建议，例如 <b>before</b> 和 <b>after</b> 建议，这些建议不需要使用拦截来实现。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface Advisor {

	/**
	 * 如果尚未配置正确的建议，则从 {@link #getAdvice()} 返回空 {@code Advice} 的通用占位符。
	 * @since 5.0
	 */
	Advice EMPTY_ADVICE = new Advice() {};


	/**
	 * 返回这方面的建议部分。一个通知可以是拦截器、前置通知、抛出通知等。
	 * @return
	 * @see org.aopalliance.intercept.MethodInterceptor
	 * @see BeforeAdvice
	 * @see ThrowsAdvice
	 * @see AfterReturningAdvice
	 */
	Advice getAdvice();

	/**
	* 返回此建议是否与特定实例关联（例如，创建 mixin）或与从同一 Spring bean 工厂获取的建议类的所有实例共享。 <p><b>请注意，框架当前未使用此方法。</b> 
	* 典型 Advisor 实现始终返回 {@code true}。使用单例/原型 bean 定义或适当的编程代理创建来确保 Advisor 具有正确的生命周期模型。 6.0.10 
	* 的 <p>A，默认实现返回 {@code true}。
	* @return 该建议与特定目标实例相关联
	*/
	default boolean isPerInstance() {
		return true;
	}

}
