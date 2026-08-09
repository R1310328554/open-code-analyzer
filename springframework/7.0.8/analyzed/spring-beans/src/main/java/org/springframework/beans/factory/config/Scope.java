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

package org.springframework.beans.factory.config;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ObjectFactory;

/**
 * 由 {@link ConfigurableBeanFactory} 使用的策略接口，表示存放 bean 实例的目标作用域。
 * 允许在 BeanFactory 标准作用域
 * {@link ConfigurableBeanFactory#SCOPE_SINGLETON "singleton"} 与
 * {@link ConfigurableBeanFactory#SCOPE_PROTOTYPE "prototype"} 之外，
 * 通过 {@link ConfigurableBeanFactory#registerScope(String, Scope) 特定键}
 * 注册自定义作用域。
 *
 * <p>{@link org.springframework.context.ApplicationContext} 实现（如
 * {@link org.springframework.web.context.WebApplicationContext}）可基于本 Scope SPI
 * 注册环境相关的额外标准作用域，例如
 * {@link org.springframework.web.context.WebApplicationContext#SCOPE_REQUEST "request"}
 * 与 {@link org.springframework.web.context.WebApplicationContext#SCOPE_SESSION "session"}。
 *
 * <p>尽管主要用于 Web 环境中的扩展作用域，本 SPI 完全通用：可从任意底层存储机制
 *（如 HTTP 会话或自定义会话机制）获取与存放对象。传入 {@code get} 与
 * {@code remove} 的名称标识当前作用域中的目标对象。
 *
 * <p>{@code Scope} 实现应为线程安全。一个 {@code Scope} 实例可同时用于多个 bean 工厂
 *（除非实现显式感知所属 BeanFactory），任意数量的线程可从任意数量的工厂并发访问
 * 该 {@code Scope}。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see ConfigurableBeanFactory#registerScope
 * @see CustomScopeConfigurer
 * @see org.springframework.aop.scope.ScopedProxyFactoryBean
 * @see org.springframework.web.context.request.RequestScope
 * @see org.springframework.web.context.request.SessionScope
 */
public interface Scope {

	/**
	 * 从底层作用域返回指定名称的对象；若底层存储中不存在则
	 * {@link org.springframework.beans.factory.ObjectFactory#getObject() 创建}。
	 * <p>这是 Scope 的核心操作，也是唯一绝对必需的操作。
	 * @param name 要获取的对象名称
	 * @param objectFactory 底层存储中不存在时用于创建作用域对象的 {@link ObjectFactory}
	 * @return 目标对象（永不为 {@code null}）
	 * @throws IllegalStateException 底层作用域当前未激活时
	 */
	Object get(String name, ObjectFactory<?> objectFactory);

	/**
	 * 从底层作用域移除指定 {@code name} 的对象。
	 * <p>未找到对象时返回 {@code null}；否则返回被移除的 {@code Object}。
	 * <p>实现还应移除该对象已注册的销毁回调（若有）。但<i>不必</i>在此执行销毁回调，
	 * 因为对象将由调用方销毁（如适用）。
	 * <p><b>注意：此为可选操作。</b>不支持显式移除的实现可抛出
	 * {@link UnsupportedOperationException}。
	 * @param name 要移除的对象名称
	 * @return 被移除的对象，不存在时返回 {@code null}
	 * @throws IllegalStateException 底层作用域当前未激活时
	 * @see #registerDestructionCallback
	 */
	@Nullable Object remove(String name);

	/**
	 * 注册在作用域中指定对象销毁时（或整个作用域终止时，若作用域不单独销毁对象
	 * 而仅整体终止）要执行的回调。
	 * <p><b>注意：此为可选操作。</b>仅对配置了实际销毁逻辑的 scoped bean
	 * （DisposableBean、destroy-method、DestructionAwareBeanPostProcessor）调用。
	 * 实现应尽力在适当时机执行回调。若底层运行环境完全不支持，回调<i>必须被忽略
	 * 并记录相应警告</i>。
	 * <p>“销毁”指作为作用域自身生命周期一部分的自动销毁，而非应用通过本门面
	 * {@link #remove(String)} 显式移除 scoped 对象。若 scoped 对象通过
	 * {@code remove} 移除，应同时移除已注册的销毁回调，假定移除的对象将被复用或手动销毁。
	 * @param name 要执行销毁回调的对象名称
	 * @param callback 要执行的销毁回调。传入的 Runnable 不会抛出异常，
	 * 可安全执行而无需 try-catch。且 Runnable 通常可序列化（前提是目标对象也可序列化）。
	 * @throws IllegalStateException 底层作用域当前未激活时
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getDestroyMethodName()
	 * @see DestructionAwareBeanPostProcessor
	 */
	void registerDestructionCallback(String name, Runnable callback);

	/**
	 * 解析给定键对应的上下文对象（若有）。例如键 "request" 对应 HttpServletRequest。
	 * <p>自 7.0 起，本接口方法默认返回 {@code null}。
	 * @param key 上下文键
	 * @return 对应对象，未找到时返回 {@code null}
	 * @throws IllegalStateException 底层作用域当前未激活时
	 */
	default @Nullable Object resolveContextualObject(String key) {
		return null;
	}

	/**
	 * 返回当前底层作用域的<em>会话 ID</em>（若有）。
	 * <p>会话 ID 的确切含义取决于底层存储机制。对 session 作用域对象，通常等于
	 * （或派生自）{@link jakarta.servlet.http.HttpSession#getId() 会话 ID}；
	 * 对整体会话内的自定义会话，则使用当前会话的特定 ID。
	 * <p><b>注意：此为可选操作。</b>若底层存储机制没有明显的 ID 候选，实现可返回
	 * {@code null}。
	 * <p>自 7.0 起，本接口方法默认返回 {@code null}。
	 * @return 会话 ID，当前作用域无会话 ID 时返回 {@code null}
	 * @throws IllegalStateException 底层作用域当前未激活时
	 */
	default @Nullable String getConversationId() {
		return null;
	}

}
