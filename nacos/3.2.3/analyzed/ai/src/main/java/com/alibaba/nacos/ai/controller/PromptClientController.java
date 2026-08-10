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

package com.alibaba.nacos.ai.controller;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.form.prompt.PromptQueryForm;
import com.alibaba.nacos.ai.param.PromptHttpParamExtractor;
import com.alibaba.nacos.ai.service.prompt.PromptClientOperationService;
import com.alibaba.nacos.ai.utils.PromptConvertUtils;
import com.alibaba.nacos.api.ai.model.prompt.Prompt;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionInfo;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prompt 客户端控制器，供运行时只读查询 Prompt 内容。
 *
 * <p>支持按 version / label / latest 优先级解析版本；客户端可携带 md5 实现 304 未修改响应。</p>
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.Prompt.CLIENT_PATH)
@ExtractorManager.Extractor(httpExtractor = PromptHttpParamExtractor.class)
public class PromptClientController {
    
    /** Prompt 客户端读操作服务 */
    private final PromptClientOperationService promptOperationService;
    
    public PromptClientController(PromptClientOperationService promptOperationService) {
        this.promptOperationService = promptOperationService;
    }
    
    /**
     * 按 version &gt; label &gt; latest 优先级查询 Prompt。
     * <p>校验表单后委托 {@link PromptClientOperationService}；若客户端 md5 与发布内容一致则返回 HTTP 304。</p>
     */
    @Since("3.2.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API)
    public Result<Prompt> queryPrompt(PromptQueryForm form, HttpServletResponse response)
        throws NacosException {
        form.validate();
        try {
            PromptVersionInfo result =
                promptOperationService.queryPrompt(form.getNamespaceId(), form.getPromptKey(),
                    form.getVersion(), form.getLabel(), form.getMd5());
            return Result.success(PromptConvertUtils.toClientPrompt(result));
        } catch (NacosException ex) {
            // 内容未变更：设置 304 状态并返回空结果
            if (ex.getErrCode() == NacosException.NOT_MODIFIED) {
                response.setStatus(NacosException.NOT_MODIFIED);
                return Result.success(null);
            }
            throw ex;
        }
    }
}
