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

import java.io.Serial;

/**
 * Prompt submit form.
 * <p>Prompt 版本提交审核表单，将草稿版本提交至发布流水线等待审核。</p>
 *
 * @author nacos
 */
public class PromptSubmitForm extends PromptForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * Optional; defaults to current editing version.
     * <p>待提交版本号，可选，默认使用当前编辑中的版本。</p>
     */
    private String version;
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}
