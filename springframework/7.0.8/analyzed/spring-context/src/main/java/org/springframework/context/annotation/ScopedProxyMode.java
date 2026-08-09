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
 * 枚举各种作用域代理选项。
 *
 * <p>关于作用域代理的完整说明，请参阅 Spring 参考文档
 * <a href="https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html#beans-factory-scopes-other-injection"
 * >Scoped Beans as Dependencies</a> 章节。
 *
 * @author Mark Fisher
 * @since 2.5
 * @see ScopeMetadata
 */
public enum ScopedProxyMode {

	/**
	 * 默认值通常等于 {@link #NO}，除非在组件扫描指令级别配置了不同默认值。
	 */
	DEFAULT,

	/**
	 * 不创建作用域代理。
	 * <p>与非单例作用域实例配合时通常意义不大；若作为依赖注入，
	 * 应优先使用 {@link #INTERFACES} 或 {@link #TARGET_CLASS} 代理模式。
	 */
	NO,

	/**
	 * 创建 JDK 动态代理，实现目标对象类暴露的<i>全部</i>接口。
	 */
	INTERFACES,

	/**
	 * 创建基于类的代理（使用 CGLIB）。
	 */
	TARGET_CLASS

}
