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

package org.springframework.instrument.classloading;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.instrument.InstrumentationSavingAgent;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 依赖虚拟机 {@link Instrumentation} 的 {@link LoadTimeWeaver}。
 *
 * <p>启动 JVM 时需指定要使用的 Java 代理 &mdash; 例如，其中
 * <code>spring-instrument-{version}.jar</code> 是随 Spring 提供的、
 * 包含 {@link InstrumentationSavingAgent} 类的 JAR 文件，
 * <code>{version}</code> 为 Spring Framework 的发布版本
 *（例如 {@code 5.1.5.RELEASE}）。
 *
 * <p><code>-javaagent:path/to/spring-instrument-{version}.jar</code>
 *
 * <p>例如在 Eclipse 中，可在 Eclipse「运行配置」的 JVM 参数中添加类似以下内容：
 *
 * <p><code>-javaagent:${project_loc}/lib/spring-instrument-{version}.jar</code>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see InstrumentationSavingAgent
 */
public class InstrumentationLoadTimeWeaver implements LoadTimeWeaver {

	private static final boolean AGENT_CLASS_PRESENT = ClassUtils.isPresent(
			"org.springframework.instrument.InstrumentationSavingAgent",
			InstrumentationLoadTimeWeaver.class.getClassLoader());


	private final @Nullable ClassLoader classLoader;

	private final @Nullable Instrumentation instrumentation;

	private final List<ClassFileTransformer> transformers = new ArrayList<>(4);


	/**
	 * 为默认 ClassLoader 创建新的 InstrumentationLoadTimeWeaver。
	 */
	public InstrumentationLoadTimeWeaver() {
		this(ClassUtils.getDefaultClassLoader());
	}

	/**
	 * 为给定 ClassLoader 创建新的 InstrumentationLoadTimeWeaver。
	 * @param classLoader 已注册转换器应适用的 ClassLoader
	 */
	public InstrumentationLoadTimeWeaver(@Nullable ClassLoader classLoader) {
		this.classLoader = classLoader;
		this.instrumentation = getInstrumentation();
	}


	@Override
	public void addTransformer(ClassFileTransformer transformer) {
		Assert.notNull(transformer, "Transformer must not be null");
		FilteringClassFileTransformer actualTransformer =
				new FilteringClassFileTransformer(transformer, this.classLoader);
		synchronized (this.transformers) {
			Assert.state(this.instrumentation != null,
					"Must start with Java agent to use InstrumentationLoadTimeWeaver. See Spring documentation.");
			this.instrumentation.addTransformer(actualTransformer);
			this.transformers.add(actualTransformer);
		}
	}

	/**
	 * 以这种方式启动 JVM 时，我们可以织入当前类加载器，
	 * 因此可织入的类加载器始终是当前加载器。
	 */
	@Override
	public ClassLoader getInstrumentableClassLoader() {
		Assert.state(this.classLoader != null, "No ClassLoader available");
		return this.classLoader;
	}

	/**
	 * 本实现始终返回 {@link SimpleThrowawayClassLoader}。
	 */
	@Override
	public ClassLoader getThrowawayClassLoader() {
		return new SimpleThrowawayClassLoader(getInstrumentableClassLoader());
	}

	/**
	 * 按注册顺序的逆序移除所有已注册的转换器。
	 */
	public void removeTransformers() {
		synchronized (this.transformers) {
			if (this.instrumentation != null && !this.transformers.isEmpty()) {
				for (int i = this.transformers.size() - 1; i >= 0; i--) {
					this.instrumentation.removeTransformer(this.transformers.get(i));
				}
				this.transformers.clear();
			}
		}
	}


	/**
	 * 检查当前虚拟机是否可用 Instrumentation 实例。
	 * @see #getInstrumentation()
	 */
	public static boolean isInstrumentationAvailable() {
		return (getInstrumentation() != null);
	}

	/**
	 * 获取当前虚拟机的 Instrumentation 实例（若可用）。
	 * @return Instrumentation 实例，若未找到则为 {@code null}
	 * @see #isInstrumentationAvailable()
	 */
	private static @Nullable Instrumentation getInstrumentation() {
		if (AGENT_CLASS_PRESENT) {
			return InstrumentationAccessor.getInstrumentation();
		}
		else {
			return null;
		}
	}


	/**
	 * 内部类，用于避免对 InstrumentationSavingAgent 的依赖。
	 */
	private static class InstrumentationAccessor {

		public static Instrumentation getInstrumentation() {
			return InstrumentationSavingAgent.getInstrumentation();
		}
	}


	/**
	 * 装饰器，仅将给定目标转换器应用于特定 ClassLoader。
	 */
	private static class FilteringClassFileTransformer implements ClassFileTransformer {

		private final ClassFileTransformer targetTransformer;

		private final @Nullable ClassLoader targetClassLoader;

		public FilteringClassFileTransformer(
				ClassFileTransformer targetTransformer, @Nullable ClassLoader targetClassLoader) {

			this.targetTransformer = targetTransformer;
			this.targetClassLoader = targetClassLoader;
		}

		@Override
		public byte @Nullable [] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

			if (this.targetClassLoader != loader) {
				return null;
			}
			return this.targetTransformer.transform(
					loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		}

		@Override
		public String toString() {
			return "FilteringClassFileTransformer for: " + this.targetTransformer;
		}
	}

}
