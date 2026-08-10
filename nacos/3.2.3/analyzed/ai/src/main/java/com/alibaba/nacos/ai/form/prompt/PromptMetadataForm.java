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

import java.io.Serial;

/**
 * Prompt metadata update form.
 *
 * <p>Used for updating prompt description without changing version.</p>
 * <p>Prompt 元数据更新表单，在不变更版本的前提下更新描述与业务标签。</p>
 *
 * @author nacos
 */
public class PromptMetadataForm extends PromptForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * New description for the prompt.
     * <p>Prompt 的新描述文本，写入配置元数据。</p>
     */
    private String description;
    
    /**
     * Prompt biz tags (comma-separated).
     * <p>Prompt 业务标签，逗号分隔多个标签值。</p>
     */
    private String bizTags;
    
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
}
