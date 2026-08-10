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

package com.alibaba.nacos.core.utils;

import com.alibaba.nacos.api.model.Page;

import java.util.Collections;
import java.util.List;

/**
 * 内存列表分页工具：基于 {@link Page} 模型计算页码、切片区间与总页数。
 * Page Utils.
 *
 * @author xiweng.yy
 */
public class PageUtil {
    
    /**
     * 对内存列表做分页切片，返回当前页元素子列表。
     * Do page operation for input list.
     *
     * @param source    need paged source list
     * @param page      page number
     * @param pageSize  size of each page
     * @param <T>       The Type of List element.
     * @return Empty list if input list is empty or expected page is larger than source list size, otherwise paged list.
     */
    public static <T> List<T> subPageList(List<T> source, int page, int pageSize) {
        if (source.isEmpty()) {
            return source;
        }
        // 计算当前页在源列表中的 [start, end) 区间
        PageMetadata metadata = calculatePageMetadata(page, pageSize, source.size());
        if (source.size() > metadata.start) {
            return source.subList(metadata.start, metadata.end);
        }
        return Collections.emptyList();
    }
    
    /**
     * 封装 {@link Page} 对象：填充页码、总数、总页数与当前页数据。
     * Do page operation for input list.
     *
     * @param source    need paged source list
     * @param page      page number
     * @param pageSize  size of each page
     * @param <T>       The Type of List element.
     * @return Empty Page if input list is empty or expected page is larger than source list size, otherwise page obj.
     */
    public static <T> Page<T> subPage(List<T> source, int page, int pageSize) {
        Page<T> result = new Page<>();
        result.setPageNumber(page);
        if (source.isEmpty()) {
            result.setPageItems(Collections.emptyList());
            return result;
        }
        int totalCount = source.size();
        result.setTotalCount(totalCount);
        PageMetadata metadata = calculatePageMetadata(page, pageSize, totalCount);
        // 总页数 = 整除页数 + 1（含不足一页的尾页）
        int pagesAvailable = (totalCount / pageSize) + 1;
        result.setPagesAvailable(pagesAvailable);
        if (totalCount > metadata.start) {
            result.setPageItems(source.subList(metadata.start, metadata.end));
        }
        return result;
    }
    
    /** 计算分页元数据：校正负页码并裁剪 end 不超过 totalCount。 */
    private static PageMetadata calculatePageMetadata(int page, int pageSize, int totalCount) {
        int start = (page - 1) * pageSize;
        if (start < 0) {
            start = 0;
        }
        int end = start + pageSize;
        if (start > totalCount) {
            start = totalCount;
        }
        if (end > totalCount) {
            end = totalCount;
        }
        PageMetadata result = new PageMetadata();
        result.start = start;
        result.end = end;
        return result;
    }
    
    /** 分页区间内部结构：start/end 为 subList 边界。 */
    private static class PageMetadata {
        
        private int start;
        
        private int end;
    }
}
