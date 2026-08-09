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

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.lang.Contract;
import org.springframework.util.StringUtils;

/**
 * 使用 AspectJ 代理的实用方法。
 * @author Rod Johnson
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AspectJProxyUtils {

	/**
	 * 如果需要使用包含 AspectJ 顾问程序的代理链，请添加特殊顾问程序：具体来说，{@link ExposeInvocationInterceptor} 位于列表的开头。 <p
	 * >这将公开当前的 Spring AOP 调用（某些 AspectJ 切入点匹配所必需的）并使当前的 AspectJ JoinPoint 可用。如果顾问程序链中没有 Aspect
	 * J 顾问程序，则该调用将无效。
	 * @param advisors 可用的顾问
	 * @return true} 如果 {@link ExposeInvocationInterceptor} 已添加到列表中，否则 {@code false}
	 */
	public static boolean makeAdvisorChainAspectJCapableIfNecessary(List<Advisor> advisors) {
		// 不要将顾问添加到空列表中；可能表明不需要代理
		if (!advisors.isEmpty()) {
			boolean foundAspectJAdvice = false;
			for (Advisor advisor : advisors) {
				// 小心不要在没有守卫的情况下获取建议，因为这可能会急切地
				// 实例化一个非单一 AspectJ 方面...
				if (isAspectJAdvice(advisor)) {
					foundAspectJAdvice = true;
					break;
				}
			}
			if (foundAspectJAdvice && !advisors.contains(ExposeInvocationInterceptor.ADVISOR)) {
				advisors.add(0, ExposeInvocationInterceptor.ADVISOR);
				return true;
			}
		}
		return false;
	}

	/**
	 * 确定给定的 Advisor 是否包含 AspectJ 建议。
	 * @param advisor 顾问检查
	 */
	private static boolean isAspectJAdvice(Advisor advisor) {
		return (advisor instanceof InstantiationModelAwarePointcutAdvisor ||
				advisor.getAdvice() instanceof AbstractAspectJAdvice ||
				(advisor instanceof PointcutAdvisor pointcutAdvisor &&
						pointcutAdvisor.getPointcut() instanceof AspectJExpressionPointcut));
	}

	@Contract("null -> false")
	static boolean isVariableName(@Nullable String name) {
		if (!StringUtils.hasLength(name)) {
			return false;
		}
		if (!Character.isJavaIdentifierStart(name.charAt(0))) {
			return false;
		}
		for (int i = 1; i < name.length(); i++) {
			if (!Character.isJavaIdentifierPart(name.charAt(i))) {
				return false;
			}
		}
		return true;
	}

}
