/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用分页结果容器。
 *
 * <p>封装总记录数、当前页码、总页数及当前页数据列表，供 Open API 分页查询返回。</p>
 *
 * @author boyan
 * @date 2010-5-6
 */
public class Page<E> implements Serializable {
    
    static final long serialVersionUID = 1234544030560484292L;
    
    /** 符合条件的总记录数。 */
    private int totalCount;
    
    /** 当前页码（从 1 开始）。 */
    private int pageNumber;
    
    /** 可用总页数。 */
    private int pagesAvailable;
    
    /** 当前页数据项列表。 */
    private List<E> pageItems = new ArrayList<>();
    
    /** 设置当前页码。 */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    /** 设置可用总页数。 */
    public void setPagesAvailable(int pagesAvailable) {
        this.pagesAvailable = pagesAvailable;
    }
    
    /** 设置当前页数据列表。 */
    public void setPageItems(List<E> pageItems) {
        this.pageItems = pageItems;
    }
    
    /** 获取总记录数。 */
    public int getTotalCount() {
        return totalCount;
    }
    
    /** 设置总记录数。 */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    /** 获取当前页码。 */
    public int getPageNumber() {
        return pageNumber;
    }
    
    /** 获取可用总页数。 */
    public int getPagesAvailable() {
        return pagesAvailable;
    }
    
    /** 获取当前页数据列表。 */
    public List<E> getPageItems() {
        return pageItems;
    }
}
