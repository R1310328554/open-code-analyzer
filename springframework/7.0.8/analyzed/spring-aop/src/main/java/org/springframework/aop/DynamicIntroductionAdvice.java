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
 * AOP Alliance Advice 的子接口，允许 Advice 实现额外接口，
 * 并通过使用该拦截器的代理对外提供。这是 AOP 的基本概念 <b>introduction</b>（引介）。
 *
 * <p>引介通常是 <b>mixins</b>，用于构建复合对象，
 * 在 Java 中实现多重继承的许多目标。
 *
 * <p>与 {@link IntroductionInfo} 相比，本接口允许 advice 实现
 * 事先未必确定的一组接口。因此可用 {@link IntroductionAdvisor}
 * 指定被通知对象将暴露哪些接口。
 *
 * @author Rod Johnson
 * @since 1.1.1
 * @see IntroductionInfo
 * @see IntroductionAdvisor
 */
public interface DynamicIntroductionAdvice extends Advice {

	/**
	 * 该引介 advice 是否实现了给定接口？
	 * @param intf 待检查的接口
	 * @return advice 是否实现指定接口
	 */
	boolean implementsInterface(Class<?> intf);

}
