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
 * 标记 AOP 代理接口（尤其是引介接口），
 * 明确表示要返回原始目标对象
 * （方法调用返回时通常会被替换为代理对象）。
 *
 * <p>本接口是 {@link java.io.Serializable} 风格的标记接口，
 * 语义上作用于声明的接口，而非具体对象的完整类。
 * 换言之，该标记仅适用于特定接口
 * （通常是不作为 AOP 代理主接口的引介接口），
 * 因此不影响具体 AOP 代理可能实现的其他接口。
 *
 * @author Juergen Hoeller
 * @since 2.0.5
 * @see org.springframework.aop.scope.ScopedObject
 */
public interface RawTargetAccess {

}
