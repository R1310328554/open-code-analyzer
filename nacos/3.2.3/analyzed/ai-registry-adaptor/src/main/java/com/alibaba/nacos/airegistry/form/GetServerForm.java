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

package com.alibaba.nacos.airegistry.form;

import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;

/**
 * Get mcp server form.
 * <p>查询 MCP 服务详情的请求表单，携带 namespaceId 等查询参数；用于 {@link com.alibaba.nacos.airegistry.controller.McpRegistryController} 版本查询接口。</p>
 * @author xinluo
 */
public class GetServerForm implements NacosForm {
    
    /** 目标命名空间 ID。 */
    private String namespaceId;
    
    /** 返回 namespaceId。 */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    /** 设置 namespaceId。 */
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /**
     * check form parameters while valid.
     * <p>校验表单参数；当前无额外必填项，预留扩展点。</p>
     *
     * @throws NacosApiException when form parameters is invalid.
     */
    @Override
    public void validate() throws NacosApiException {
    }
}
