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
 * 用于显式返回原始目标对象（从方法调用返回时通常会被代理对象替换）的 AOP 代理接口（特别是：引入接口）的标记。
 * <p>注意，这是 {@link java.io.Serializable} 风格的标记接口，语义上应用于声明的接口而不是具体对象的完整类。换句话说，该标记仅适用于特定接口（通常
 * 是不作为 AOP 代理的主要接口的引入接口），因此不会影响具体 AOP 代理可能实现的其他接口。
 * @author Juergen Hoeller
 * @since 2.0.5
 * @see org.springframework.aop.scope.ScopedObject
 */
public interface RawTargetAccess {

}
