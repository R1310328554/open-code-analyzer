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

package com.alibaba.nacos.api.ai.remote.request;

/**
 * 查询 Nacos AI 模块中 Prompt 配置的远程请求。
 *
 * <p>继承 {@link AbstractPromptRequest}，支持按 {@link #version}、{@link #label} 与 {@link #md5}
 * 精确定位 Prompt 内容。</p>
 *
 * @author nacos
 */
public class QueryPromptRequest extends AbstractPromptRequest {
    
    /** 目标 Prompt 版本号。 */
    private String version;
    
    /** Prompt 标签，用于灰度或环境区分。 */
    private String label;
    
    /** Prompt 内容 MD5 校验值，用于一致性校验。 */
    private String md5;
    
    /** 获取查询目标版本号。 */
    public String getVersion() {
        return version;
    }
    
    /** 设置查询目标版本号。 */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /** 获取 Prompt 标签。 */
    public String getLabel() {
        return label;
    }
    
    /** 设置 Prompt 标签。 */
    public void setLabel(String label) {
        this.label = label;
    }
    
    /** 获取 Prompt 内容 MD5 校验值。 */
    public String getMd5() {
        return md5;
    }
    
    /** 设置 Prompt 内容 MD5 校验值。 */
    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
