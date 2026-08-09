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

/**
 * 执行一个或多个 AOP <b>introduction</b>（引介）的 Advisor 超接口。
 *
 * <p>本接口不能直接实现；子接口必须提供实现引介的 advice 类型。
 *
 * <p>引介是通过 AOP advice 为目标实现其原本不具备的额外接口。
 *
 * @author Rod Johnson
 * @since 04.04.2003
 * @see IntroductionInterceptor
 */
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

	/**
	 * 返回决定该引介应作用于哪些目标类的过滤器。
	 * <p>这对应切入点的类匹配部分。注意引介不涉及方法匹配。
	 * @return 类过滤器
	 */
	ClassFilter getClassFilter();

	/**
	 * 被通知接口能否由引介 advice 实现？
	 * 在添加 IntroductionAdvisor 之前调用。
	 * @throws IllegalArgumentException 若被通知接口无法由引介 advice 实现
	 */
	void validateInterfaces() throws IllegalArgumentException;

}
