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

/**
 * Nacos 扩展版 MCP 服务器列表表单，在官方表单基础上增加 namespaceId。
 *
 * <p>继承 {@link ListServersOfficialForm}，供 Nacos 内部 API 按命名空间过滤服务器。</p>
 *
 * @author xinluo
 */
public class ListServersNacosForm extends ListServersOfficialForm {
    
    private String namespaceId;
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
}
