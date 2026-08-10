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

package com.alibaba.nacos.core.paramcheck.impl;

import com.alibaba.nacos.api.ai.remote.request.QueryPromptRequest;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.paramcheck.AbstractRpcParamExtractor;

import java.util.List;

/**
 * AI Prompt 查询 gRPC 请求参数提取器，从 {@link com.alibaba.nacos.api.ai.remote.request.QueryPromptRequest} 解析命名空间与 dataId。
 * Nacos prompt grpc request param extractor.
 *
 * @author nacos
 */
public class PromptRequestParamExtractor extends AbstractRpcParamExtractor {
    
    /** Prompt 配置在配置中心对应的 dataId 后缀。 */
    private static final String PROMPT_DATA_ID_SUFFIX = ".json";
    
    /**
     * 提取 Prompt 查询请求的命名空间与 dataId（promptKey + .json）。
     *
     * @param request RPC 请求
     * @return 待校验参数列表
     * @throws NacosException 提取异常
     */
    @Override
    public List<ParamInfo> extractParam(Request request) throws NacosException {
        QueryPromptRequest promptRequest = (QueryPromptRequest) request;
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setNamespaceId(promptRequest.getNamespaceId());
        if (StringUtils.isNotBlank(promptRequest.getPromptKey())) {
            paramInfo.setDataId(promptRequest.getPromptKey() + PROMPT_DATA_ID_SUFFIX);
        }
        return List.of(paramInfo);
    }
}
