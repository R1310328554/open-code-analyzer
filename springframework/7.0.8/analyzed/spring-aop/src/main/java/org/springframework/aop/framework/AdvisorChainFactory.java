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

import java.lang.reflect.Method;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * 顾问链的工厂接口。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface AdvisorChainFactory {

	/**
	 * 确定给定顾问链配置的 {@link org.aopalliance.intercept.MethodInterceptor} 对象列表。
	 * @param config Advised 对象形式的 AOP 配置
	 * @param method 代理方法
	 * @param targetClass 目标类（可能是 {@code null} 来指示没有目标对象的代理，在这种情况下，方法的声明类是下一个最佳选择）
	 * @return MethodInterceptors 列表（也可能包括 InterceptorAndDynamicMethodMatchers）
	 */
	List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, @Nullable Class<?> targetClass);

}
