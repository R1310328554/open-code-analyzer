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
 * <p> 该接口上没有任何方法，因为方法是通过引用调用的。 实现类必须实现以下形式的方法： <pre class="code">void
 * afterThrowing([method, args, goal], ThrowableSubclass);</pre> <p> 有效方法的一些示例是： <pre
 * class="code">public void afterThrowing(Exception ex)</pre> <pre class="code">public void
 * afterThrowing(RemoteException ex)</pre> <pre class="code">public void
 * afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre> <pre
 * class="code">public void afterThrowing(Method method, Object[]
 * args、对象目标、ServletException前)</pre> <p> 前三个参数是可选的，仅当我们需要有关连接点的更多信息时才用，如 AspectJ <b>after-
 * throws </b> 建议中所示。 <p><b> 注：</b> 如果抛出建议它本身发送异常，会覆盖原始异常（即更改向用户发送的异常）。重写异常通常是
 * RuntimeException；这与任何方法签名兼容。但是，如果 throws-advice
 * 方法发送已检查的异常，则必须与目标方法的异常相匹配，因此声明在某种与特定的目标方法签名连接。 <b> 不要转发与目标方法的签名不兼容的未声明的检查异常！</b>
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AfterReturningAdvice
 * @see MethodBeforeAdvice
 */
public interface ThrowsAdvice extends AfterAdvice {

}
