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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * PagedListHolder 是用于处理对象列表并将其分页的简单状态持有者。页码从 0 开始。
 *
 * <p>主要面向 Web UI 场景。典型用法是：用 Bean 列表实例化后放入 Session，并导出为模型。
 * 所有属性均可通过编程方式设置/获取，但最常见的是数据绑定，即从请求参数填充 Bean。
 * Getter 主要由视图使用。
 *
 * <p>支持通过 {@link SortDefinition} 实现对底层列表排序，对应属性名为 "sort"。
 * 默认使用 {@link MutableSortDefinition}，在重复设置同一属性时会切换升序标志。
 *
 * <p>数据绑定名称须为 "pageSize" 和 "sort.ascending"，与 BeanWrapper 的约定一致。
 * 注意名称与嵌套语法与相应 JSTL EL 表达式一致，例如 "myModelAttr.pageSize" 和
 * "myModelAttr.sort.ascending"。
 *
 * @author Juergen Hoeller
 * @since 19.05.2003
 * @param <E> 元素类型
 * @see #getPageList()
 * @deprecated as severely outdated and superseded by more modern solutions,
 * for example in Spring Data Commons
 */
@Deprecated(since = "7.0.3", forRemoval = true)
@SuppressWarnings({"removal", "serial"})
public class PagedListHolder<E> implements Serializable {

	/** 默认每页大小。 */
	public static final int DEFAULT_PAGE_SIZE = 10;

	/** 默认最大页码链接数。 */
	public static final int DEFAULT_MAX_LINKED_PAGES = 10;


	private List<E> source = Collections.emptyList();

	private @Nullable Date refreshDate;

	private @Nullable SortDefinition sort;

	private @Nullable SortDefinition sortUsed;

	private int pageSize = DEFAULT_PAGE_SIZE;

	private int page = 0;

	private boolean newPageSet;

	private int maxLinkedPages = DEFAULT_MAX_LINKED_PAGES;


	/**
	 * 创建新的持有者实例。
	 * 使用前须设置源列表。
	 * @see #setSource
	 */
	public PagedListHolder() {
		this(new ArrayList<>(0));
	}

	/**
	 * 使用给定源列表创建新持有者，并以默认排序定义启动（启用 "toggleAscendingOnProperty"）。
	 * @param source 源 List
	 * @see MutableSortDefinition#setToggleAscendingOnProperty
	 */
	public PagedListHolder(List<E> source) {
		this(source, new MutableSortDefinition(true));
	}

	/**
	 * 使用给定源列表和排序定义创建新持有者。
	 * @param source 源 List
	 * @param sort 初始 SortDefinition
	 */
	public PagedListHolder(List<E> source, SortDefinition sort) {
		setSource(source);
		setSort(sort);
	}


	/**
	 * 设置此持有者的源列表。
	 */
	public void setSource(List<E> source) {
		Assert.notNull(source, "Source List must not be null");
		this.source = source;
		this.refreshDate = new Date();
		this.sortUsed = null;
	}

	/**
	 * 返回此持有者的源列表。
	 */
	public List<E> getSource() {
		return this.source;
	}

	/**
	 * 返回上次从源提供者获取列表的时间。
	 */
	public @Nullable Date getRefreshDate() {
		return this.refreshDate;
	}

	/**
	 * 设置此持有者的排序定义。
	 * 通常为 MutableSortDefinition 实例。
	 * @see org.springframework.beans.support.MutableSortDefinition
	 */
	public void setSort(@Nullable SortDefinition sort) {
		this.sort = sort;
	}

	/**
	 * 返回此持有者的排序定义。
	 */
	public @Nullable SortDefinition getSort() {
		return this.sort;
	}

	/**
	 * 设置当前每页大小。
	 * 若大小改变，则重置当前页码。
	 * <p>默认值为 10。
	 */
	public void setPageSize(int pageSize) {
		if (pageSize != this.pageSize) {
			this.pageSize = pageSize;
			if (!this.newPageSet) {
				this.page = 0;
			}
		}
	}

	/**
	 * 返回当前每页大小。
	 */
	public int getPageSize() {
		return this.pageSize;
	}

	/**
	 * 设置当前页码。
	 * 页码从 0 开始。
	 */
	public void setPage(int page) {
		this.page = page;
		this.newPageSet = true;
	}

	/**
	 * 返回当前页码。
	 * 页码从 0 开始。
	 */
	public int getPage() {
		this.newPageSet = false;
		if (this.page >= getPageCount()) {
			this.page = getPageCount() - 1;
		}
		return this.page;
	}

	/**
	 * 设置当前页附近可链接的最大页数。
	 */
	public void setMaxLinkedPages(int maxLinkedPages) {
		this.maxLinkedPages = maxLinkedPages;
	}

	/**
	 * 返回当前页附近可链接的最大页数。
	 */
	public int getMaxLinkedPages() {
		return this.maxLinkedPages;
	}


	/**
	 * 返回当前源列表的总页数。
	 */
	public int getPageCount() {
		float nrOfPages = (float) getNrOfElements() / getPageSize();
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
	}

	/**
	 * 返回当前页是否为第一页。
	 */
	public boolean isFirstPage() {
		return getPage() == 0;
	}

	/**
	 * 返回当前页是否为最后一页。
	 */
	public boolean isLastPage() {
		return getPage() == getPageCount() -1;
	}

	/**
	 * 切换到上一页。
	 * 若已在第一页则保持不动。
	 */
	public void previousPage() {
		if (!isFirstPage()) {
			this.page--;
		}
	}

	/**
	 * 切换到下一页。
	 * 若已在最后一页则保持不动。
	 */
	public void nextPage() {
		if (!isLastPage()) {
			this.page++;
		}
	}

	/**
	 * 返回源列表中的元素总数。
	 */
	public int getNrOfElements() {
		return getSource().size();
	}

	/**
	 * 返回当前页第一个元素的索引。
	 * 元素索引从 0 开始。
	 */
	public int getFirstElementOnPage() {
		return (getPageSize() * getPage());
	}

	/**
	 * 返回当前页最后一个元素的索引。
	 * 元素索引从 0 开始。
	 */
	public int getLastElementOnPage() {
		int endIndex = getPageSize() * (getPage() + 1);
		int size = getNrOfElements();
		return (endIndex > size ? size : endIndex) - 1;
	}

	/**
	 * 返回表示当前页的子列表。
	 */
	public List<E> getPageList() {
		return getSource().subList(getFirstElementOnPage(), getLastElementOnPage() + 1);
	}

	/**
	 * 返回围绕当前页创建链接的起始页码。
	 */
	public int getFirstLinkedPage() {
		return Math.max(0, getPage() - (getMaxLinkedPages() / 2));
	}

	/**
	 * 返回围绕当前页创建链接的结束页码。
	 */
	public int getLastLinkedPage() {
		return Math.min(getFirstLinkedPage() + getMaxLinkedPages() - 1, getPageCount() - 1);
	}


	/**
	 * 必要时重新排序列表，即当前 {@code sort} 与备份的 {@code sortUsed} 不一致时。
	 * <p>调用 {@code doSort} 触发实际排序。
	 * @see #doSort
	 */
	public void resort() {
		SortDefinition sort = getSort();
		if (sort != null && !sort.equals(this.sortUsed)) {
			this.sortUsed = copySortDefinition(sort);
			doSort(getSource(), sort);
			setPage(0);
		}
	}

	/**
	 * 深拷贝给定排序定义，用作与修改后排序定义比较的状态持有者。
	 * <p>默认实现创建 MutableSortDefinition 实例。
	 * 子类可覆盖，尤其在对 SortDefinition 接口进行自定义扩展时。
	 * 允许返回 {@code null}，表示不保存排序状态，每次 {@code resort} 都会实际排序。
	 * @param sort 当前 SortDefinition 对象
	 * @return SortDefinition 的深拷贝
	 * @see MutableSortDefinition#MutableSortDefinition(SortDefinition)
	 */
	protected SortDefinition copySortDefinition(SortDefinition sort) {
		return new MutableSortDefinition(sort);
	}

	/**
	 * 根据给定排序定义对源列表执行实际排序。
	 * <p>默认实现使用 Spring 的 PropertyComparator。
	 * 子类可覆盖。
	 * @see PropertyComparator#sort(java.util.List, SortDefinition)
	 */
	protected void doSort(List<E> source, SortDefinition sort) {
		PropertyComparator.sort(source, sort);
	}

}
