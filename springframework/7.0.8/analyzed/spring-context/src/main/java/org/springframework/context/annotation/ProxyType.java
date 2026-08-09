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

package org.springframework.context.annotation;

/**
 * 指示所需代理类型的公共枚举。
 *
 * @author Juergen Hoeller
 * @since 7.0
 * @see Proxyable#value()
 */
public enum ProxyType {

	/**
	 * 默认为 JDK 动态代理；若全局配置为基于类，则可能使用 CGLIB 代理。
	 */
	DEFAULT,

	/**
	 * 建议使用 JDK 动态代理，实现目标对象类暴露的<em>全部</em>接口。
	 * 覆盖全局默认配置。
	 */
	INTERFACES,

	/**
	 * 建议使用基于类的 CGLIB 代理。覆盖全局默认配置。
	 */
	TARGET_CLASS

}
