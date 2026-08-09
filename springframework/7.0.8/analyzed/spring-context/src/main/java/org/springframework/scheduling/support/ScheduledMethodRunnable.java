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

package org.springframework.scheduling.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Supplier;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.util.ReflectionUtils;

/**
 * {@link MethodInvokingRunnable} 的变体，用于处理无参调度方法。
 * 将用户异常传播给调用方，前提是已为 Runnable 配置错误处理策略。
 *
 * @author Juergen Hoeller
 * @author Brian Clozel
 * @since 3.0.6
 * @see org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor
 */
public class ScheduledMethodRunnable implements SchedulingAwareRunnable {

	private static final ScheduledTaskObservationConvention DEFAULT_CONVENTION =
			new DefaultScheduledTaskObservationConvention();

	private final Object target;

	private final Method method;

	private final @Nullable String qualifier;

	private final Supplier<ObservationRegistry> observationRegistrySupplier;


	/**
	 * 为给定目标实例创建 {@code ScheduledMethodRunnable}，调用指定方法。
	 * @param target 要调用方法的目标实例
	 * @param method 要调用的目标方法
	 * @param qualifier 与本 Runnable 关联的限定符，
	 * 例如用于确定运行该调度方法的调度器
	 * @param observationRegistrySupplier 观测注册表的供应器
	 * @since 6.1
	 */
	public ScheduledMethodRunnable(Object target, Method method, @Nullable String qualifier,
			Supplier<ObservationRegistry> observationRegistrySupplier) {

		this.target = target;
		this.method = method;
		this.qualifier = qualifier;
		this.observationRegistrySupplier = observationRegistrySupplier;
	}

	/**
	 * 为给定目标实例创建 {@code ScheduledMethodRunnable}，调用指定方法。
	 * @param target 要调用方法的目标实例
	 * @param method 要调用的目标方法
	 */
	public ScheduledMethodRunnable(Object target, Method method) {
		this(target, method, null, () -> ObservationRegistry.NOOP);
	}

	/**
	 * 为给定目标实例创建 {@code ScheduledMethodRunnable}，按名称调用指定方法。
	 * @param target 要调用方法的目标实例
	 * @param methodName 目标方法名
	 * @throws NoSuchMethodException 若指定方法不存在
	 */
	public ScheduledMethodRunnable(Object target, String methodName) throws NoSuchMethodException {
		this(target, target.getClass().getMethod(methodName));
	}


	/**
	 * 返回要调用方法的目标实例。
	 */
	public Object getTarget() {
		return this.target;
	}

	/**
	 * 返回要调用的目标方法。
	 */
	public Method getMethod() {
		return this.method;
	}

	@Override
	public @Nullable String getQualifier() {
		return this.qualifier;
	}


	@Override
	public void run() {
		ScheduledTaskObservationContext context = new ScheduledTaskObservationContext(this.target, this.method);
		Observation observation = ScheduledTaskObservationDocumentation.TASKS_SCHEDULED_EXECUTION.observation(
				null, DEFAULT_CONVENTION,
				() -> context, this.observationRegistrySupplier.get());
		observation.observe(() -> runInternal(context));
	}

	private void runInternal(ScheduledTaskObservationContext context) {
		try {
			ReflectionUtils.makeAccessible(this.method);
			this.method.invoke(this.target);
			context.setComplete(true);
		}
		catch (InvocationTargetException ex) {
			ReflectionUtils.rethrowRuntimeException(ex.getTargetException());
		}
		catch (IllegalAccessException ex) {
			throw new UndeclaredThrowableException(ex);
		}
	}

	@Override
	public String toString() {
		return this.method.getDeclaringClass().getName() + "." + this.method.getName();
	}

}
