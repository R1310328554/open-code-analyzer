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

package com.alibaba.nacos.ai.form.skills.admin;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * Skill publish form.
 * <p>Skill 版本发布表单，将指定版本发布上线；可选 {@link #updateLatestLabel} 同步更新 latest 语义标签。</p>
 *
 * @author nacos
 */
public class SkillPublishForm extends SkillForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 待发布的版本号，必填。 */
    private String version;
    
    /** 为 true 时发布成功后自动将 latest 标签指向该版本。 */
    private Boolean updateLatestLabel;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultNamespaceId(); // 补全默认命名空间
        if (StringUtils.isBlank(getSkillName())) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Request parameter `skillName` should not be blank.");
        }
        if (StringUtils.isBlank(version)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Request parameter `version` should not be blank.");
        }
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Boolean getUpdateLatestLabel() {
        return updateLatestLabel;
    }
    
    public void setUpdateLatestLabel(Boolean updateLatestLabel) {
        this.updateLatestLabel = updateLatestLabel;
    }
}
