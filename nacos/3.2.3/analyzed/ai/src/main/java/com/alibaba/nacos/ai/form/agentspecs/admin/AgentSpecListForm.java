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

package com.alibaba.nacos.ai.form.agentspecs.admin;

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * AgentSpec list form.
 * <p>AgentSpec 分页列表查询表单，支持精确/模糊搜索模式与按下载量等字段排序；列表场景下 agentSpecName 为可选参数。</p>
 *
 * @author nacos
 */
public class AgentSpecListForm extends AgentSpecForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 搜索模式：{@code accurate} 精确匹配或 {@code blur} 模糊匹配。 */
    private String search;
    
    /**
     * Sort field. Supported values: "download_count". Defaults to gmt_modified when null or empty.
     * <p>排序字段，当前支持 {@code download_count}；为空时默认按 gmt_modified 排序。</p>
     */
    private String orderBy;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultNamespaceId();
        // 列表查询时 agentSpecName 为可选参数
        if (StringUtils.isNotBlank(search)
            && !Constants.AgentSpecs.SEARCH_ACCURATE.equalsIgnoreCase(search)
            && !Constants.AgentSpecs.SEARCH_BLUR.equalsIgnoreCase(search)) {
            throw new NacosApiException(NacosApiException.INVALID_PARAM,
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Request parameter `search` should be `accurate` or `blur`.");
        }
    }
    
    public String getSearch() {
        return search;
    }
    
    public void setSearch(String search) {
        this.search = search;
    }
    
    public String getOrderBy() {
        return orderBy;
    }
    
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
