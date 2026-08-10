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
import com.alibaba.nacos.ai.form.skills.client.SkillQueryForm;
import com.alibaba.nacos.ai.param.SkillHttpParamExtractor;
import com.alibaba.nacos.ai.service.skills.SkillClientOperationService;
import com.alibaba.nacos.ai.service.skills.SkillQueryResult;
import com.alibaba.nacos.ai.utils.SkillRequestUtil;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.alibaba.nacos.plugin.auth.constant.Constants.Tag.ALLOW_ANONYMOUS;

/**
 * Skill 客户端控制器，供运行时只读下载 Skill ZIP。
 *
 * <p>支持 label / version / latest 解析；客户端可携带 md5 实现监听器式 304 轮询。</p>
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.Skills.CLIENT_PATH)
@ExtractorManager.Extractor(httpExtractor = SkillHttpParamExtractor.class)
public class SkillClientController {
    
    /** Skill 客户端读操作服务 */
    private final SkillClientOperationService skillClientOperationService;
    
    public SkillClientController(SkillClientOperationService skillClientOperationService) {
        this.skillClientOperationService = skillClientOperationService;
    }
    
    /**
     * 按 label / version / latest 下载已上线 Skill 版本为 ZIP。
     *
     * <p>支持监听器式轮询：{@code md5} 与发布内容一致时返回 HTTP 304 及
     * {@code ETag}/{@code X-Nacos-Skill-Md5} 头，客户端可继续使用本地缓存。</p>
     */
    @Since("3.2.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API,
        tags = {ALLOW_ANONYMOUS})
    public ResponseEntity<byte[]> get(SkillQueryForm form) throws NacosException {
        form.validate();
        SkillQueryResult result = skillClientOperationService.querySkill(form.getNamespaceId(),
            form.getName(), form.getVersion(), form.getLabel(), form.getMd5());
        if (result.isNotModified()) {
            // 客户端 md5 与发布内容一致：回显 ETag，无需重新加载 Skill 字节
            // re-loading the skill bytes.
            return SkillRequestUtil.buildSkillNotModifiedResponse(result.getMd5(),
                result.getResolvedVersion());
        }
        return SkillRequestUtil.buildSkillZipResponseWithMd5(result.getSkill(),
            result.getMd5(), result.getResolvedVersion());
    }
}
