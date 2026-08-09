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

package org.springframework.scheduling.annotation;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.CoroutinesUtils;
import org.springframework.core.KotlinDetector;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.scheduling.support.DefaultScheduledTaskObservationConvention;
import org.springframework.scheduling.support.ScheduledTaskObservationContext;
import org.springframework.scheduling.support.ScheduledTaskObservationConvention;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import static org.springframework.scheduling.support.ScheduledTaskObservationDocumentation.TASKS_SCHEDULED_EXECUTION;

/**
 * 供 @{@link ScheduledAnnotationBeanPostProcessor} 使用的辅助类，
 * 在不依赖可选类的情况下支持响应式场景。
 *
 * @author Simon Baslé
 * @author Brian Clozel
 * @since 6.1
 */
abstract class ScheduledAnnotationReactiveSupport {

	static final boolean REACTOR_PRESENT = ClassUtils.isPresent(
			"reactor.core.publisher.Flux", ScheduledAnnotationReactiveSupport.class.getClassLoader());

	static final boolean COROUTINES_REACTOR_PRESENT = ClassUtils.isPresent(
			"kotlinx.coroutines.reactor.MonoKt", ScheduledAnnotationReactiveSupport.class.getClassLoader());

	private static final Log logger = LogFactory.getLog(ScheduledAnnotationReactiveSupport.class);


	/**
	 * 检查响应式方法是否可调度。若方法返回可转换为 {@code Publisher} 的类型
	 * 或为 Kotlin 挂起函数，则视为符合响应式调度条件。
	 * 若不符合，返回 {@code false}。
	 * <p>调度 Kotlin 挂起函数时，运行时须存在 Coroutine-Reactor 桥接
	 * {@code kotlinx.coroutines.reactor}（以 {@code Publisher} 形式调用挂起函数）。
	 * 满足条件时返回 {@code true}，否则抛出 {@code IllegalStateException}。
	 * @throws IllegalStateException 方法为响应式但运行时缺少 Reactor 和/或 Kotlin 协程桥接
	 */
	public static boolean isReactive(Method method) {
		if (KotlinDetector.isSuspendingFunction(method)) {
			// Note that suspending functions declared without args have a single Continuation
			// parameter in reflective inspection
			Assert.isTrue(method.getParameterCount() == 1,
					"Kotlin suspending functions may only be annotated with @Scheduled if declared without arguments");
			Assert.isTrue(COROUTINES_REACTOR_PRESENT, "Kotlin suspending functions may only be annotated with " +
					"@Scheduled if the Coroutine-Reactor bridge (kotlinx.coroutines.reactor) is present at runtime");
			return true;
		}
		ReactiveAdapterRegistry registry = ReactiveAdapterRegistry.getSharedInstance();
		if (!registry.hasAdapters()) {
			return false;
		}
		Class<?> returnType = method.getReturnType();
		ReactiveAdapter candidateAdapter = registry.getAdapter(returnType);
		if (candidateAdapter == null) {
			return false;
		}
		Assert.isTrue(method.getParameterCount() == 0,
				"Reactive methods may only be annotated with @Scheduled if declared without arguments");
		Assert.isTrue(candidateAdapter.getDescriptor().isDeferred(),
				"Reactive methods may only be annotated with @Scheduled if the return type supports deferred execution");
		return true;
	}

	/**
	 * 为定时基础设施创建 {@link Runnable}，允许对响应式方法产生的 Publisher 进行定时订阅。
	 * <p>响应式方法仅调用一次，但产生的 {@code Publisher} 在每次 {@code Runnable}
	 * 调用时重复订阅。
	 * <p>固定延迟配置下，{@link Runnable} 内的订阅转为阻塞调用以维持固定延迟语义
	 * （任务阻塞直至 Publisher 完成，延迟应用于下次迭代前）。
	 */
	public static Runnable createSubscriptionRunnable(Method method, Object targetBean, Scheduled scheduled,
			Supplier<ObservationRegistry> observationRegistrySupplier, List<Runnable> subscriptionTrackerRegistry) {

		boolean shouldBlock = (scheduled.fixedDelay() > 0 || StringUtils.hasText(scheduled.fixedDelayString()));
		Publisher<?> publisher = getPublisherFor(method, targetBean);
		Supplier<ScheduledTaskObservationContext> contextSupplier =
				() -> new ScheduledTaskObservationContext(targetBean, method);
		String displayName = targetBean.getClass().getName() + "." + method.getName();
		return new SubscribingRunnable(publisher, shouldBlock, scheduled.scheduler(),
				subscriptionTrackerRegistry, displayName, observationRegistrySupplier, contextSupplier);
	}

	/**
	 * 将给定 {@code Method} 的调用转为 {@code Publisher}：
	 * 反射调用并通过 {@link ReactiveAdapterRegistry} 转换结果，
	 * 或通过 {@link CoroutinesUtils} 将 Kotlin 挂起函数转为 {@code Publisher}。
	 * <p>调用本方法前须通过 {@link #isReactive(Method)} 检查。
	 * 若运行时存在 Reactor，{@code Publisher}  additionally 转为带 checkpoint 字符串的
	 * {@code Flux}，便于调试。
	 */
	static Publisher<?> getPublisherFor(Method method, Object bean) {
		if (KotlinDetector.isSuspendingFunction(method)) {
			return CoroutinesUtils.invokeSuspendingFunction(method, bean, (Object[]) method.getParameters());
		}

		ReactiveAdapterRegistry registry = ReactiveAdapterRegistry.getSharedInstance();
		Class<?> returnType = method.getReturnType();
		ReactiveAdapter adapter = registry.getAdapter(returnType);
		if (adapter == null) {
			throw new IllegalArgumentException("Cannot convert @Scheduled reactive method return type to Publisher");
		}
		if (!adapter.getDescriptor().isDeferred()) {
			throw new IllegalArgumentException("Cannot convert @Scheduled reactive method return type to Publisher: " +
					returnType.getSimpleName() + " is not a deferred reactive type");
		}

		Method invocableMethod = AopUtils.selectInvocableMethod(method, bean.getClass());
		try {
			ReflectionUtils.makeAccessible(invocableMethod);
			Object returnValue = invocableMethod.invoke(bean);

			Publisher<?> publisher = adapter.toPublisher(returnValue);
			// If Reactor is on the classpath, we could benefit from having a checkpoint for debuggability
			if (REACTOR_PRESENT) {
				return Flux.from(publisher).checkpoint(
						"@Scheduled '"+ method.getName() + "()' in '" + method.getDeclaringClass().getName() + "'");
			}
			else {
				return publisher;
			}
		}
		catch (InvocationTargetException ex) {
			throw new IllegalArgumentException(
					"Cannot obtain a Publisher-convertible value from the @Scheduled reactive method",
					ex.getTargetException());
		}
		catch (IllegalAccessException | InaccessibleObjectException ex) {
			throw new IllegalArgumentException(
					"Cannot obtain a Publisher-convertible value from the @Scheduled reactive method", ex);
		}
	}


	/**
	 * 订阅 {@code Publisher} 的 {@code Runnable} 工具实现；
	 * 若 {@code shouldBlock} 为 {@code true} 则订阅后阻塞。
	 */
	static final class SubscribingRunnable implements SchedulingAwareRunnable {

		private static final ScheduledTaskObservationConvention DEFAULT_CONVENTION =
				new DefaultScheduledTaskObservationConvention();

		private final Publisher<?> publisher;

		final boolean shouldBlock;

		final String displayName;

		private final @Nullable String qualifier;

		private final List<Runnable> subscriptionTrackerRegistry;

		final Supplier<ObservationRegistry> observationRegistrySupplier;

		final Supplier<ScheduledTaskObservationContext> contextSupplier;

		SubscribingRunnable(Publisher<?> publisher, boolean shouldBlock,
				@Nullable String qualifier, List<Runnable> subscriptionTrackerRegistry,
				String displayName, Supplier<ObservationRegistry> observationRegistrySupplier,
				Supplier<ScheduledTaskObservationContext> contextSupplier) {

			this.publisher = publisher;
			this.shouldBlock = shouldBlock;
			this.displayName = displayName;
			this.qualifier = qualifier;
			this.subscriptionTrackerRegistry = subscriptionTrackerRegistry;
			this.observationRegistrySupplier = observationRegistrySupplier;
			this.contextSupplier = contextSupplier;
		}

		@Override
		public @Nullable String getQualifier() {
			return this.qualifier;
		}

		@Override
		public void run() {
			Observation observation = TASKS_SCHEDULED_EXECUTION.observation(null, DEFAULT_CONVENTION,
					this.contextSupplier, this.observationRegistrySupplier.get());
			if (this.shouldBlock) {
				CountDownLatch latch = new CountDownLatch(1);
				TrackingSubscriber subscriber = new TrackingSubscriber(this.subscriptionTrackerRegistry, observation, latch);
				subscribe(subscriber, observation);
				try {
					latch.await();
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
			else {
				TrackingSubscriber subscriber = new TrackingSubscriber(this.subscriptionTrackerRegistry, observation);
				subscribe(subscriber, observation);
			}
		}

		private void subscribe(TrackingSubscriber subscriber, Observation observation) {
			this.subscriptionTrackerRegistry.add(subscriber);
			if (REACTOR_PRESENT) {
				observation.start();
				Flux.from(this.publisher)
						.contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, observation))
						.subscribe(subscriber);
			}
			else {
				this.publisher.subscribe(subscriber);
			}
		}

		@Override
		public String toString() {
			return this.displayName;
		}
	}


	/**
	 * 跟踪其 {@code Subscription} 并将取消订阅能力暴露为 {@code Runnable} 的
	 * {@code Subscriber}。构造时提供 {@code CountDownLatch} 时可选择支持阻塞。
	 */
	private static final class TrackingSubscriber implements Subscriber<Object>, Runnable {

		private final List<Runnable> subscriptionTrackerRegistry;

		private final Observation observation;

		private final @Nullable CountDownLatch blockingLatch;

		// Implementation note: since this is created last-minute when subscribing,
		// there shouldn't be a way to cancel the tracker externally from the
		// ScheduledAnnotationBeanProcessor before the #setSubscription(Subscription)
		// method is called.
		private @Nullable Subscription subscription;

		TrackingSubscriber(List<Runnable> subscriptionTrackerRegistry, Observation observation) {
			this(subscriptionTrackerRegistry, observation, null);
		}

		TrackingSubscriber(List<Runnable> subscriptionTrackerRegistry, Observation observation, @Nullable CountDownLatch latch) {
			this.subscriptionTrackerRegistry = subscriptionTrackerRegistry;
			this.observation = observation;
			this.blockingLatch = latch;
		}

		@Override
		public void run() {
			if (this.subscription != null) {
				this.subscription.cancel();
				this.observation.stop();
			}
			if (this.blockingLatch != null) {
				this.blockingLatch.countDown();
			}
		}

		@Override
		public void onSubscribe(Subscription subscription) {
			this.subscription = subscription;
			subscription.request(Integer.MAX_VALUE);
		}

		@Override
		public void onNext(Object obj) {
			// no-op
		}

		@Override
		public void onError(Throwable ex) {
			this.subscriptionTrackerRegistry.remove(this);
			logger.warn("Unexpected error occurred in scheduled reactive task", ex);
			this.observation.error(ex);
			this.observation.stop();
			if (this.blockingLatch != null) {
				this.blockingLatch.countDown();
			}
		}

		@Override
		public void onComplete() {
			this.subscriptionTrackerRegistry.remove(this);
			if (this.observation.getContext() instanceof ScheduledTaskObservationContext context) {
				context.setComplete(true);
			}
			this.observation.stop();
			if (this.blockingLatch != null) {
				this.blockingLatch.countDown();
			}
		}
	}

}
