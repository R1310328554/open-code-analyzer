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

package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.Assert;

/**
 * 希望感知应用上下文的应用对象的便捷超类，例如用于自定义查找协作 Bean
 * 或进行上下文特定的资源访问。它保存应用上下文引用并提供初始化回调方法。
 * 此外，还提供众多用于消息查找的便捷方法。
 *
 * <p>并非必须继承本类：若需要访问上下文（例如访问文件资源或消息源），
 * 继承本类会使事情更简单。注意，许多应用对象根本不需要感知应用上下文，
 * 因为它们可以通过 Bean 引用接收协作 Bean。
 *
 * <p>许多框架类派生自本类，尤其是在 Web 支持模块中。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.context.support.WebApplicationObjectSupport
 */
public abstract class ApplicationObjectSupport implements ApplicationContextAware {

	/** 子类可用的日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 本对象运行的 ApplicationContext。 */
	private @Nullable ApplicationContext applicationContext;

	/** 用于便捷访问消息的 MessageSourceAccessor。 */
	private @Nullable MessageSourceAccessor messageSourceAccessor;


	@Override
	public final void setApplicationContext(@Nullable ApplicationContext context) throws BeansException {
		if (context == null && !isContextRequired()) {
			// Reset internal context state.
			this.applicationContext = null;
			this.messageSourceAccessor = null;
		}
		else if (this.applicationContext == null) {
			// Initialize with passed-in context.
			if (!requiredContextClass().isInstance(context)) {
				throw new ApplicationContextException(
						"Invalid application context: needs to be of type [" + requiredContextClass().getName() + "]");
			}
			this.applicationContext = context;
			this.messageSourceAccessor = new MessageSourceAccessor(context);
			initApplicationContext(context);
		}
		else {
			// Ignore reinitialization if same context passed in.
			if (this.applicationContext != context) {
				throw new ApplicationContextException(
						"Cannot reinitialize with different application context: current one is [" +
						this.applicationContext + "], passed-in one is [" + context + "]");
			}
		}
	}

	/**
	 * 判断本应用对象是否必须在 ApplicationContext 中运行。
	 * <p>默认为 {@code false}。可覆盖以强制要求在上下文中运行
	 * （即在上下文外访问时抛出 IllegalStateException）。
	 * @see #getApplicationContext
	 * @see #getMessageSourceAccessor
	 */
	protected boolean isContextRequired() {
		return false;
	}

	/**
	 * 确定传入 {@code setApplicationContext} 的上下文必须属于的上下文类型。
	 * 子类可覆盖。
	 * @see #setApplicationContext
	 */
	protected Class<?> requiredContextClass() {
		return ApplicationContext.class;
	}

	/**
	 * 子类可覆盖此方法以实现自定义初始化行为。
	 * 在设置上下文实例后由 {@code setApplicationContext} 调用。
	 * <p>注意：在上下文重新初始化时<i>不会</i>调用，仅在首次初始化本对象的上下文引用时调用。
	 * <p>默认实现调用不带 ApplicationContext 参数的 {@link #initApplicationContext()} 重载方法。
	 * @param context 包含本对象的 ApplicationContext
	 * @throws ApplicationContextException 若初始化出错
	 * @throws BeansException 若 ApplicationContext 方法抛出异常
	 * @see #setApplicationContext
	 */
	protected void initApplicationContext(ApplicationContext context) throws BeansException {
		initApplicationContext();
	}

	/**
	 * 子类可覆盖此方法以实现自定义初始化行为。
	 * <p>默认实现为空。由 {@link #initApplicationContext(ApplicationContext)} 调用。
	 * @throws ApplicationContextException 若初始化出错
	 * @throws BeansException 若 ApplicationContext 方法抛出异常
	 * @see #setApplicationContext
	 */
	protected void initApplicationContext() throws BeansException {
	}


	/**
	 * 返回本对象关联的 ApplicationContext。
	 * @throws IllegalStateException 若未在 ApplicationContext 中运行
	 */
	public final @Nullable ApplicationContext getApplicationContext() throws IllegalStateException {
		if (this.applicationContext == null && isContextRequired()) {
			throw new IllegalStateException(
					"ApplicationObjectSupport instance [" + this + "] does not run in an ApplicationContext");
		}
		return this.applicationContext;
	}

	/**
	 * 获取实际使用的 ApplicationContext。
	 * @return ApplicationContext（永不为 {@code null}）
	 * @throws IllegalStateException 若未设置 ApplicationContext
	 * @since 5.0
	 */
	protected final ApplicationContext obtainApplicationContext() {
		ApplicationContext applicationContext = getApplicationContext();
		Assert.state(applicationContext != null, "No ApplicationContext");
		return applicationContext;
	}

	/**
	 * 返回本对象所用应用上下文的 MessageSourceAccessor，便于消息访问。
	 * @throws IllegalStateException 若未在 ApplicationContext 中运行
	 */
	protected final @Nullable MessageSourceAccessor getMessageSourceAccessor() throws IllegalStateException {
		if (this.messageSourceAccessor == null && isContextRequired()) {
			throw new IllegalStateException(
					"ApplicationObjectSupport instance [" + this + "] does not run in an ApplicationContext");
		}
		return this.messageSourceAccessor;
	}

}
