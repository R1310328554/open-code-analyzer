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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.Primitive;
import bsh.XThis;
import org.jspecify.annotations.Nullable;

import org.springframework.core.NestedRuntimeException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 处理 BeanShell 脚本化对象的实用方法。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @deprecated 无替代方案，已不再积极维护
 */
@Deprecated(since = "7.0")
public abstract class BshScriptUtils {

	/**
	 * 从给定脚本文本创建新的 BeanShell 脚本化对象。
	 * <p>此 {@code createBshObject} 变体要求脚本声明完整类
	 * 或返回脚本化对象的实际实例。
	 * @param scriptSource 脚本文本
	 * @return 脚本化 Java 对象
	 * @throws EvalError BeanShell 解析失败时
	 */
	public static Object createBshObject(String scriptSource) throws EvalError {
		return createBshObject(scriptSource, null, null);
	}

	/**
	 * 使用默认 ClassLoader 从给定脚本文本创建 BeanShell 脚本化对象。
	 * <p>脚本可以是需生成对应代理的简单脚本，也可声明完整类或返回实例
	 *（此时指定接口须由该类/实例实现）。
	 * @param scriptSource 脚本文本
	 * @param scriptInterfaces 脚本化 Java 对象应实现的接口
	 *（若脚本自行声明完整类或返回实例，可为 {@code null} 或空）
	 * @return 脚本化 Java 对象
	 * @throws EvalError BeanShell 解析失败时
	 * @see #createBshObject(String, Class[], ClassLoader)
	 */
	public static Object createBshObject(String scriptSource, Class<?> @Nullable ... scriptInterfaces) throws EvalError {
		return createBshObject(scriptSource, scriptInterfaces, ClassUtils.getDefaultClassLoader());
	}

	/**
	 * 从给定脚本文本创建 BeanShell 脚本化对象。
	 * <p>脚本可以是需生成对应代理的简单脚本，也可声明完整类或返回实例
	 *（此时指定接口须由该类/实例实现）。
	 * @param scriptSource 脚本文本
	 * @param scriptInterfaces 脚本化 Java 对象应实现的接口
	 *（若脚本自行声明完整类或返回实例，可为 {@code null} 或空）
	 * @param classLoader 用于求值脚本的 ClassLoader
	 * @return 脚本化 Java 对象
	 * @throws EvalError BeanShell 解析失败时
	 */
	public static Object createBshObject(String scriptSource, Class<?> @Nullable [] scriptInterfaces, @Nullable ClassLoader classLoader)
			throws EvalError {

		Object result = evaluateBshScript(scriptSource, scriptInterfaces, classLoader);
		if (result instanceof Class<?> clazz) {
			try {
				return ReflectionUtils.accessibleConstructor(clazz).newInstance();
			}
			catch (Throwable ex) {
				throw new IllegalStateException("Could not instantiate script class: " + clazz.getName(), ex);
			}
		}
		else {
			return result;
		}
	}

	/**
	 * 根据给定脚本文本求值 BeanShell 脚本，返回脚本定义的 Class。
	 * <p>脚本可声明完整类或返回脚本化对象实例（此时返回该对象的 Class）；
	 * 其他情况返回 {@code null}。
	 * @param scriptSource 脚本文本
	 * @param classLoader 用于求值脚本的 ClassLoader
	 * @return 脚本化 Java 类；若无法确定则为 {@code null}
	 * @throws EvalError BeanShell 解析失败时
	 */
	static @Nullable Class<?> determineBshObjectType(String scriptSource, @Nullable ClassLoader classLoader) throws EvalError {
		Assert.hasText(scriptSource, "Script source must not be empty");
		Interpreter interpreter = new Interpreter();
		if (classLoader != null) {
			interpreter.setClassLoader(classLoader);
		}
		Object result = interpreter.eval(scriptSource);
		if (result instanceof Class<?> clazz) {
			return clazz;
		}
		else if (result != null) {
			return result.getClass();
		}
		else {
			return null;
		}
	}

	/**
	 * 根据给定脚本文本求值 BeanShell 脚本，原样保留返回的 Class 或 Object。
	 * <p>脚本可以是需生成对应代理的简单脚本，也可声明完整类或返回实例
	 *（此时指定接口须由该类/实例实现）。
	 * @param scriptSource 脚本文本
	 * @param scriptInterfaces 脚本化 Java 对象应实现的接口
	 * @param classLoader 用于求值脚本的 ClassLoader
	 * @return 脚本化 Java 类或 Java 对象
	 * @throws EvalError BeanShell 解析失败时
	 */
	static Object evaluateBshScript(
			String scriptSource, Class<?> @Nullable [] scriptInterfaces, @Nullable ClassLoader classLoader)
			throws EvalError {

		Assert.hasText(scriptSource, "Script source must not be empty");
		Interpreter interpreter = new Interpreter();
		interpreter.setClassLoader(classLoader);
		Object result = interpreter.eval(scriptSource);
		if (result != null) {
			return result;
		}
		else {
			// Simple BeanShell script: Let's create a proxy for it, implementing the given interfaces.
			if (ObjectUtils.isEmpty(scriptInterfaces)) {
				throw new IllegalArgumentException("Given script requires a script proxy: " +
						"At least one script interface is required.\nScript: " + scriptSource);
			}
			XThis xt = (XThis) interpreter.eval("return this");
			return Proxy.newProxyInstance(classLoader, scriptInterfaces, new BshObjectInvocationHandler(xt));
		}
	}


	/**
	 * 调用 BeanShell 脚本方法的 InvocationHandler。
	 */
	private static class BshObjectInvocationHandler implements InvocationHandler {

		private final XThis xt;

		public BshObjectInvocationHandler(XThis xt) {
			this.xt = xt;
		}

		@Override
		public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (ReflectionUtils.isEqualsMethod(method)) {
				return (isProxyForSameBshObject(args[0]));
			}
			else if (ReflectionUtils.isHashCodeMethod(method)) {
				return this.xt.hashCode();
			}
			else if (ReflectionUtils.isToStringMethod(method)) {
				return "BeanShell object [" + this.xt + "]";
			}
			try {
				Object result = this.xt.invokeMethod(method.getName(), args);
				if (result == Primitive.NULL || result == Primitive.VOID) {
					return null;
				}
				if (result instanceof Primitive primitive) {
					return primitive.getValue();
				}
				return result;
			}
			catch (EvalError ex) {
				throw new BshExecutionException(ex);
			}
		}

		private boolean isProxyForSameBshObject(Object obj) {
			if (!Proxy.isProxyClass(obj.getClass())) {
				return false;
			}
			InvocationHandler ih = Proxy.getInvocationHandler(obj);
			return (ih instanceof BshObjectInvocationHandler that && this.xt.equals(that.xt));
		}
	}


	/**
	 * 脚本执行失败时抛出的异常。
	 */
	@SuppressWarnings("serial")
	public static final class BshExecutionException extends NestedRuntimeException {

		private BshExecutionException(EvalError ex) {
			super("BeanShell script execution failed", ex);
		}
	}

}
