/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;

/**
 * Web 应用类型的枚举。
 * <p>
 * 可通过 {@link #deduce()} 从类路径推断，或由 {@link SpringApplication} 显式指定。
 *
 * @author Andy Wilkinson
 * @author Brian Clozel
 * @author Phillip Webb
 * @since 2.0.0
 */
public enum WebApplicationType {

	/**
	 * 非 Web 应用，不启动嵌入式 Web 服务器。
	 */
	NONE,

	/**
	 * Servlet 风格 Web 应用，启动嵌入式 Servlet Web 服务器。
	 */
	SERVLET,

	/**
	 * 响应式 Web 应用，启动嵌入式响应式 Web 服务器。
	 */
	REACTIVE;

	private static final String[] SERVLET_INDICATOR_CLASSES = { "jakarta.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext" };

	/**
	 * 从当前类路径推断 {@link WebApplicationType}。
	 *
	 * @return 推断出的 Web 应用类型
	 * @since 4.0.1
	 */
	public static WebApplicationType deduce() {
		for (Deducer deducer : SpringFactoriesLoader.forDefaultResourceLocation().load(Deducer.class)) {
			WebApplicationType deduced = deducer.deduceWebApplicationType();
			if (deduced != null) {
				return deduced;
			}
		}
		return isServletApplication() ? WebApplicationType.SERVLET : WebApplicationType.NONE;
	}

	private static boolean isServletApplication() {
		for (String servletIndicatorClass : SERVLET_INDICATOR_CLASSES) {
			if (!ClassUtils.isPresent(servletIndicatorClass, null)) {
				return false;
			}
		}
		return true;
	}

	static class WebApplicationTypeRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
			for (String servletIndicatorClass : SERVLET_INDICATOR_CLASSES) {
				registerTypeIfPresent(servletIndicatorClass, classLoader, hints);
			}
		}

		private void registerTypeIfPresent(String typeName, @Nullable ClassLoader classLoader, RuntimeHints hints) {
			if (ClassUtils.isPresent(typeName, classLoader)) {
				hints.reflection().registerType(TypeReference.of(typeName));
			}
		}

	}

	/**
	 * 可由模块实现的策略，用于推断 {@link WebApplicationType}。
	 *
	 * @since 4.0.1
	 */
	@FunctionalInterface
	public interface Deducer {

		/**
		 * 推断 Web 应用类型。
		 *
		 * @return 推断出的类型，或 {@code null}
		 */
		@Nullable WebApplicationType deduceWebApplicationType();

	}

}
