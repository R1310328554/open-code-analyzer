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

package org.springframework.scripting.bsh;

import java.io.IOException;

import bsh.EvalError;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * BeanShell 脚本的 {@link org.springframework.scripting.ScriptFactory} 实现。
 *
 * <p>通常与 {@link org.springframework.scripting.support.ScriptFactoryPostProcessor}
 * 配合使用；配置示例见后者的 JavaDoc。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see BshScriptUtils
 * @see org.springframework.scripting.support.ScriptFactoryPostProcessor
 * @deprecated 无替代方案，已不再积极维护
 */
@Deprecated(since = "7.0")
public class BshScriptFactory implements ScriptFactory, BeanClassLoaderAware {

	private final String scriptSourceLocator;

	private final Class<?> @Nullable [] scriptInterfaces;

	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private @Nullable Class<?> scriptClass;

	private final Object scriptClassMonitor = new Object();

	private boolean wasModifiedForTypeCheck = false;


	/**
	 * 为给定脚本来源创建新的 BshScriptFactory。
	 * <p>此 {@code BshScriptFactory} 变体要求脚本声明完整类
	 * 或返回脚本化对象的实际实例。
	 * @param scriptSourceLocator 指向脚本来源的定位符，由实际创建脚本的后处理器解释。
	 */
	public BshScriptFactory(String scriptSourceLocator) {
		Assert.hasText(scriptSourceLocator, "'scriptSourceLocator' must not be empty");
		this.scriptSourceLocator = scriptSourceLocator;
		this.scriptInterfaces = null;
	}

	/**
	 * 为给定脚本来源创建新的 BshScriptFactory。
	 * <p>脚本可以是需生成对应代理（实现指定接口）的简单脚本，
	 * 也可声明完整类或返回脚本化对象实例
	 *（此时指定接口须由该类/实例实现）。
	 * @param scriptSourceLocator 指向脚本来源的定位符，由实际创建脚本的后处理器解释。
	 * @param scriptInterfaces 脚本化对象应实现的 Java 接口（可为 {@code null}）
	 */
	public BshScriptFactory(String scriptSourceLocator, Class<?> @Nullable ... scriptInterfaces) {
		Assert.hasText(scriptSourceLocator, "'scriptSourceLocator' must not be empty");
		this.scriptSourceLocator = scriptSourceLocator;
		this.scriptInterfaces = scriptInterfaces;
	}


	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}


	@Override
	public String getScriptSourceLocator() {
		return this.scriptSourceLocator;
	}

	@Override
	public Class<?> @Nullable [] getScriptInterfaces() {
		return this.scriptInterfaces;
	}

	/**
	 * BeanShell 脚本需要配置接口。
	 */
	@Override
	public boolean requiresConfigInterface() {
		return true;
	}

	/**
	 * 通过 {@link BshScriptUtils} 加载并解析 BeanShell 脚本。
	 * @see BshScriptUtils#createBshObject(String, Class[], ClassLoader)
	 */
	@Override
	public @Nullable Object getScriptedObject(ScriptSource scriptSource, Class<?> @Nullable ... actualInterfaces)
			throws IOException, ScriptCompilationException {

		Class<?> clazz;

		try {
			synchronized (this.scriptClassMonitor) {
				boolean requiresScriptEvaluation = (this.wasModifiedForTypeCheck && this.scriptClass == null);
				this.wasModifiedForTypeCheck = false;

				if (scriptSource.isModified() || requiresScriptEvaluation) {
					// New script content: Let's check whether it evaluates to a Class.
					Object result = BshScriptUtils.evaluateBshScript(
							scriptSource.getScriptAsString(), actualInterfaces, this.beanClassLoader);
					if (result instanceof Class<?> type) {
						// A Class: We'll cache the Class here and create an instance
						// outside the synchronized block.
						this.scriptClass = type;
					}
					else {
						// Not a Class: OK, we'll simply create BeanShell objects
						// through evaluating the script for every call later on.
						// For this first-time check, let's simply return the
						// already evaluated object.
						return result;
					}
				}
				clazz = this.scriptClass;
			}
		}
		catch (EvalError ex) {
			this.scriptClass = null;
			throw new ScriptCompilationException(scriptSource, ex);
		}

		if (clazz != null) {
			// A Class: We need to create an instance for every call.
			try {
				return ReflectionUtils.accessibleConstructor(clazz).newInstance();
			}
			catch (Throwable ex) {
				throw new ScriptCompilationException(
						scriptSource, "Could not instantiate script class: " + clazz.getName(), ex);
			}
		}
		else {
			// Not a Class: We need to evaluate the script for every call.
			try {
				return BshScriptUtils.createBshObject(
						scriptSource.getScriptAsString(), actualInterfaces, this.beanClassLoader);
			}
			catch (EvalError ex) {
				throw new ScriptCompilationException(scriptSource, ex);
			}
		}
	}

	@Override
	public @Nullable Class<?> getScriptedObjectType(ScriptSource scriptSource)
			throws IOException, ScriptCompilationException {

		synchronized (this.scriptClassMonitor) {
			try {
				if (scriptSource.isModified()) {
					// New script content: Let's check whether it evaluates to a Class.
					this.wasModifiedForTypeCheck = true;
					this.scriptClass = BshScriptUtils.determineBshObjectType(
							scriptSource.getScriptAsString(), this.beanClassLoader);
				}
				return this.scriptClass;
			}
			catch (EvalError ex) {
				this.scriptClass = null;
				throw new ScriptCompilationException(scriptSource, ex);
			}
		}
	}

	@Override
	public boolean requiresScriptedObjectRefresh(ScriptSource scriptSource) {
		synchronized (this.scriptClassMonitor) {
			return (scriptSource.isModified() || this.wasModifiedForTypeCheck);
		}
	}


	@Override
	public String toString() {
		return "BshScriptFactory: script source locator [" + this.scriptSourceLocator + "]";
	}

}
