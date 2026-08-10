/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.auth.parser.http;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Properties;

/**
 * 配置中心 HTTP 资源解析器。
 *
 * <p>从 HTTP 请求参数中提取 tenant（命名空间）、group 与 dataId；
 * 支持通过 {@link Secured#tags()} 动态映射命名空间参数名。</p>
 *
 * @author xiweng.yy
 */
public class ConfigHttpResourceParser extends AbstractHttpResourceParser {
    
    /** {@inheritDoc} — 依次尝试 namespaceId、tenant 参数并规范化命名空间。 */
    @Override
    protected String getNamespaceId(HttpServletRequest request) {
        String namespaceId = request.getParameter(Constants.NAMESPACE_ID);
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = request.getParameter(Constants.TENANT);
        }
        return NamespaceUtil.processNamespaceParameter(namespaceId);
    }
    
    /**
     * 根据 {@link Secured#tags()} 动态解析命名空间参数。
     *
     * <p>tags 中以 namespaceId 开头的条目可指定实际参数名，否则回退到 {@link #getNamespaceId(HttpServletRequest)}。</p>
     *
     * @param request HTTP 请求
     * @param secured 接口安全注解
     * @return 规范化后的命名空间 ID
     */
    @Override
    protected String getNamespaceId(HttpServletRequest request, Secured secured) {
        return Arrays.stream(secured.tags()).filter(tag -> tag.startsWith(Constants.NAMESPACE_ID))
            .map(tag -> tag
                .split(com.alibaba.nacos.plugin.auth.constant.Constants.Resource.SPLITTER))
            .filter(splitTags -> splitTags.length >= 2)
            .map(splitTags -> request.getParameter(splitTags[1]))
            .filter(StringUtils::isNotBlank).findFirst().orElseGet(() -> getNamespaceId(request));
    }
    
    /** {@inheritDoc} — 依次尝试 groupName、group 请求参数。 */
    @Override
    protected String getGroup(HttpServletRequest request) {
        String groupName = request.getParameter(Constants.GROUP_NAME);
        if (StringUtils.isBlank(groupName)) {
            groupName = request.getParameter(com.alibaba.nacos.api.common.Constants.GROUP);
        }
        return StringUtils.isBlank(groupName) ? StringUtils.EMPTY : groupName;
    }
    
    /** {@inheritDoc} — 从 dataId 请求参数读取配置资源名。 */
    @Override
    protected String getResourceName(HttpServletRequest request) {
        String dataId = request.getParameter(com.alibaba.nacos.api.common.Constants.DATA_ID);
        return StringUtils.isBlank(dataId) ? StringUtils.EMPTY : dataId;
    }
    
    /** {@inheritDoc} — 配置 HTTP 资源无额外扩展属性。 */
    @Override
    protected Properties getProperties(HttpServletRequest request) {
        return new Properties();
    }
}
