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

/**
 * Prompt query form for client read API.
 * <p>客户端运行时 Prompt 只读查询表单，支持按 version、label 或 md5 条件拉取内容。</p>
 *
 * @author nacos
 */
public class PromptQueryForm extends PromptForm {
    
    /** 指定版本号，与 label 二选一或组合使用。 */
    private String version;
    
    /** 语义标签（如 latest、stable），解析为对应版本。 */
    private String label;
    
    /** 客户端本地缓存 md5，用于判断内容是否变更。 */
    private String md5;
    
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
