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

package org.springframework.aop.framework;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 创建代理所用配置的便捷超类，
 * 确保所有代理创建器属性一致。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AdvisedSupport
 */
public class ProxyConfig implements Serializable {

	/** 使用 Spring 1.2 的 serialVersionUID 以保持互操作性。 */
	private static final long serialVersionUID = -8409359707199703185L;


	private @Nullable Boolean proxyTargetClass;

	private @Nullable Boolean optimize;

	private @Nullable Boolean opaque;

	private @Nullable Boolean exposeProxy;

	private @Nullable Boolean frozen;


	/**
	 * 设置是否直接代理目标类，而非仅代理特定接口。默认为 "false"。
	 * <p>设为 "true" 可强制代理 TargetSource 暴露的目标类。
	 * 若目标类为接口，则为该接口创建 JDK 代理；
	 * 若为其他类，则为该类创建 CGLIB 代理。
	 * <p>注意：取决于具体代理工厂配置，
	 * 若未指定接口（且未启用接口自动检测），
	 * 也会应用 proxy-target-class 行为。
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	/**
	 * 返回是否直接代理目标类（以及任意接口）。
	 */
	public boolean isProxyTargetClass() {
		return (this.proxyTargetClass != null && this.proxyTargetClass);
	}

	/**
	 * 设置代理是否执行激进优化。
	 * 「激进优化」的具体含义因代理类型而异，通常存在权衡。默认为 "false"。
	 * <p>在 Spring 当前代理选项下，此标志等效于强制 CGLIB 代理
	 *（类似 {@link #setProxyTargetClass}），
	 * 但不进行类校验（如 final 方法等）。
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	/**
	 * 返回代理是否执行激进优化。
	 */
	public boolean isOptimize() {
		return (this.optimize != null && this.optimize);
	}

	/**
	 * 设置本配置创建的代理是否禁止强制转换为 {@link Advised} 以查询代理状态。
	 * <p>默认为 "false"，表示任意 AOP 代理可转换为 {@link Advised}。
	 */
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	/**
	 * 返回本配置创建的代理是否禁止转换为 {@link Advised}。
	 */
	public boolean isOpaque() {
		return (this.opaque != null && this.opaque);
	}

	/**
	 * 设置 AOP 框架是否通过 ThreadLocal 暴露代理，
	 * 以便通过 AopContext 类获取。当被通知对象需调用自身另一被通知方法时有用
	 *（若使用 {@code this}，调用不会被通知）。
	 * <p>默认为 "false"，以避免不必要的额外拦截。
	 * 这意味着不保证在被通知对象的任意方法内
	 * AopContext 访问始终一致可用。
	 */
	public void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}

	/**
	 * 返回 AOP 代理是否在每次调用时暴露自身。
	 */
	public boolean isExposeProxy() {
		return (this.exposeProxy != null && this.exposeProxy);
	}

	/**
	 * 设置本配置是否应冻结。
	 * <p>配置冻结后不可更改 Advice。有利于优化，
	 * 也用于防止调用者在转换为 Advised 后修改配置。
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	/**
	 * 返回配置是否已冻结（不可更改 Advice）。
	 */
	public boolean isFrozen() {
		return (this.frozen != null && this.frozen);
	}


	/**
	 * 从其他配置对象复制配置。
	 * @param other 要复制配置的来源对象
	 */
	public void copyFrom(ProxyConfig other) {
		Assert.notNull(other, "Other ProxyConfig object must not be null");
		this.proxyTargetClass = other.proxyTargetClass;
		this.optimize = other.optimize;
		this.opaque = other.opaque;
		this.exposeProxy = other.exposeProxy;
		this.frozen = other.frozen;
	}

	/**
	 * 从其他配置对象复制默认设置，
	 * 仅针对本地未设置的项。
	 * @param other 要复制配置的来源对象
	 * @since 7.0
	 */
	public void copyDefault(ProxyConfig other) {
		Assert.notNull(other, "Other ProxyConfig object must not be null");
		if (this.proxyTargetClass == null) {
			this.proxyTargetClass = other.proxyTargetClass;
		}
		if (this.optimize == null) {
			this.optimize = other.optimize;
		}
		if (this.opaque == null) {
			this.opaque = other.opaque;
		}
		if (this.exposeProxy == null) {
			this.exposeProxy = other.exposeProxy;
		}
		if (this.frozen == null) {
			this.frozen = other.frozen;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("proxyTargetClass=").append(this.proxyTargetClass).append("; ");
		sb.append("optimize=").append(this.optimize).append("; ");
		sb.append("opaque=").append(this.opaque).append("; ");
		sb.append("exposeProxy=").append(this.exposeProxy).append("; ");
		sb.append("frozen=").append(this.frozen);
		return sb.toString();
	}

}
