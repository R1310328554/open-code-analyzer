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

package com.alibaba.nacos.core.namespace.model.form;

import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.NacosForm;
import org.springframework.http.HttpStatus;

/**
 * 命名空间 HTTP API 基础表单，实现 {@link NacosForm} 参数校验契约。
 * Nacos HTTP namespace API basic form.
 *
 * @author xiweng.yy
 * @author dongyafei
 */
public class NamespaceForm implements NacosForm {
    
    private static final long serialVersionUID = -1078976569495343487L;
    
    /** 命名空间 ID。 */
    private String namespaceId;
    
    /** 命名空间名称。 */
    private String namespaceName;
    
    /** 命名空间描述。 */
    private String namespaceDesc;
    
    /** 无参构造。 */
    public NamespaceForm() {
    }
    
    /**
     * 全字段构造。
     *
     * @param namespaceId 命名空间 ID
     * @param namespaceName 命名空间名称
     * @param namespaceDesc 命名空间描述
     */
    public NamespaceForm(String namespaceId, String namespaceName, String namespaceDesc) {
        this.namespaceId = namespaceId;
        this.namespaceName = namespaceName;
        this.namespaceDesc = namespaceDesc;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getNamespaceName() {
        return namespaceName;
    }
    
    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }
    
    public String getNamespaceDesc() {
        return namespaceDesc;
    }
    
    public void setNamespaceDesc(String namespaceDesc) {
        this.namespaceDesc = namespaceDesc;
    }
    
    /** 校验 namespaceId 与 namespaceName 非空。 */
    @Override
    public void validate() throws NacosApiException {
        if (null == namespaceId) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "required parameter 'namespaceId' is missing");
        }
        if (null == namespaceName) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.PARAMETER_MISSING,
                "required parameter 'namespaceName' is missing");
        }
    }
}
