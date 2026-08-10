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

package com.alibaba.nacos.maintainer.client.ai;

import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardDetailInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardVersionInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentInterface;
import com.alibaba.nacos.api.ai.model.a2a.AgentVersionDetail;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.utils.HttpMethod;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.maintainer.client.constants.Constants;
import com.alibaba.nacos.maintainer.client.model.HttpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link A2aMaintainerService} HTTP 实现：调用 AI Agent 管理 Admin API。
 *
 * <p>注册/更新失败时若服务端报旧字段缺失，会自动降级为 legacy AgentCard 格式重试。</p>
 */
final class A2aMaintainerServiceImpl extends AbstractAiDelegateMaintainerService
    implements A2aMaintainerService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(A2aMaintainerServiceImpl.class);
    
    /** 列表/搜索时的模糊匹配模式。 */
    private static final String SEARCH_BLUR = "blur";
    
    /** 列表/搜索时的精确匹配模式。 */
    private static final String SEARCH_ACCURATE = "accurate";
    
    /** 使用共享 HTTP 上下文构造 A2A 维护服务。 */
    A2aMaintainerServiceImpl(AiMaintainerHttpContext context) {
        super(context);
    }
    
    /** 注册 Agent；遇 legacy 参数错误时自动转换 AgentCard 并重试。 */
    @Override
    public boolean registerAgent(AgentCard agentCard, String namespaceId, String registrationType)
        throws NacosException {
        try {
            return doRegisterAgent(agentCard, namespaceId, registrationType);
        } catch (NacosException e) {
            if (shouldRetryWithLegacyFormat(e)) {
                LOGGER.info("Retry register agent {} with legacy fields for compatibility.",
                    agentCard.getName());
                return doRegisterAgent(buildLegacyCompatibleAgentCard(agentCard), namespaceId,
                    registrationType);
            }
            throw e;
        }
    }
    
    /** 执行 POST 注册请求并解析 {@link Result} 成功与否。 */
    private boolean doRegisterAgent(AgentCard agentCard, String namespaceId,
        String registrationType)
        throws NacosException {
        Map<String, String> params = new HashMap<>(4);
        params.put("agentCard", JacksonUtils.toJson(agentCard));
        params.put("namespaceId", namespaceId);
        params.put("agentName", agentCard.getName());
        params.put("registrationType", registrationType);
        HttpRequest request =
            buildHttpRequestBuilder(buildRequestResource(namespaceId, agentCard.getName()))
                .setHttpMethod(HttpMethod.POST).setParamValue(params)
                .setPath(Constants.AdminApiPath.AI_AGENT_ADMIN_PATH)
                .build();
        HttpRestResult<String> restResult = executeSyncHttpRequest(request);
        Result<String> result =
            JacksonUtils.toObj(restResult.getData(), new TypeReference<Result<String>>() {
            });
        return ErrorCode.SUCCESS.getCode().equals(result.getCode());
    }
    
    /** GET 查询指定版本的 Agent Card 详情。 */
    @Override
    public AgentCardDetailInfo getAgentCard(String agentName, String namespaceId,
        String registrationType,
        String version) throws NacosException {
        Map<String, String> params = new HashMap<>(4);
        params.put("agentName", agentName);
        params.put("namespaceId", namespaceId);
        params.put("registrationType", registrationType);
        params.put("version", version);
        HttpRequest request = buildHttpRequestBuilder(buildRequestResource(namespaceId, agentName))
            .setHttpMethod(HttpMethod.GET).setParamValue(params)
            .setPath(Constants.AdminApiPath.AI_AGENT_ADMIN_PATH)
            .build();
        HttpRestResult<String> restResult = executeSyncHttpRequest(request);
        Result<AgentCardDetailInfo> result = JacksonUtils.toObj(restResult.getData(),
            new TypeReference<Result<AgentCardDetailInfo>>() {
            });
        return result.getData();
    }
    
    /** 更新 Agent Card；遇 legacy 参数错误时自动转换并重试。 */
    @Override
    public boolean updateAgentCard(AgentCard agentCard, String namespaceId, boolean setAsLatest,
        String registrationType) throws NacosException {
        try {
            return doUpdateAgentCard(agentCard, namespaceId, setAsLatest, registrationType);
        } catch (NacosException e) {
            if (shouldRetryWithLegacyFormat(e)) {
                LOGGER.info("Retry update agent card {} with legacy fields for compatibility.",
                    agentCard.getName());
                return doUpdateAgentCard(buildLegacyCompatibleAgentCard(agentCard), namespaceId,
                    setAsLatest,
                    registrationType);
            }
            throw e;
        }
    }
    
    /** 执行 PUT 更新 Agent Card 请求。 */
    private boolean doUpdateAgentCard(AgentCard agentCard, String namespaceId, boolean setAsLatest,
        String registrationType) throws NacosException {
        Map<String, String> params = new HashMap<>(5);
        params.put("agentCard", JacksonUtils.toJson(agentCard));
        params.put("namespaceId", namespaceId);
        params.put("agentName", agentCard.getName());
        params.put("setAsLatest", String.valueOf(setAsLatest));
        params.put("registrationType", registrationType);
        HttpRequest request =
            buildHttpRequestBuilder(buildRequestResource(namespaceId, agentCard.getName()))
                .setHttpMethod(HttpMethod.PUT).setParamValue(params)
                .setPath(Constants.AdminApiPath.AI_AGENT_ADMIN_PATH)
                .build();
        HttpRestResult<String> restResult = executeSyncHttpRequest(request);
        Result<String> result =
            JacksonUtils.toObj(restResult.getData(), new TypeReference<Result<String>>() {
            });
        return ErrorCode.SUCCESS.getCode().equals(result.getCode());
    }
    
    /** DELETE 删除 Agent（可指定版本）。 */
    @Override
    public boolean deleteAgent(String agentName, String namespaceId, String version)
        throws NacosException {
        Map<String, String> params = new HashMap<>(4);
        params.put("agentName", agentName);
        params.put("namespaceId", namespaceId);
        params.put("version", version);
        HttpRequest request = buildHttpRequestBuilder(buildRequestResource(namespaceId, agentName))
            .setHttpMethod(HttpMethod.DELETE).setParamValue(params)
            .setPath(Constants.AdminApiPath.AI_AGENT_ADMIN_PATH).build();
        HttpRestResult<String> restResult = executeSyncHttpRequest(request);
        Result<String> result =
            JacksonUtils.toObj(restResult.getData(), new TypeReference<Result<String>>() {
            });
        return ErrorCode.SUCCESS.getCode().equals(result.getCode());
    }
    
    /** 列出 Agent 全部版本明细。 */
    @Override
    public List<AgentVersionDetail> listAllVersionOfAgent(String agentName, String namespaceId)
        throws NacosException {
        Map<String, String> params = new HashMap<>(2);
        params.put("agentName", agentName);
        params.put("namespaceId", namespaceId);
        HttpRequest request = buildHttpRequestBuilder(buildRequestResource(namespaceId, agentName))
            .setHttpMethod(HttpMethod.GET).setParamValue(params)
            .setPath(Constants.AdminApiPath.AI_AGENT_LIST_VERSION_ADMIN_PATH).build();
        HttpRestResult<String> restResult = executeSyncHttpRequest(request);
        Result<List<AgentVersionDetail>> result = JacksonUtils.toObj(restResult.getData(),
            new TypeReference<Result<List<AgentVersionDetail>>>() {
            });
        return result.getData();
    }
    
    @Override
    public Page<AgentCardVersionInfo> searchAgentCardsByName(String namespaceId,
        String agentNamePattern, int pageNo,
        int pageSize) throws NacosException {
        return listOrSearchAgentCardsByName(namespaceId, agentNamePattern, pageNo, pageSize, true);
    }
    
    @Override
    public Page<AgentCardVersionInfo> listAgentCards(String namespaceId, String agentName,
        int pageNo, int pageSize)
        throws NacosException {
        return listOrSearchAgentCardsByName(namespaceId, agentName, pageNo, pageSize, false);
    }
    
    /** 分页列出或模糊搜索 Agent Card 列表。 */
    private Page<AgentCardVersionInfo> listOrSearchAgentCardsByName(String namespaceId,
        String agentName, int pageNo,
        int pageSize, boolean isBlur) throws NacosException {
        Map<String, String> params = new HashMap<>(5);
        params.put("agentName", agentName);
        params.put("namespaceId", namespaceId);
        params.put("search", isBlur ? SEARCH_BLUR : SEARCH_ACCURATE);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("pageSize", String.valueOf(pageSize));
        HttpRequest request = buildHttpRequestBuilder(buildRequestResource(namespaceId, null))
            .setHttpMethod(HttpMethod.GET).setParamValue(params)
            .setPath(Constants.AdminApiPath.AI_AGENT_LIST_ADMIN_PATH).build();
        HttpRestResult<String> restResult = executeSyncHttpRequest(request);
        Result<Page<AgentCardVersionInfo>> result = JacksonUtils.toObj(restResult.getData(),
            new TypeReference<Result<Page<AgentCardVersionInfo>>>() {
            });
        return result.getData();
    }
    
    /** 判断 INVALID_PARAM 是否因旧版 AgentCard 字段缺失，需 legacy 重试。 */
    private boolean shouldRetryWithLegacyFormat(NacosException e) {
        if (e.getErrCode() != NacosException.INVALID_PARAM) {
            return false;
        }
        String errMsg = e.getErrMsg();
        if (StringUtils.isEmpty(errMsg)) {
            return false;
        }
        return errMsg.contains("agentCard.protocolVersion")
            || errMsg.contains("agentCard.preferredTransport")
            || errMsg.contains("agentCard.url");
    }
    
    /** 从 supportedInterfaces 回填 url/preferredTransport 等 legacy 字段。 */
    private AgentCard buildLegacyCompatibleAgentCard(AgentCard source) {
        AgentCard result = JacksonUtils.toObj(JacksonUtils.toJson(source), AgentCard.class);
        List<AgentInterface> supportedInterfaces = result.getSupportedInterfaces();
        if (null != supportedInterfaces && !supportedInterfaces.isEmpty()) {
            AgentInterface preferred = supportedInterfaces.get(0);
            result.setUrl(preferred.getUrl());
            result.setPreferredTransport(preferred.getProtocolBinding());
            result.setProtocolVersion(preferred.getProtocolVersion());
            if (supportedInterfaces.size() > 1) {
                result.setAdditionalInterfaces(
                    new ArrayList<>(supportedInterfaces.subList(1, supportedInterfaces.size())));
            }
        }
        return result;
    }
}
