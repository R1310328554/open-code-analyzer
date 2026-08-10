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

package com.alibaba.nacos.ai.form.agentspecs.admin;

import java.io.Serial;

/**
 * AgentSpec update form.
 * <p>AgentSpec 更新表单，继承 {@link AgentSpecDetailForm} 的卡片内容校验，并可选择是否将当前版本设为 latest。</p>
 *
 * @author nacos
 */
public class AgentSpecUpdateForm extends AgentSpecDetailForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * Whether to set as latest version.
     * <p>更新完成后是否将当前版本标记为 latest 最新版本。</p>
     */
    private Boolean setAsLatest;
    
    public Boolean getSetAsLatest() {
        return setAsLatest;
    }
    
    public void setSetAsLatest(Boolean setAsLatest) {
        this.setAsLatest = setAsLatest;
    }
}
