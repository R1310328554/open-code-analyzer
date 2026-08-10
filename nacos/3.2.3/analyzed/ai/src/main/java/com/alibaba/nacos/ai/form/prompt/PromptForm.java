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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.NamespaceUtil;

import java.io.Serial;

/**
 * Prompt form base class.
 * <p>Prompt 管理端请求表单基类，封装 namespaceId 与 promptKey；校验时 promptKey 必填，命名空间为空时由 {@link com.alibaba.nacos.common.utils.NamespaceUtil} 处理默认值。</p>
 *
 * @author nacos
 */
public class PromptForm implements NacosForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 命名空间 ID，为空时自动填充默认命名空间。 */
    private String namespaceId;
    
    /** Prompt 资源唯一键，多数管理端操作必填。 */
    private String promptKey;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultNamespaceId();
        if (StringUtils.isEmpty(promptKey)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'promptKey' type String is not present");
        }
    }
    
    /** 命名空间参数为空时填充并规范化 namespaceId。 */
    protected void fillDefaultNamespaceId() {
        namespaceId = NamespaceUtil.processNamespaceParameter(namespaceId);
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getPromptKey() {
        return promptKey;
    }
    
    public void setPromptKey(String promptKey) {
        this.promptKey = promptKey;
    }
}
