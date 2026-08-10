/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.visibility.spi;

import com.alibaba.nacos.plugin.visibility.model.AuthorizedResources;
import com.alibaba.nacos.plugin.visibility.model.BaseVisibilityPredicate;

/**
 * 可见性范围/列表查询建议器。
 *
 * <p>组合基础谓词与已授权资源集合，指导存储层执行过滤查询。</p>
 *
 * @author xiweng.yy
 */
public class QueryAdvisor {
    
    /** 基础可见性谓词，默认为公开与所有者并集。 */
    private BaseVisibilityPredicate basePredicate = BaseVisibilityPredicate.PUBLIC_AND_OWNER;
    
    /** 已授权资源过滤条件。 */
    private AuthorizedResources authorizedPredicate = new AuthorizedResources();
    
    /**
     * 获取基础可见性谓词。
     *
     * @return 基础谓词
     */
    public BaseVisibilityPredicate getBasePredicate() {
        return basePredicate;
    }
    
    /**
     * 设置基础可见性谓词。
     *
     * @param basePredicate 基础谓词
     */
    public void setBasePredicate(BaseVisibilityPredicate basePredicate) {
        this.basePredicate = basePredicate;
    }
    
    /**
     * 获取已授权资源过滤条件。
     *
     * @return 已授权资源集合
     */
    public AuthorizedResources getAuthorizedPredicate() {
        return authorizedPredicate;
    }
    
    /**
     * 设置已授权资源过滤条件。
     *
     * @param authorizedPredicate 已授权资源集合
     */
    public void setAuthorizedPredicate(AuthorizedResources authorizedPredicate) {
        this.authorizedPredicate = authorizedPredicate;
    }
}
