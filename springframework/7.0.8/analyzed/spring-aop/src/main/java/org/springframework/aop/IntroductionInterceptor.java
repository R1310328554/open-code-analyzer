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

import org.aopalliance.intercept.MethodInterceptor;

/**
 * AOP 联盟 MethodInterceptor 的子接口，允许拦截器实现其他接口，并通过使用该拦截器的代理可用。这是一个基本的 AOP 概念，称为 <b> 简介 </b>。 
 * <p>引入通常是<b>mixins</b>，使得能够构建可以实现Java中多重继承的许多目标的复合对象。
 * @author Rod Johnson
 * @see DynamicIntroductionAdvice
 */
public interface IntroductionInterceptor extends MethodInterceptor, DynamicIntroductionAdvice {

}
