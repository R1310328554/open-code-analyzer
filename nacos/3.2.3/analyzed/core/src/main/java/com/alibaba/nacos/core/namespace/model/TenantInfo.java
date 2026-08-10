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

package com.alibaba.nacos.core.namespace.model;

import java.io.Serializable;

/**
 * 租户（命名空间）持久化模型，对应 tenant_info 表行。
 * <p>旧称 TenantInfo，API 层现以 {@link com.alibaba.nacos.api.model.response.Namespace} 对外暴露。</p>
 * TenantInfo.
 *
 * <p>Old name of {@link com.alibaba.nacos.api.model.response.Namespace}</p>
 *
 * @author Nacos
 */
public class TenantInfo implements Serializable {
    
    private static final long serialVersionUID = -1498218072016383809L;
    
    /** 命名空间 ID（tenant_id）。 */
    private String tenantId;
    
    /** 命名空间显示名称。 */
    private String tenantName;
    
    /** 命名空间描述。 */
    private String tenantDesc;
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getTenantName() {
        return tenantName;
    }
    
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
    
    public String getTenantDesc() {
        return tenantDesc;
    }
    
    public void setTenantDesc(String tenantDesc) {
        this.tenantDesc = tenantDesc;
    }
    
}
