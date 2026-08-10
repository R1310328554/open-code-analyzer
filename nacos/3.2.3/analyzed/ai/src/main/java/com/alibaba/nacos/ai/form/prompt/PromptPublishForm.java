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

package com.alibaba.nacos.ai.form.prompt;

import com.alibaba.nacos.ai.utils.PromptVersionUtils;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * Prompt publish form.
 * <p>Prompt 版本发布表单（旧版 API），携带模板内容、变量定义与提交说明，版本号须符合 major.minor.patch 格式。</p>
 *
 * @author nacos
 */
public class PromptPublishForm extends PromptForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * Version in format "major.minor.patch" (e.g., "1.0.0").
     * <p>版本号，格式为 major.minor.patch，例如 1.0.0。</p>
     */
    private String version;
    
    /**
     * Prompt template content.
     * <p>Prompt 模板正文内容。</p>
     */
    private String template;
    
    /**
     * Commit message for this version.
     * <p>本版本的提交说明信息。</p>
     */
    private String commitMsg;
    
    /**
     * Description for the prompt (optional, stored in config metadata).
     * <p>Prompt 描述，可选，存储于配置元数据中。</p>
     */
    private String description;
    
    /**
     * Prompt biz tags (comma-separated, optional).
     * <p>Prompt 业务标签，逗号分隔，可选。</p>
     */
    private String bizTags;
    
    /**
     * Variable definitions with default values (JSON array string, optional).
     *
     * <p>Example: [{"name":"question","defaultValue":"Hello","description":"User question"}]</p>
     * <p>变量定义 JSON 数组，含 name、defaultValue、description 等字段，可选。</p>
     */
    private String variables;
    
    @Override
    public void validate() throws NacosApiException {
        super.validate(); // 先执行基类 promptKey 校验
        
        if (StringUtils.isEmpty(version)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'version' type String is not present");
        }
        
        if (!PromptVersionUtils.isValidVersion(version)) {
            throw new NacosApiException(NacosException.INVALID_PARAM,
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Parameter 'version' must be in format 'major.minor.patch' (e.g., '1.0.0')");
        }
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public void setTemplate(String template) {
        this.template = template;
    }
    
    public String getCommitMsg() {
        return commitMsg;
    }
    
    public void setCommitMsg(String commitMsg) {
        this.commitMsg = commitMsg;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBizTags() {
        return bizTags;
    }
    
    public void setBizTags(String bizTags) {
        this.bizTags = bizTags;
    }
    
    public String getVariables() {
        return variables;
    }
    
    public void setVariables(String variables) {
        this.variables = variables;
    }
}
