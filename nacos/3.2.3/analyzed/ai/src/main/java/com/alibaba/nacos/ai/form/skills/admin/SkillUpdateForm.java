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

package com.alibaba.nacos.ai.form.skills.admin;

import java.io.Serial;

/**
 * Skill update form.
 * <p>Skill 更新表单，继承 {@link SkillDetailForm} 的 skillCard；可指定是否设为最新版本及提交说明。</p>
 *
 * @author nacos
 */
public class SkillUpdateForm extends SkillDetailForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * Whether to set as latest version.
     * <p>更新成功后是否将 latest 标签指向该版本。</p>
     */
    private Boolean setAsLatest;
    
    /** 本次更新的提交说明信息。 */
    private String commitMsg;
    
    public Boolean getSetAsLatest() {
        return setAsLatest;
    }
    
    public void setSetAsLatest(Boolean setAsLatest) {
        this.setAsLatest = setAsLatest;
    }
    
    public String getCommitMsg() {
        return commitMsg;
    }
    
    public void setCommitMsg(String commitMsg) {
        this.commitMsg = commitMsg;
    }
}
