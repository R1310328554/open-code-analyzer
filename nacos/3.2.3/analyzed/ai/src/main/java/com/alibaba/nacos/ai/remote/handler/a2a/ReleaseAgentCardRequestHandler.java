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

package com.alibaba.nacos.ai.remote.handler.a2a;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.ai.service.a2a.A2aServerOperationService;
import com.alibaba.nacos.ai.utils.AgentRequestUtil;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardDetailInfo;
import com.alibaba.nacos.api.ai.remote.request.ReleaseAgentCardRequest;
import com.alibaba.nacos.api.ai.remote.response.ReleaseAgentCardResponse;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.namespace.filter.NamespaceValidation;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.core.paramcheck.impl.AgentRequestParamExtractor;
import com.alibaba.nacos.core.remote.RequestHandler;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Nacos AI module release agent card request handler.
 * <p>客户端发布 AgentCard：Agent 不存在则注册，存在但版本缺失则更新；同版本已存在则记录日志（不抛冲突）。</p>
 *
 * @author xiweng.yy
 */
@Since("3.1.0")
@Component
public class ReleaseAgentCardRequestHandler
    extends RequestHandler<ReleaseAgentCardRequest, ReleaseAgentCardResponse> {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(ReleaseAgentCardRequestHandler.class);
    
    /** A2A Agent 注册与版本更新服务。 */
    private final A2aServerOperationService a2aServerOperationService;
    
    public ReleaseAgentCardRequestHandler(A2aServerOperationService a2aServerOperationService) {
        this.a2aServerOperationService = a2aServerOperationService;
    }
    
    @Override
    @NamespaceValidation
    @ExtractorManager.Extractor(rpcExtractor = AgentRequestParamExtractor.class)
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI)
    public ReleaseAgentCardResponse handle(ReleaseAgentCardRequest request, RequestMeta meta)
        throws NacosException {
        AgentRequestUtil.fillNamespaceId(request);
        ReleaseAgentCardResponse response = new ReleaseAgentCardResponse();
        try {
            validateRequest(request);
            doHandler(request, meta);
            return response;
        } catch (NacosException e) {
            response.setErrorInfo(e.getErrCode(), e.getErrMsg());
            LOGGER.error("[{}] Release agent card {} error: {}", meta.getConnectionId(),
                null == request.getAgentCard() ? null : JacksonUtils.toJson(request.getAgentCard()),
                e.getErrMsg());
        }
        return response;
    }
    
    /** 校验 agentCard 非空并通过 AgentRequestUtil 校验卡片字段。 */
    private void validateRequest(ReleaseAgentCardRequest request) throws NacosApiException {
        if (null == request.getAgentCard()) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "parameters `agentCard` can't be null");
        }
        AgentRequestUtil.validateAgentCard(request.getAgentCard());
    }
    
    private void doHandler(ReleaseAgentCardRequest request, RequestMeta meta)
        throws NacosException {
        String namespaceId = request.getNamespaceId();
        AgentCard agentCard = request.getAgentCard();
        LOGGER.info("Release new agent {}, version {} into namespaceId {} from connectionId {}.",
            agentCard.getName(),
            agentCard.getVersion(), namespaceId, meta.getConnectionId());
        try {
            AgentCardDetailInfo existAgentCard = a2aServerOperationService.getAgentCard(namespaceId,
                agentCard.getName(), agentCard.getVersion(), StringUtils.EMPTY);
            LOGGER.info("AgentCard {} and target version {} already exist.",
                existAgentCard.getName(),
                existAgentCard.getVersion());
        } catch (NacosApiException e) {
            if (ErrorCode.AGENT_NOT_FOUND.getCode() == e.getDetailErrCode()) {
                // Agent 不存在，注册新卡片
                createAgentCard(namespaceId, agentCard, request.getRegistrationType());
                LOGGER.info("AgentCard {} released.", agentCard.getName());
            } else if (ErrorCode.AGENT_VERSION_NOT_FOUND.getCode() == e.getDetailErrCode()) {
                // Agent 存在但版本不存在，发布新版本
                createNewVersionAgentCard(namespaceId, agentCard, request.getRegistrationType(),
                    request.isSetAsLatest());
                LOGGER.info("AgentCard {} new version {} released.", agentCard.getName(),
                    agentCard.getVersion());
            } else {
                LOGGER.error("AgentCard {} released failed.", agentCard.getName(), e);
                throw e;
            }
        }
    }
    
    /** 首次注册 AgentCard。 */
    private void createAgentCard(String namespaceId, AgentCard agentCard, String registrationType)
        throws NacosException {
        a2aServerOperationService.registerAgent(agentCard, namespaceId, registrationType);
    }
    
    /** 为已有 Agent 追加新版本，可选设为 latest。 */
    private void createNewVersionAgentCard(String namespaceId, AgentCard agentCard,
        String registrationType,
        boolean setAsLatest) throws NacosException {
        a2aServerOperationService.updateAgentCard(agentCard, namespaceId, registrationType,
            setAsLatest);
    }
}
