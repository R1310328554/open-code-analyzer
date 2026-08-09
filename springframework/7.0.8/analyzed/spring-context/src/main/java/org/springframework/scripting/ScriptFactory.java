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

package org.springframework.scripting;

import java.io.IOException;

import org.jspecify.annotations.Nullable;

/**
 * 脚本定义接口，封装特定脚本的配置以及
 * 创建实际脚本化 Java {@code Object} 的工厂方法。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see #getScriptSourceLocator
 * @see #getScriptedObject
 */
public interface ScriptFactory {

	/**
	 * 返回指向脚本来源的定位符，由实际创建脚本的后处理器解释。
	 * <p>常见定位符包括 Spring 资源位置
	 *（如 "file:C:/myScript.bsh" 或 "classpath:myPackage/myScript.bsh"）
	 * 以及内联脚本（"inline:myScriptText..."）。
	 * @return 脚本来源定位符
	 * @see org.springframework.scripting.support.ScriptFactoryPostProcessor#convertToScriptSource
	 * @see org.springframework.core.io.ResourceLoader
	 */
	String getScriptSourceLocator();

	/**
	 * 返回脚本应实现的业务接口。
	 * <p>若脚本自行决定 Java 接口（如 Groovy）可返回 {@code null}。
	 * @return 脚本接口
	 */
	Class<?> @Nullable [] getScriptInterfaces();

	/**
	 * 返回脚本是否需要为其生成配置接口。
	 * 通常适用于无法自行确定 Java 签名且 {@code getScriptInterfaces()} 中
	 * 未指定合适配置接口的脚本。
	 * @return 是否需要生成配置接口
	 * @see #getScriptInterfaces()
	 */
	boolean requiresConfigInterface();

	/**
	 * 创建脚本化 Java 对象的工厂方法。
	 * <p>建议实现缓存脚本元数据（如生成的脚本类）。
	 * 本方法可能被并发调用，须线程安全实现。
	 * @param scriptSource 获取脚本文本的 ScriptSource（永不为 {@code null}）
	 * @param actualInterfaces 要暴露的实际接口，含脚本接口及生成的配置接口（若适用；可为 {@code null}）
	 * @return 脚本化 Java 对象
	 * @throws IOException 若脚本获取失败
	 * @throws ScriptCompilationException 若脚本编译失败
	 */
	@Nullable Object getScriptedObject(ScriptSource scriptSource, Class<?> @Nullable ... actualInterfaces)
			throws IOException, ScriptCompilationException;

	/**
	 * 确定脚本化 Java 对象的类型。
	 * <p>建议实现缓存脚本元数据（如生成的脚本类）。
	 * 本方法可能被并发调用，须线程安全实现。
	 * @param scriptSource 获取脚本文本的 ScriptSource（永不为 {@code null}）
	 * @return 脚本化 Java 对象的类型；若无法确定则为 {@code null}
	 * @throws IOException 若脚本获取失败
	 * @throws ScriptCompilationException 若脚本编译失败
	 * @since 2.0.3
	 */
	@Nullable Class<?> getScriptedObjectType(ScriptSource scriptSource)
			throws IOException, ScriptCompilationException;

	/**
	 * 判断是否需要刷新（例如通过 ScriptSource 的 {@code isModified()}）。
	 * @param scriptSource 获取脚本文本的 ScriptSource（永不为 {@code null}）
	 * @return 是否需要重新调用 {@link #getScriptedObject}
	 * @since 2.5.2
	 * @see ScriptSource#isModified()
	 */
	boolean requiresScriptedObjectRefresh(ScriptSource scriptSource);

}
