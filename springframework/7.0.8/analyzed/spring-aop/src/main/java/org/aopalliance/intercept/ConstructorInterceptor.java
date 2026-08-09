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

/**
 * <p>用户应实现{@link
 * #construct(ConstructorInvocation)}方法来原始行为。例如，下面的类实现了一个单实例拦截器（只允许被拦截的类有一个唯一的实例）： <pre
 * class=code> 类 DebuggingInterceptor 实现 ConstructorInterceptor { 对象实例=null; 拦截器修改新对象的构造。
 * 对象构造（ConstructorInitation i）抛出Throwable { if（instance==null）{ return instance=i.proceed（
 * ）; } else { throw new Exception("单例不允许多实例"); </pre>
 * @author Rod Johnson
 */
public interface ConstructorInterceptor extends Interceptor {

	/**
	* 实现此方法可以在构造新对象之前和之后执行额外的处理。礼貌的实现肯定会调用 {@link Joinpoint#proceed()}。
	* @param invocation 施工连接点
	* @return 新创建的对象，也是调用{@link Joinpoint#proceed()}的结果；可能会被拦截器取代
	* @throws Throwable 如果拦截器或目标对象抛出异常
	*/
	Object construct(ConstructorInvocation invocation) throws Throwable;

}
