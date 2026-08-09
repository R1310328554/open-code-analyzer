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
 * 用于执行一个或多个 AOP <b> 简介 </b> 的顾问程序的超级接口。 <p>该接口不能直接实现；子接口必须提供实现引入的建议类型。 <p>Introduction 是通过
 *  AOP 建议实现附加接口（未由目标实现）。
 * @author Rod Johnson
 * @since 04.04.2003
 * @see IntroductionInterceptor
 */
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

	/**
	* 返回过滤器，确定此介绍应适用于哪些目标类。 <p>这表示切入点的类部分。请注意，方法匹配对于介绍没有意义。
	* @return 类过滤器
	*/
	ClassFilter getClassFilter();

	/**
	 * 所建议的接口可以通过引入advice来实现吗？在添加IntroductionAdvisor之前调用。
	 * @throws IllegalArgumentException 如果介绍的advice无法实现建议的接口
	 */
	void validateInterfaces() throws IllegalArgumentException;

}
