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

package org.springframework.beans.factory;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.FatalBeanException;
import org.springframework.core.NestedRuntimeException;

/**
 * BeanFactory 根据 bean 定义创建 bean 时遇到错误而抛出的异常。
 *
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class BeanCreationException extends FatalBeanException {

	/** 所请求的 bean 名称（若可知）。 */
	private final @Nullable String beanName;

	/** bean 定义所来自资源的描述（若可知）。 */
	private final @Nullable String resourceDescription;

	/** 同一 bean 创建过程中更早出现的相关原因。 */
	private @Nullable List<Throwable> relatedCauses;


	/**
	 * 创建新的 BeanCreationException。
	 * @param msg 详细消息
	 */
	public BeanCreationException(String msg) {
		super(msg);
		this.beanName = null;
		this.resourceDescription = null;
	}

	/**
	 * 创建新的 BeanCreationException。
	 * @param msg 详细消息
	 * @param cause 根原因
	 */
	public BeanCreationException(String msg, Throwable cause) {
		super(msg, cause);
		this.beanName = null;
		this.resourceDescription = null;
	}

	/**
	 * 创建新的 BeanCreationException。
	 * @param beanName 所请求的 bean 名称
	 * @param msg 详细消息
	 */
	public BeanCreationException(String beanName, String msg) {
		super("Error creating bean with name '" + beanName + "': " + msg);
		this.beanName = beanName;
		this.resourceDescription = null;
	}

	/**
	 * 创建新的 BeanCreationException。
	 * @param beanName 所请求的 bean 名称
	 * @param msg 详细消息
	 * @param cause 根原因
	 */
	public BeanCreationException(String beanName, String msg, Throwable cause) {
		this(beanName, msg);
		initCause(cause);
	}

	/**
	 * 创建新的 BeanCreationException。
	 * @param resourceDescription bean 定义所来自资源的描述
	 * @param beanName 所请求的 bean 名称
	 * @param msg 详细消息
	 */
	public BeanCreationException(@Nullable String resourceDescription, @Nullable String beanName, @Nullable String msg) {
		super("Error creating bean with name '" + beanName + "'" +
				(resourceDescription != null ? " defined in " + resourceDescription : "") + ": " + msg);
		this.resourceDescription = resourceDescription;
		this.beanName = beanName;
		this.relatedCauses = null;
	}

	/**
	 * 创建新的 BeanCreationException。
	 * @param resourceDescription bean 定义所来自资源的描述
	 * @param beanName 所请求的 bean 名称
	 * @param msg 详细消息
	 * @param cause 根原因
	 */
	public BeanCreationException(@Nullable String resourceDescription, String beanName, @Nullable String msg, Throwable cause) {
		this(resourceDescription, beanName, msg);
		initCause(cause);
	}


	/**
	 * 返回 bean 定义所来自资源的描述（若有）。
	 */
	public @Nullable String getResourceDescription() {
		return this.resourceDescription;
	}

	/**
	 * 返回所请求的 bean 名称（若有）。
	 */
	public @Nullable String getBeanName() {
		return this.beanName;
	}

	/**
	 * 向本 bean 创建异常添加相关原因：
	 * 并非失败的直接原因，而是同一 bean 实例创建过程中更早出现的异常。
	 * @param ex 要添加的相关原因
	 */
	public void addRelatedCause(Throwable ex) {
		if (this.relatedCauses == null) {
			this.relatedCauses = new ArrayList<>();
		}
		this.relatedCauses.add(ex);
	}

	/**
	 * 返回相关原因（若有）。
	 * @return 相关原因数组；若无则为 {@code null}
	 */
	public Throwable @Nullable [] getRelatedCauses() {
		if (this.relatedCauses == null) {
			return null;
		}
		return this.relatedCauses.toArray(new Throwable[0]);
	}


	/**
	 * 在 toString 中追加相关原因信息。
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		if (this.relatedCauses != null) {
			for (Throwable relatedCause : this.relatedCauses) {
				sb.append("\nRelated cause: ");
				sb.append(relatedCause);
			}
		}
		return sb.toString();
	}

	/**
	 * 打印堆栈跟踪时一并输出相关原因。
	 */
	@Override
	public void printStackTrace(PrintStream ps) {
		synchronized (ps) {
			super.printStackTrace(ps);
			if (this.relatedCauses != null) {
				for (Throwable relatedCause : this.relatedCauses) {
					ps.println("Related cause:");
					relatedCause.printStackTrace(ps);
				}
			}
		}
	}

	/**
	 * 打印堆栈跟踪时一并输出相关原因。
	 */
	@Override
	public void printStackTrace(PrintWriter pw) {
		synchronized (pw) {
			super.printStackTrace(pw);
			if (this.relatedCauses != null) {
				for (Throwable relatedCause : this.relatedCauses) {
					pw.println("Related cause:");
					relatedCause.printStackTrace(pw);
				}
			}
		}
	}

	/**
	 * 除自身原因链外，还检查相关原因中是否包含指定异常类型。
	 */
	@Override
	public boolean contains(@Nullable Class<?> exClass) {
		if (super.contains(exClass)) {
			return true;
		}
		if (this.relatedCauses != null) {
			for (Throwable relatedCause : this.relatedCauses) {
				if (relatedCause instanceof NestedRuntimeException nested && nested.contains(exClass)) {
					return true;
				}
			}
		}
		return false;
	}

}
