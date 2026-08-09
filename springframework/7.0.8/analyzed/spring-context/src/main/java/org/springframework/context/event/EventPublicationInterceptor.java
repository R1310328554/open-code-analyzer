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

package org.springframework.context.event;

import java.lang.reflect.Constructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;

/**
 * 在每次方法调用<i>成功</i>后，向 {@code ApplicationEventPublisher} 注册的所有
 * {@code ApplicationListener} 发布 {@code ApplicationEvent} 的
 * {@link MethodInterceptor Interceptor}。
 *
 * <p>本拦截器可在每次<i>成功</i>的方法调用后发布自定义事件，通过
 * {@link #setApplicationEventClass "applicationEventClass"} 属性配置。
 * 自 7.0.3 起，也可配置 {@link #setApplicationEventFactory 工厂函数}，
 * 在其中实现主要的 {@link ApplicationEventFactory#onSuccess} 方法。
 *
 * <p>默认情况下（自 7.0.3 起），本拦截器会为方法调用抛出的每个异常发布
 * {@link MethodFailureEvent}。可通过 {@code ApplicationListener<MethodFailureEvent>}
 * 类或 {@code @EventListener(MethodFailureEvent.class)} 方法便捷地监听。
 * 失败事件可通过重写 {@link ApplicationEventFactory#onFailure} 方法自定义。
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @author Rick Evans
 * @see #setApplicationEventClass
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.ApplicationEventPublisher
 * @see org.springframework.context.ApplicationContext
 */
public class EventPublicationInterceptor
		implements MethodInterceptor, ApplicationEventPublisherAware, InitializingBean {

	/** 根据方法调用结果构建应用事件的工厂。 */
	private ApplicationEventFactory applicationEventFactory = (invocation, returnValue) -> null;

	/** 用于发布事件的事件发布器。 */
	private @Nullable ApplicationEventPublisher applicationEventPublisher;


	/**
	 * 设置在每次成功调用后要发布的应用事件类。
	 * <p>事件类<b>必须</b>具有接受单个 {@code Object} 参数（事件源）的构造函数；
	 * 拦截器将传入被调用的对象。
	 * @throws IllegalArgumentException 若提供的 {@code Class} 为 {@code null}、
	 * 不是 {@code ApplicationEvent} 子类，或未暴露接受单个 {@code Object} 参数的构造函数
	 * @see #setApplicationEventFactory
	 */
	public void setApplicationEventClass(Class<? extends ApplicationEvent> applicationEventClass) {
		if (ApplicationEvent.class == applicationEventClass ||
				!ApplicationEvent.class.isAssignableFrom(applicationEventClass)) {
			throw new IllegalArgumentException("'applicationEventClass' needs to extend ApplicationEvent");
		}
		try {
			Constructor<? extends ApplicationEvent> ctor = applicationEventClass.getConstructor(Object.class);
			this.applicationEventFactory = ((invocation, returnValue) ->
					BeanUtils.instantiateClass(ctor, invocation.getThis()));
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException("ApplicationEvent class [" +
					applicationEventClass.getName() + "] does not have the required Object constructor: " + ex);
		}
	}

	/**
	 * 指定根据 {@link MethodInvocation} 构建 {@link ApplicationEvent} 的工厂函数，
	 * 表示每次<i>成功</i>的方法调用。
	 * @since 7.0.3
	 * @see #setApplicationEventClass
	 */
	public void setApplicationEventFactory(ApplicationEventFactory applicationEventFactory) {
		this.applicationEventFactory = applicationEventFactory;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.applicationEventPublisher == null) {
			throw new IllegalArgumentException("Property 'applicationEventPublisher' is required");
		}
	}


	@Override
	public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {
		Assert.state(this.applicationEventPublisher != null, "No ApplicationEventPublisher available");

		Object retVal;
		try {
			retVal = invocation.proceed();
		}
		catch (Throwable ex) {
			// 调用失败后发布事件。
			ApplicationEvent event = this.applicationEventFactory.onFailure(invocation, ex);
			if (event != null) {
				this.applicationEventPublisher.publishEvent(event);
			}
			throw ex;
		}

		// 调用成功后发布事件。
		ApplicationEvent event = this.applicationEventFactory.onSuccess(invocation, retVal);
		if (event != null) {
			this.applicationEventPublisher.publishEvent(event);
		}
		return retVal;
	}


	/**
	 * 在方法调用后构建 {@link ApplicationEvent} 的回调接口。
	 * @since 7.0.3
	 */
	@FunctionalInterface
	public interface ApplicationEventFactory {

		/**
		 * 为给定的成功方法调用构建 {@link ApplicationEvent}。
		 * <p>这是需要实现的主要方法，因为成功时默认没有固定事件类型。
		 * 也可返回 {@code null} 表示成功时不发布任何事件。
		 * @param invocation 成功的方法调用
		 * @param returnValue 方法返回值（若有）
		 * @return 要发布的事件，或 {@code null} 表示不发布
		 */
		@Nullable ApplicationEvent onSuccess(MethodInvocation invocation, @Nullable Object returnValue);

		/**
		 * 为给定的失败方法调用构建 {@link ApplicationEvent}。
		 * <p>默认实现构建通用的 {@link MethodFailureEvent}。
		 * 可重写以构建自定义事件，或返回 {@code null} 表示失败时不发布任何事件。
		 * @param invocation 失败的方法调用
		 * @param failure 方法抛出的异常
		 * @return 要发布的事件，或 {@code null} 表示不发布
		 */
		default @Nullable ApplicationEvent onFailure(MethodInvocation invocation, Throwable failure) {
			return new MethodFailureEvent(invocation, failure);
		}
	}

}
