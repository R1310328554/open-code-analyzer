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

package com.alibaba.nacos.ai.form.agentspecs.client;

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * AgentSpec query form for client runtime get.
 * <p>客户端运行时按名称/版本/标签获取 AgentSpec 的查询表单，支持 md5 条件校验以实现增量拉取。</p>
 *
 * @author nacos
 */
public class AgentSpecQueryForm {
    
    /** 命名空间 ID，为空时使用默认命名空间。 */
    private String namespaceId;
    
    /** AgentSpec 名称，必填。 */
    private String name;
    
    /** 指定版本号，与 label 二选一或组合使用。 */
    private String version;
    
    /** 语义标签（如 latest、stable），解析为对应版本。 */
    private String label;
    
    /** 客户端本地缓存 md5，用于判断内容是否变更。 */
    private String md5;
    
    /**
     * Validate and normalize query parameters.
     * <p>校验并规范化查询参数：补全默认命名空间，name 不能为空。</p>
     *
     * @throws NacosApiException if required parameters are missing
     */
    public void validate() throws NacosApiException {
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = Constants.AgentSpecs.AGENTSPEC_DEFAULT_NAMESPACE;
        }
        if (StringUtils.isBlank(name)) {
            throw new NacosApiException(NacosApiException.INVALID_PARAM,
                ErrorCode.PARAMETER_MISSING,
                "AgentSpec name is required");
        }
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
