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

package org.springframework.beans.support;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@link SortDefinition} 接口的可变实现。
 * 在重复设置同一属性时支持切换升序标志。
 *
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 26.05.2003
 * @see #setToggleAscendingOnProperty
 * @deprecated as severely outdated and superseded by more modern solutions,
 * for example in Spring Data Commons
 */
@Deprecated(since = "7.0.3", forRemoval = true)
@SuppressWarnings({"removal", "serial"})
public class MutableSortDefinition implements SortDefinition, Serializable {

	private String property = "";

	private boolean ignoreCase = true;

	private boolean ascending = true;

	private boolean toggleAscendingOnProperty = false;


	/**
	 * 创建空的 MutableSortDefinition，通过 Bean 属性进行填充。
	 * @see #setProperty
	 * @see #setIgnoreCase
	 * @see #setAscending
	 */
	public MutableSortDefinition() {
	}

	/**
	 * 拷贝构造器：创建镜像给定排序定义的新 MutableSortDefinition。
	 * @param source 原始排序定义
	 */
	public MutableSortDefinition(SortDefinition source) {
		this.property = source.getProperty();
		this.ignoreCase = source.isIgnoreCase();
		this.ascending = source.isAscending();
	}

	/**
	 * 根据给定设置创建 MutableSortDefinition。
	 * @param property 用于比较的属性
	 * @param ignoreCase 比较 String 值时是否忽略大小写
	 * @param ascending 是否升序（{@code true}）或降序（{@code false}）
	 */
	public MutableSortDefinition(String property, boolean ignoreCase, boolean ascending) {
		this.property = property;
		this.ignoreCase = ignoreCase;
		this.ascending = ascending;
	}

	/**
	 * 创建新的 MutableSortDefinition。
	 * @param toggleAscendingOnSameProperty 若再次设置同一属性（即对已设置的属性名再次调用
	 * {@code setProperty}），是否切换升序标志
	 */
	public MutableSortDefinition(boolean toggleAscendingOnSameProperty) {
		this.toggleAscendingOnProperty = toggleAscendingOnSameProperty;
	}


	/**
	 * 设置用于比较的属性。
	 * <p>若属性与当前属性相同，且已启用 "toggleAscendingOnProperty"，
	 * 则反转排序方向；否则忽略此次设置。
	 * @see #setToggleAscendingOnProperty
	 */
	public void setProperty(String property) {
		if (!StringUtils.hasLength(property)) {
			this.property = "";
		}
		else {
			// 隐式切换升序？
			if (isToggleAscendingOnProperty()) {
				this.ascending = (!property.equals(this.property) || !this.ascending);
			}
			this.property = property;
		}
	}

	@Override
	public String getProperty() {
		return this.property;
	}

	/**
	 * 设置比较 String 值时是否忽略大小写。
	 */
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	@Override
	public boolean isIgnoreCase() {
		return this.ignoreCase;
	}

	/**
	 * 设置升序（{@code true}）或降序（{@code false}）。
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	@Override
	public boolean isAscending() {
		return this.ascending;
	}

	/**
	 * 设置若再次设置同一属性（即对已设置的属性名再次调用 {@link #setProperty}），
	 * 是否切换升序标志。
	 * <p>这在通过 Web 请求进行参数绑定时特别有用：再次点击同一列表头字段时，
	 * 可能期望对同一字段按相反顺序重新排序。
	 */
	public void setToggleAscendingOnProperty(boolean toggleAscendingOnProperty) {
		this.toggleAscendingOnProperty = toggleAscendingOnProperty;
	}

	/**
	 * 返回若再次设置同一属性（即对已设置的属性名再次调用 {@code setProperty}），
	 * 是否切换升序标志。
	 */
	public boolean isToggleAscendingOnProperty() {
		return this.toggleAscendingOnProperty;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof SortDefinition that &&
				getProperty().equals(that.getProperty()) &&
				isAscending() == that.isAscending() &&
				isIgnoreCase() == that.isIgnoreCase()));
	}

	@Override
	public int hashCode() {
		int hashCode = getProperty().hashCode();
		hashCode = 29 * hashCode + (isIgnoreCase() ? 1 : 0);
		hashCode = 29 * hashCode + (isAscending() ? 1 : 0);
		return hashCode;
	}

}
