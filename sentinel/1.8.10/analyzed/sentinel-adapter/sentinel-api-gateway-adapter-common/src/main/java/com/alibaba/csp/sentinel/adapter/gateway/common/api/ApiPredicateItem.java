/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.common.api;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * 网关 API 谓词项标记接口，用于定义 URL 或参数匹配条件。
 *
 * @author Eric Zhao
 * @since 1.6.0
 */
public interface ApiPredicateItem {

    /**
     * 将两个 {@link ApiPredicateItem} 组合为 AND 关系（已注释）。
     *
     * @param item another predicate item
     * @return combined predicate group item
     */
    /*default ApiPredicateItem and(ApiPredicateItem item) {
        AssertUtil.notNull(item, "item cannot be null");
        return new ApiPredicateGroupItem()
            .addItem(this).addItem(item);
    }*/
}
