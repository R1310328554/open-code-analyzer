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

package com.alibaba.nacos.ai.form.a2a.admin;

import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * Agent Card 更新请求表单。
 *
 * <p>支持 {@code setAsLatest} 标记；更新场景下 registrationType 可选。</p>
 *
 * @author xiweng.yy
 */
public class AgentCardUpdateForm extends AgentCardForm {
    
    @Serial
    private static final long serialVersionUID = 353698557363707304L;
    
    /** 是否将本次更新设为 latest 版本 */
    private boolean setAsLatest;
    
    public boolean getSetAsLatest() {
        return setAsLatest;
    }
    
    public void setSetAsLatest(boolean setAsLatest) {
        this.setAsLatest = setAsLatest;
    }
    
    @Override
    protected void fillDefaultRegistrationType() {
        // 更新请求无需填充默认 registrationType
    }
    
    @Override
    protected void validateRegistrationType() throws NacosApiException {
        // 未传 registrationType 表示不修改注册类型
        if (StringUtils.isEmpty(getRegistrationType())) {
            return;
        }
        super.validateRegistrationType();
    }
}
