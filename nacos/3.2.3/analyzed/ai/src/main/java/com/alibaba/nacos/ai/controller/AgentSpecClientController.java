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

package com.alibaba.nacos.ai.controller;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.form.agentspecs.client.AgentSpecQueryForm;
import com.alibaba.nacos.ai.form.agentspecs.client.AgentSpecSearchForm;
import com.alibaba.nacos.ai.service.agentspecs.AgentSpecOperationService;
import com.alibaba.nacos.ai.service.agentspecs.AgentSpecQueryResult;
import com.alibaba.nacos.ai.utils.AgentSpecRequestUtil;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpecBasicInfo;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.alibaba.nacos.plugin.auth.constant.Constants.Tag.ALLOW_ANONYMOUS;

/**
 * AgentSpec 客户端控制器，供运行时只读查询。
 *
 * <p>支持搜索已启用 AgentSpec 及按 label/version/latest 获取在线版本；
 * 支持基于 MD5 的条件查询，客户端缓存未过期时返回 304。</p>
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.AgentSpecs.CLIENT_PATH)
@ExtractorManager.Extractor(httpExtractor = ExtractorManager.DefaultHttpExtractor.class)
public class AgentSpecClientController {
    
    private final AgentSpecOperationService agentSpecOperationService;
    
    public AgentSpecClientController(AgentSpecOperationService agentSpecOperationService) {
        this.agentSpecOperationService = agentSpecOperationService;
    }
    
    /**
     * 搜索已启用的 AgentSpec，供运行时使用。
     */
    @Since("3.2.0")
    @GetMapping("/search")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API)
    public Result<Page<AgentSpecBasicInfo>> search(AgentSpecSearchForm form, PageForm pageForm)
        throws NacosException {
        form.validate();
        pageForm.validate();
        return Result.success(
            agentSpecOperationService.searchAgentSpecs(form.getNamespaceId(), form.getKeyword(),
                pageForm.getPageNo(), pageForm.getPageSize()));
    }
    
    /**
     * 按 label/version/latest 获取在线 AgentSpec 版本。
     * 支持基于 MD5 的条件查询：客户端缓存有效时返回 304。
     */
    @Since("3.2.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API,
        tags = {ALLOW_ANONYMOUS})
    public ResponseEntity<Result<AgentSpec>> get(AgentSpecQueryForm form)
        throws NacosException {
        form.validate();
        AgentSpecQueryResult result =
            agentSpecOperationService.queryAgentSpecForClient(
                form.getNamespaceId(), form.getName(), form.getVersion(),
                form.getLabel(), form.getMd5());
        if (result.isNotModified()) {
            return AgentSpecRequestUtil.buildAgentSpecNotModifiedResponse(result.getMd5());
        }
        return AgentSpecRequestUtil.buildAgentSpecResponse(result.getAgentSpec(),
            result.getMd5(), result.getResolvedVersion());
    }
}
