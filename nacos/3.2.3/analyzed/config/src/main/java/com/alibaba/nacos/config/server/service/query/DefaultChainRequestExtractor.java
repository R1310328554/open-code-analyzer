/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.service.query;

import com.alibaba.nacos.api.config.remote.request.ConfigQueryRequest;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.model.gray.BetaGrayRule;
import com.alibaba.nacos.config.server.model.gray.TagGrayRule;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainRequest;
import com.alibaba.nacos.config.server.utils.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.api.common.Constants.VIPSERVER_TAG;

/**
 * 默认配置查询请求提取器（SPI 名 nacos）：从 HTTP 或 gRPC 请求解析
 * dataId、group、tenant、灰度标签与客户端 IP 标签，供责任链灰度路由使用。
 * DefaultChainRequestExtractor.
 *
 * @author Nacos
 */
public class DefaultChainRequestExtractor implements ConfigQueryChainRequestExtractor {
    
    /** SPI 实现名，固定返回 {@code nacos}。 */
    @Override
    public String getName() {
        return "nacos";
    }
    
    @Override
    public ConfigQueryChainRequest extract(HttpServletRequest request) {
        final String dataId = request.getParameter("dataId");
        final String group = request.getParameter("group");
        // namespaceId 与 tenant 参数二选一，空则归一化为空串
            ? request.getParameter("namespaceId") : request.getParameter("tenant");
        if (StringUtils.isBlank(tenant)) {
            tenant = StringUtils.EMPTY;
        }
        String tag = request.getParameter("tag");
        // 读取 VIPServer 自动标签头，用于无显式 tag 时的灰度匹配
        String clientIp = RequestUtil.getRemoteIp(request);
        
        Map<String, String> appLabels = new HashMap<>(4);
        // 写入客户端 IP 标签，供 Beta 灰度规则匹配
        if (StringUtils.isNotBlank(tag)) {
            appLabels.put(TagGrayRule.VIP_SERVER_TAG_LABEL, tag);
        } else if (StringUtils.isNotBlank(autoTag)) {
            appLabels.put(TagGrayRule.VIP_SERVER_TAG_LABEL, autoTag);
        }
        
        ConfigQueryChainRequest chainRequest = new ConfigQueryChainRequest();
        chainRequest.setDataId(dataId);
        chainRequest.setGroup(group);
        chainRequest.setTenant(tenant);
        chainRequest.setTag(tag);
        chainRequest.setAppLabels(appLabels);
        
        return chainRequest;
    }
    
    /**
     * 从 gRPC 请求与元数据构建链式查询对象，合并 appLabels 供灰度链使用。
     *
     * @param request     RPC 配置查询请求
     * @param requestMeta 含 clientIp 与 appLabels 的元数据
     * @return 责任链统一请求模型
     */
    @Override
    public ConfigQueryChainRequest extract(ConfigQueryRequest request, RequestMeta requestMeta) {
        ConfigQueryChainRequest chainRequest = new ConfigQueryChainRequest();
        
        String tag = request.getTag();
        Map<String, String> appLabels = new HashMap<>(4);
        appLabels.put(BetaGrayRule.CLIENT_IP_LABEL, requestMeta.getClientIp());
        if (StringUtils.isNotBlank(tag)) {
            appLabels.put(TagGrayRule.VIP_SERVER_TAG_LABEL, tag);
        } else {
            appLabels.putAll(requestMeta.getAppLabels());
        }
        
        chainRequest.setDataId(request.getDataId());
        chainRequest.setGroup(request.getGroup());
        chainRequest.setTenant(request.getTenant());
        chainRequest.setTag(request.getTag());
        chainRequest.setAppLabels(appLabels);
        
        return chainRequest;
    }
}
