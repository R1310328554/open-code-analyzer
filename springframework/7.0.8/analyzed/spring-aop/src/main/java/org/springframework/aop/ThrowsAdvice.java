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
 * 异常抛出通知的标记接口。
 *
 * <p>本接口不含任何方法，方法通过反射调用。
 * 实现类须实现如下形式的方法：
 *
 * <pre class="code">void afterThrowing([Method, args, target], ThrowableSubclass);</pre>
 *
 * <p>有效方法示例：
 *
 * <pre class="code">public void afterThrowing(Exception ex)</pre>
 * <pre class="code">public void afterThrowing(RemoteException ex)</pre>
 * <pre class="code">public void afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre>
 * <pre class="code">public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)</pre>
 *
 * <p>前三个参数可选，仅在需要连接点额外信息时有用，
 * 类似 AspectJ 的 <b>after-throwing</b> 通知。
 *
 * <p><b>注意：</b>若 throws-advice 方法自身抛出异常，
 * 将覆盖原始异常（即改变抛给调用者的异常）。
 * 覆盖异常通常为 RuntimeException，与任意方法签名兼容。
 * 但若 throws-advice 方法抛出受检异常，
 * 则须与目标方法声明的异常匹配，
 * 因此在一定程度上与特定目标方法签名耦合。
 * <b>切勿抛出与目标方法签名不兼容的未声明受检异常！</b>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AfterReturningAdvice
 * @see MethodBeforeAdvice
 */
public interface ThrowsAdvice extends AfterAdvice {

}
