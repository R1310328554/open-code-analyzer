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

package com.alibaba.nacos.ai.form.prompt;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * Prompt online/offline form.
 * <p>Prompt 上线/下线操作表单，指定 version 将对应版本启用或禁用。</p>
 *
 * @author nacos
 */
public class PromptOnlineForm extends PromptForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 目标版本号，上下线操作必填。 */
    private String version;
    
    @Override
    public void validate() throws NacosApiException {
        super.validate(); // 先执行基类 promptKey 校验
        if (StringUtils.isBlank(version)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'version' type String is not present");
        }
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}
