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

package com.alibaba.nacos.config.server.paramcheck;

import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.paramcheck.AbstractHttpParamExtractor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置模糊搜索 HTTP 参数提取器：当 {@code search=blur} 时跳过参数校验，
 * 否则从 tenant/dataId/group 构造 {@link ParamInfo} 供统一参数检查。
 * The type Config blur search http param extractor.
 *
 * @author zhuoguang
 */
public class ConfigBlurSearchHttpParamExtractor extends AbstractHttpParamExtractor {
    
    /** 模糊搜索模式标识，匹配时不做精确参数校验 */
    private static final String BLUR_SEARCH_MODE = "blur";
    
    /**
     * 从 HTTP 请求提取待校验的配置三元组。
     *
     * @param request 当前 HTTP 请求
     * @return 参数信息列表；模糊搜索模式下返回空列表
     */
    @Override
    public List<ParamInfo> extractParam(HttpServletRequest request) {
        String searchMode = request.getParameter("search");
        ArrayList<ParamInfo> paramInfos = new ArrayList<>();
        // TODO 后续可将 '*' 替换为空字符后仍执行校验
        if (StringUtils.equals(searchMode, BLUR_SEARCH_MODE)) {
            return paramInfos;
        }
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setNamespaceId(request.getParameter("tenant"));
        paramInfo.setDataId(request.getParameter("dataId"));
        paramInfo.setGroup(request.getParameter("group"));
        paramInfos.add(paramInfo);
        return paramInfos;
    }
}
