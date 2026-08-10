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

package com.alibaba.nacos.api.ai.remote.response;

import com.alibaba.nacos.api.ai.model.prompt.Prompt;
import com.alibaba.nacos.api.remote.response.Response;

/**
 * 查询 Prompt 配置的远程响应。
 *
 * <p>继承 {@link com.alibaba.nacos.api.remote.response.Response}，
 * 通过 {@link #promptInfo} 返回 {@link com.alibaba.nacos.api.ai.model.prompt.Prompt} 完整内容。</p>
 *
 * @author nacos
 */
public class QueryPromptResponse extends Response {
    
    /** 查询到的 Prompt 完整信息。 */
    private Prompt promptInfo;
    
    /** 获取 Prompt 完整信息。 */
    public Prompt getPromptInfo() {
        return promptInfo;
    }
    
    /** 设置 Prompt 完整信息。 */
    public void setPromptInfo(Prompt promptInfo) {
        this.promptInfo = promptInfo;
    }
}
