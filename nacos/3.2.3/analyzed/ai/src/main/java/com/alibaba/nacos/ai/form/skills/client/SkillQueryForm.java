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

package com.alibaba.nacos.ai.form.skills.client;

import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Skill query form for client runtime get.
 * <p>客户端运行时 Skill 单条查询表单，支持按 name、version、label 定位；携带 md5 时可触发 304 未修改响应以复用本地缓存。</p>
 *
 * @author nacos
 */
public class SkillQueryForm {
    
    /** 命名空间 ID，为空时默认 public。 */
    private String namespaceId;
    
    /** Skill 资源名称，必填。 */
    private String name;
    
    /** 目标版本号，与 label 二选一或组合使用。 */
    private String version;
    
    /** 语义标签（如 latest），用于解析到具体版本。 */
    private String label;
    
    /**
     * Optional content MD5 carried by skill listener. When provided and matches the published
     * content MD5, the server returns NOT_MODIFIED so the client may keep its local cache.
     * <p>客户端监听器携带的内容 MD5；与已发布内容一致时服务端返回 NOT_MODIFIED，客户端可保留本地缓存。</p>
     */
    private String md5;
    
    /**
     * Validate and normalize query parameters.
     * <p>校验并规范化查询参数：命名空间为空时填充 public，name 必填。</p>
     *
     * @throws NacosApiException if required parameters are missing
     */
    public void validate() throws NacosApiException {
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = "public"; // 默认 public 命名空间
        }
        if (StringUtils.isBlank(name)) {
            throw new NacosApiException(NacosApiException.INVALID_PARAM,
                ErrorCode.PARAMETER_MISSING,
                "Skill name is required");
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
