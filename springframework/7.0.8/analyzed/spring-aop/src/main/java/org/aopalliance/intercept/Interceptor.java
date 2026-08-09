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

package org.aopalliance.intercept;

import org.aopalliance.aop.Advice;

/**
 * 该接口代表一个通用拦截器。 <p>A 通用拦截器可以拦截基本程序中发生的运行时事件。这些事件由连接点化（具体化）。运行时连接点可以是调用、字段访问、异常......
 * <p>这个接口不直接使用。使用子接口来拦截特定事件。例如，以下类实现一些特定拦截器以实现调试器： <pre class=code> 类 DebuggingInterceptor
 * 实现MethodInterceptor、ConstructorInterceptor { 对象调用(MethodInvocau i) throws Throwable {
 * debug(i.getMethod(), i.getThis(), i.getArgs());返回 i.proceed(); } }
 * 构造对象（ConstructorInitation i）发送Throwable {
 * debug（i.getConstructor（），i.getThis（），i.getArgs（））;返回 i.proceed(); } void
 * debug(AccessibleObject ao, 对象 this, 对象值) { ... } } </pre>
 * @author Rod Johnson
 * @see Joinpoint
 */
public interface Interceptor extends Advice {

}
