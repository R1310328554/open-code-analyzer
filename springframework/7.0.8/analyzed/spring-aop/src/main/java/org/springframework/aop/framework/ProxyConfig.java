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
 * 创建代理时使用的配置的便利超类，以确保所有代理创建者具有一致的属性。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AdvisedSupport
 */
public class ProxyConfig implements Serializable {

	/**
	 */
	private static final long serialVersionUID = -8409359707199703185L;


	/** 类相关状态（`proxyTargetClass`）。 */
	private @Nullable Boolean proxyTargetClass;

	/** `optimize`：该类的成员状态。 */
	private @Nullable Boolean optimize;

	/** `opaque`：该类的成员状态。 */
	private @Nullable Boolean opaque;

	/** 代理相关状态（`exposeProxy`）。 */
	private @Nullable Boolean exposeProxy;

	/** `frozen`：该类的成员状态。 */
	private @Nullable Boolean frozen;


	/**
	 * 设置是否直接代理目标类，而不是仅仅代理特定的接口。默认为“假”。 <p>将此设置为“true”以强制代理 TargetSource 的公开目标类。如果该目标类是一个接口，将为给
	 * 定接口创建一个 JDK 代理。如果该目标类是任何其他类，则将为给定类创建 CGLIB 代理。 <p>注意：根据具体代理工厂的配置，如果未指定接口（并且未激活接口自动检测），也将
	 * 应用代理目标类行为。
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	/**
	 * 返回是否直接代理目标类以及任何接口。
	 */
	public boolean isProxyTargetClass() {
		return (this.proxyTargetClass != null && this.proxyTargetClass);
	}

	/**
	 * 设置代理是否应执行积极优化。 “积极优化”的确切含义因代理而异，但通常存在一些权衡。默认为“假”。 <p>使用 Spring 当前的代理选项，此标志有效地强制执行 CGLIB 
	 * 代理（类似于 {@link #setProxyTargetClass}），但没有任何类验证检查（对于最终方法等）。
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	/**
	 * 返回代理是否应该执行积极的优化。
	 */
	public boolean isOptimize() {
		return (this.optimize != null && this.optimize);
	}

	/**
	 * 设置是否应阻止将此配置创建的代理强制转换为 {@link Advised} 以查询代理状态。 <p>Default 为“false”，这意味着任何 AOP 代理都可以转换为 {
	 * @link Advised}。
	 */
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	/**
	 * 返回是否应阻止由此配置创建的代理被转换为 {@link Advised}。
	 */
	public boolean isOpaque() {
		return (this.opaque != null && this.opaque);
	}

	/**
	 * 设置 AOP 框架是否应将代理公开为 ThreadLocal，以便通过 AopContext 类进行检索。如果建议的对象需要调用另一个建议的方法，这非常有用。 （如果使用{@c
	 * ode this}，则不建议调用）。 <p>D默认为“false”，以避免不必要的额外拦截。这意味着不保证 AopContext 访问将在建议对象的任何方法中一致地工作。
	 */
	public void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}

	/**
	 * 返回 AOP 代理是否将为每次调用公开 AOP 代理。
	 */
	public boolean isExposeProxy() {
		return (this.exposeProxy != null && this.exposeProxy);
	}

	/**
	 * 设置是否应冻结此配置。 <p>当配置被冻结时，无法进行任何建议更改。这对于优化很有用，当我们不希望调用者在转换为 Advised 后能够操作配置时也很有用。
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	/**
	 * 返回配置是否被冻结，并且不能进行任何建议更改。
	 */
	public boolean isFrozen() {
		return (this.frozen != null && this.frozen);
	}


	/**
	 * 从其他配置对象复制配置。
	 * @param other 从中复制配置的对象
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
	 * 对于尚未在本地设置的设置，从其他配置对象复制默认设置。
	 * @param other 从中复制配置的对象
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

	/**
	 * 返回字符串表示。
	 */
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
