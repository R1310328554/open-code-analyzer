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
 *
 */

package com.alibaba.nacos.api.ai.model.a2a;

import java.util.List;
import java.util.Objects;

/**
 * Agent Card 版本汇总信息，用于列举 Agent 各历史版本及最新发布版本。
 *
 * <p>继承 {@link AgentCardBasicInfo} 的基础字段，并附加版本明细列表与注册类型，
 * 便于客户端选择特定版本订阅或展示版本时间线。</p>
 *
 * @author KiteSoar
 */
public class AgentCardVersionInfo extends AgentCardBasicInfo {
    
    /** 最新已发布版本号。 */
    private String latestPublishedVersion;
    
    /** 各历史版本的明细列表。 */
    private List<AgentVersionDetail> versionDetails;
    
    /** Agent 注册类型（URL 或端点拆分等形式）。 */
    private String registrationType;
    
    public String getLatestPublishedVersion() {
        return latestPublishedVersion;
    }
    
    public void setLatestPublishedVersion(String latestPublishedVersion) {
        this.latestPublishedVersion = latestPublishedVersion;
    }
    
    public List<AgentVersionDetail> getVersionDetails() {
        return versionDetails;
    }
    
    public void setVersionDetails(List<AgentVersionDetail> versionDetails) {
        this.versionDetails = versionDetails;
    }
    
    public String getRegistrationType() {
        return registrationType;
    }
    
    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AgentCardVersionInfo that = (AgentCardVersionInfo) o;
        return Objects.equals(latestPublishedVersion, that.latestPublishedVersion)
            && Objects.equals(versionDetails,
                that.versionDetails)
            && Objects.equals(registrationType, that.registrationType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), latestPublishedVersion, versionDetails,
            registrationType);
    }
}
