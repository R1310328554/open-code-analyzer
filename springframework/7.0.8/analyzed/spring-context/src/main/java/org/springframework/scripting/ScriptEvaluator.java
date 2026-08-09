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

import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Spring 用于求值脚本的策略接口。
 *
 * <p>除各语言专用实现外，Spring 还提供基于标准
 * {@code javax.script} 包（JSR-223）的实现：
 * {@link org.springframework.scripting.support.StandardScriptEvaluator}。
 *
 * @author Juergen Hoeller
 * @author Costin Leau
 * @since 4.0
 */
public interface ScriptEvaluator {

	/**
	 * 求值给定脚本。
	 * @param script 待求值脚本的 ScriptSource
	 * @return 脚本返回值（若有）
	 * @throws ScriptCompilationException 若读取、编译或求值失败
	 */
	@Nullable Object evaluate(ScriptSource script) throws ScriptCompilationException;

	/**
	 * 以给定参数求值脚本。
	 * @param script 待求值脚本的 ScriptSource
	 * @param arguments 暴露给脚本的键值对，通常作为脚本变量（可为 {@code null} 或空）
	 * @return 脚本返回值（若有）
	 * @throws ScriptCompilationException 若读取、编译或求值失败
	 */
	@Nullable Object evaluate(ScriptSource script, @Nullable Map<String, Object> arguments) throws ScriptCompilationException;

}
