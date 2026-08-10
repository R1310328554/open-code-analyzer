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

import com.alibaba.nacos.api.naming.CommonParams;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.common.utils.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Properties;

/**
 * 命名服务 HTTP 资源解析器。
 *
 * <p>从 HTTP 请求参数中提取 namespaceId、groupName 与 serviceName；
 * 服务名可能含 {@code group@@service} 格式，需借助 {@link NamingUtils} 拆分。</p>
 *
 * @author xiweng.yy
 */
public class NamingHttpResourceParser extends AbstractHttpResourceParser {
    
    /** {@inheritDoc} — 读取并规范化 namespaceId 请求参数。 */
    @Override
    protected String getNamespaceId(HttpServletRequest request) {
        return NamespaceUtil
            .processNamespaceParameter(request.getParameter(CommonParams.NAMESPACE_ID));
        
    }
    
    /**
     * 从 HTTP 请求解析分组名。
     *
     * <p>HTTP 请求中的 groupName 可能为空，此时 serviceName 可能为 {@code group@@service} 格式，
     * 需从 serviceName 中解析分组。</p>
     *
     * @param request HTTP 请求
     * @return 分组名，无法解析时返回空字符串
     */
    @Override
    protected String getGroup(HttpServletRequest request) {
        String groupName = request.getParameter(CommonParams.GROUP_NAME);
        if (StringUtils.isBlank(groupName)) {
            String serviceName = request.getParameter(CommonParams.SERVICE_NAME);
            groupName = NamingUtils.getGroupName(serviceName);
        }
        return StringUtils.isBlank(groupName) ? StringUtils.EMPTY : groupName;
    }
    
    /** {@inheritDoc} — 从 serviceName 参数解析纯服务名（去除分组前缀）。 */
    @Override
    protected String getResourceName(HttpServletRequest request) {
        // 逻辑与 #getGroup 对应：serviceName 可能含 group@@service 前缀
        String serviceName =
            NamingUtils.getServiceName(request.getParameter(CommonParams.SERVICE_NAME));
        return StringUtils.isBlank(serviceName) ? StringUtils.EMPTY : serviceName;
    }
    
    /** {@inheritDoc} — 命名 HTTP 资源无额外扩展属性。 */
    @Override
    protected Properties getProperties(HttpServletRequest request) {
        return new Properties();
    }
}
