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
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.AfterAdvice;
import org.springframework.aop.BeforeAdvice;

/**
 * 用于处理 AspectJ Advisor 的实用方法。
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AspectJAopUtils {

	/**
	 * 如果 Advisor 是 before 建议的形式，则返回 {@code true}。
	 */
	public static boolean isBeforeAdvice(Advisor anAdvisor) {
		AspectJPrecedenceInformation precedenceInfo = getAspectJPrecedenceInformationFor(anAdvisor);
		if (precedenceInfo != null) {
			return precedenceInfo.isBeforeAdvice();
		}
		return (anAdvisor.getAdvice() instanceof BeforeAdvice);
	}

	/**
	 * 如果顾问是事后建议的形式，则返回 {@code true}。
	 */
	public static boolean isAfterAdvice(Advisor anAdvisor) {
		AspectJPrecedenceInformation precedenceInfo = getAspectJPrecedenceInformationFor(anAdvisor);
		if (precedenceInfo != null) {
			return precedenceInfo.isAfterAdvice();
		}
		return (anAdvisor.getAdvice() instanceof AfterAdvice);
	}

	/**
	 * 返回此顾问或其建议提供的 AspectJPrecedenceInformation。如果顾问和建议都没有优先级信息，则此方法将返回 {@code null}。
	 */
	public static @Nullable AspectJPrecedenceInformation getAspectJPrecedenceInformationFor(Advisor anAdvisor) {
		if (anAdvisor instanceof AspectJPrecedenceInformation ajpi) {
			return ajpi;
		}
		Advice advice = anAdvisor.getAdvice();
		if (advice instanceof AspectJPrecedenceInformation ajpi) {
			return ajpi;
		}
		return null;
	}

}
