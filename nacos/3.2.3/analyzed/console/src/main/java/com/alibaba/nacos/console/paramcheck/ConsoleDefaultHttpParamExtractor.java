/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.paramcheck;

import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.paramcheck.AbstractHttpParamExtractor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 控制台默认 HTTP 参数提取器：从请求参数中解析命名空间 ID 与展示名，供参数校验链路使用。
 * Console default http param extractor.
 *
 * @author zhuoguang
 */
public class ConsoleDefaultHttpParamExtractor extends AbstractHttpParamExtractor {
    
    /** 从 HTTP 请求提取命名空间相关参数并封装为 {@link ParamInfo} 列表。 */
    @Override
    public List<ParamInfo> extractParam(HttpServletRequest request) {
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setNamespaceId(getAliasNamespaceId(request));
        paramInfo.setNamespaceShowName(getAliasNamespaceShowName(request));
        ArrayList<ParamInfo> paramInfos = new ArrayList<>();
        paramInfos.add(paramInfo);
        return paramInfos;
    }
    
    /** 解析命名空间 ID，优先 {@code namespaceId}，否则回退 {@code customNamespaceId}。 */
    private String getAliasNamespaceId(HttpServletRequest request) {
        String namespaceId = request.getParameter("namespaceId");
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = request.getParameter("customNamespaceId");
        }
        return namespaceId;
    }
    
    /** 从 {@code namespaceName} 请求参数读取命名空间展示名。 */
    private String getAliasNamespaceShowName(HttpServletRequest request) {
        return request.getParameter("namespaceName");
    }
}
