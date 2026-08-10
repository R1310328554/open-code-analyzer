/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.persistence.handler.support;

import com.alibaba.nacos.plugin.auth.impl.model.OffsetFetchResult;
import com.alibaba.nacos.plugin.auth.impl.persistence.handler.PageHandlerAdapter;

/**
 * 默认分页适配器（不改写 SQL）。
 *
 * <p>{@link #supports} 恒为 false，仅作为外部数据源无匹配方言时的兜底， 原样返回 SQL 与参数。</p>
 *
 * @author huangKeMing
 */
public class DefaultPageHandlerAdapter implements PageHandlerAdapter {
    
    /** 默认适配器不匹配任何数据源类型。 */
    @Override
    public boolean supports(String dataSourceType) {
        return false;
    }
    
    /** 不追加方言分页子句，直接封装原 SQL。 */
    @Override
    public OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,
        int pageSize) {
        return new OffsetFetchResult(fetchSql, arg);
    }
    
}
