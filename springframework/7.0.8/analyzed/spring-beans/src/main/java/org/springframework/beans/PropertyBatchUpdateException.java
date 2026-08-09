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

package org.springframework.beans;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.StringJoiner;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 组合异常，由若干独立的 {@link PropertyAccessException} 组成。
 * 绑定过程开始时会创建本类实例，并在需要时向其中追加错误。
 *
 * <p>遇到应用层 {@code PropertyAccessException} 时绑定仍会继续：
 * 能成功应用的变更照常生效，被拒绝的变更则记录在本类对象中。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 18 April 2001
 */
@SuppressWarnings("serial")
public class PropertyBatchUpdateException extends BeansException {

	/** 所包含的 PropertyAccessException 对象列表。 */
	private final PropertyAccessException[] propertyAccessExceptions;


	/**
	 * 创建一个新的 {@code PropertyBatchUpdateException}。
	 * @param propertyAccessExceptions PropertyAccessException 列表
	 */
	public PropertyBatchUpdateException(PropertyAccessException[] propertyAccessExceptions) {
		super(null, null);
		Assert.notEmpty(propertyAccessExceptions, "At least 1 PropertyAccessException required");
		this.propertyAccessExceptions = propertyAccessExceptions;
	}


	/**
	 * 返回异常数量。若返回 0，表示绑定过程中未遇到错误。
	 */
	public final int getExceptionCount() {
		return this.propertyAccessExceptions.length;
	}

	/**
	 * 返回本对象中保存的 {@code propertyAccessExceptions} 数组。
	 * <p>若没有错误，返回空数组（而非 {@code null}）。
	 */
	public final PropertyAccessException[] getPropertyAccessExceptions() {
		return this.propertyAccessExceptions;
	}

	/**
	 * 返回指定字段对应的异常；若不存在则返回 {@code null}。
	 * @param propertyName 属性名
	 * @return 该属性对应的 PropertyAccessException；没有则返回 {@code null}
	 */
	public @Nullable PropertyAccessException getPropertyAccessException(String propertyName) {
		for (PropertyAccessException pae : this.propertyAccessExceptions) {
			if (ObjectUtils.nullSafeEquals(propertyName, pae.getPropertyName())) {
				return pae;
			}
		}
		return null;
	}


	/**
	 * 返回汇总后的详细消息，包含所有嵌套的属性访问异常信息。
	 */
	@Override
	public String getMessage() {
		StringJoiner stringJoiner = new StringJoiner("; ", "Failed properties: ", "");
		for (PropertyAccessException exception : this.propertyAccessExceptions) {
			stringJoiner.add(exception.getMessage());
		}
		return stringJoiner.toString();
	}

	/**
	 * 返回包含嵌套异常概要的字符串表示。
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName()).append("; nested PropertyAccessExceptions (");
		sb.append(getExceptionCount()).append(") are:");
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			sb.append('\n').append("PropertyAccessException ").append(i + 1).append(": ");
			sb.append(this.propertyAccessExceptions[i]);
		}
		return sb.toString();
	}

	/**
	 * 将本异常及全部嵌套 {@code PropertyAccessException} 的堆栈打印到指定流。
	 */
	@Override
	public void printStackTrace(PrintStream ps) {
		synchronized (ps) {
			ps.println(getClass().getName() + "; nested PropertyAccessException details (" +
					getExceptionCount() + ") are:");
			for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
				ps.println("PropertyAccessException " + (i + 1) + ":");
				this.propertyAccessExceptions[i].printStackTrace(ps);
			}
		}
	}

	/**
	 * 将本异常及全部嵌套 {@code PropertyAccessException} 的堆栈打印到指定 Writer。
	 */
	@Override
	public void printStackTrace(PrintWriter pw) {
		synchronized (pw) {
			pw.println(getClass().getName() + "; nested PropertyAccessException details (" +
					getExceptionCount() + ") are:");
			for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
				pw.println("PropertyAccessException " + (i + 1) + ":");
				this.propertyAccessExceptions[i].printStackTrace(pw);
			}
		}
	}

	/**
	 * 判断本异常或其嵌套的 {@code PropertyAccessException} 是否包含指定类型。
	 */
	@Override
	public boolean contains(@Nullable Class<?> exType) {
		if (exType == null) {
			return false;
		}
		if (exType.isInstance(this)) {
			return true;
		}
		for (PropertyAccessException pae : this.propertyAccessExceptions) {
			if (pae.contains(exType)) {
				return true;
			}
		}
		return false;
	}

}
