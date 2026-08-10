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

package com.alibaba.nacos.ai.param;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.paramcheck.AbstractHttpParamExtractor;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;

/**
 * Nacos AI Prompt param extractor.
 * <p>Prompt 资源 HTTP 请求参数提取器，从 promptKey 推导 Config 的 dataId（追加 .json 后缀）。</p>
 *
 * @author nacos
 */
public class PromptHttpParamExtractor extends AbstractHttpParamExtractor {
    
    /** Prompt 配置 dataId 后缀。 */
    private static final String PROMPT_DATA_ID_SUFFIX = ".json";
    
    @Override
    public List<ParamInfo> extractParam(HttpServletRequest request) throws NacosException {
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setNamespaceId(request.getParameter("namespaceId")); // 命名空间
        String promptKey = request.getParameter("promptKey");
        if (StringUtils.isNotBlank(promptKey)) { // promptKey 非空时拼接 dataId
            paramInfo.setDataId(promptKey + PROMPT_DATA_ID_SUFFIX);
        }
        return Collections.singletonList(paramInfo);
    }
}
