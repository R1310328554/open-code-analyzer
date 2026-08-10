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

package com.alibaba.nacos.api.ai.remote.request;

import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;

/**
 * 发布新 Agent Card 或为已有 Agent 发布新版本的远程请求。
 *
 * <p>携带完整 {@link com.alibaba.nacos.api.ai.model.a2a.AgentCard} 定义，
 * 可通过 {@link #setAsLatest} 标记为最新版本。</p>
 *
 * @author xiweng.yy
 */
public class ReleaseAgentCardRequest extends AbstractAgentRequest {
    
    /** 待发布的 Agent Card 完整定义。 */
    private AgentCard agentCard;
    
    /** 注册类型，默认为 {@link com.alibaba.nacos.api.ai.constant.AiConstants.A2a#A2A_ENDPOINT_TYPE_SERVICE}。 */
    private String registrationType = AiConstants.A2a.A2A_ENDPOINT_TYPE_SERVICE;
    
    /** 是否将本次发布版本标记为最新版本。 */
    private boolean setAsLatest;
    
    /** 获取待发布的 Agent Card。 */
    public AgentCard getAgentCard() {
        return agentCard;
    }
    
    /** 设置待发布的 Agent Card。 */
    public void setAgentCard(AgentCard agentCard) {
        this.agentCard = agentCard;
    }
    
    /** 获取注册类型。 */
    public String getRegistrationType() {
        return registrationType;
    }
    
    /** 设置注册类型。 */
    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }
    
    /** 获取是否标记为最新版本。 */
    public boolean isSetAsLatest() {
        return setAsLatest;
    }
    
    /** 设置是否标记为最新版本。 */
    public void setSetAsLatest(boolean setAsLatest) {
        this.setAsLatest = setAsLatest;
    }
}
