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

/* ===== [OCA 中文解析] =====
文件意图总览

注解配置应用上下文的通用接口，定义 {@link #register} 与 {@link #scan} 方法。
===== [OCA 中文解析结束] ===== */
package org.springframework.context.annotation;

import org.springframework.beans.factory.BeanRegistrar;

/* ===== [OCA 中文解析] =====
interface AnnotationConfigRegistry — 意图说明

为 {@code AnnotationConfigApplicationContext} 等上下文提供编程式注册与扫描入口。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Common interface for annotation config application contexts,
 * defining {@link #register} and {@link #scan} methods.
 *
 * @author Juergen Hoeller
 * @since 4.1
 */
public interface AnnotationConfigRegistry {

	/**
	 * Invoke the given registrars for registering their beans with this
	 * application context.
	 * <p>This can be used to register custom beans without inferring
	 * annotation-based characteristics for primary/fallback/lazy-init,
	 * rather specifying those programmatically if needed.
	 * @param registrars one or more {@link BeanRegistrar} instances
	 * @since 7.0
	 * @see #register(Class[])
	 */
	void register(BeanRegistrar... registrars);

	/**
	 * Register one or more component classes to be processed, inferring
	 * annotation-based characteristics for primary/fallback/lazy-init
	 * just like for scanned component classes.
	 * <p>Calls to {@code register} are idempotent; adding the same
	 * component class more than once has no additional effect.
	 * @param componentClasses one or more component classes,
	 * for example, {@link Configuration @Configuration} classes
	 * @see #scan(String...)
	 */
	void register(Class<?>... componentClasses);

	/**
	 * Perform a scan within the specified base packages.
	 * @param basePackages the packages to scan for component classes
	 * @see #register(Class[])
	 */
	void scan(String... basePackages);

}
