/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.representations.idm;

import java.io.Serializable;
import java.util.List;

/**
 * 认证流执行步骤的元信息表示，供 Admin Console 展示可选执行器及其配置项。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AuthenticationExecutionInfoRepresentation implements Serializable {

    /** 执行步骤 ID。 */
    protected String id;
    /** 当前 requirement 值（REQUIRED、ALTERNATIVE、DISABLED 等）。 */
    protected String requirement;
    /** 管理界面显示名称。 */
    protected String displayName;
    /** 执行步骤别名。 */
    protected String alias;
    /** 执行步骤描述。 */
    protected String description;
    /** 可选的 requirement 枚举列表。 */
    protected List<String> requirementChoices;
    /** 是否可在 Admin Console 中配置。 */
    protected Boolean configurable;
    /** 该步骤是否代表一个嵌套认证流。 */
    protected Boolean authenticationFlow;
    /** 认证器提供方 ID。 */
    protected String providerId;
    /** 关联的认证器配置 ID。 */
    protected String authenticationConfig;
    /** 嵌套子流 ID。 */
    protected String flowId;
    /** 在流树中的层级深度。 */
    protected int level;
    /** 同级步骤中的序号。 */
    protected int index;
    /** 执行优先级。 */
    protected int priority;

    /** @return 执行步骤 ID */
    public String getId() {
        return id;
    }

    /** @param execution 执行步骤 ID */
    public void setId(String execution) {
        this.id = execution;
    }

    /** @return 显示名称 */
    public String getDisplayName() {
        return displayName;
    }

    /** @param displayName 显示名称 */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** @return 别名 */
    public String getAlias() {
        return alias;
    }

    /** @param alias 别名 */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /** @return 描述 */
    public String getDescription() {
        return description;
    }

    /** @param description 描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 当前 requirement */
    public String getRequirement() {
        return requirement;
    }

    /** @param requirement requirement 值 */
    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    /** @return 可选 requirement 列表 */
    public List<String> getRequirementChoices() {
        return requirementChoices;
    }

    /** @param requirementChoices 可选 requirement 列表 */
    public void setRequirementChoices(List<String> requirementChoices) {
        this.requirementChoices = requirementChoices;
    }

    /** @return 是否可配置 */
    public Boolean getConfigurable() {
        return configurable;
    }

    /** @param configurable 是否可配置 */
    public void setConfigurable(Boolean configurable) {
        this.configurable = configurable;
    }

    /** @return 认证器提供方 ID */
    public String getProviderId() {
        return providerId;
    }

    /** @param providerId 认证器提供方 ID */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @return 认证器配置 ID */
    public String getAuthenticationConfig() {
        return authenticationConfig;
    }

    /** @param authenticationConfig 认证器配置 ID */
    public void setAuthenticationConfig(String authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    /** @return 是否为嵌套认证流 */
    public Boolean getAuthenticationFlow() {
        return authenticationFlow;
    }

    /** @param authenticationFlow 是否为嵌套认证流 */
    public void setAuthenticationFlow(Boolean authenticationFlow) {
        this.authenticationFlow = authenticationFlow;
    }

    /** @return 流树层级 */
    public int getLevel() {
        return level;
    }

    /** @param level 流树层级 */
    public void setLevel(int level) {
        this.level = level;
    }

    /** @return 同级序号 */
    public int getIndex() {
        return index;
    }

    /** @param index 同级序号 */
    public void setIndex(int index) {
        this.index = index;
    }

    /** @return 嵌套子流 ID */
    public String getFlowId() {
        return flowId;
    }

    /** @param flowId 嵌套子流 ID */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    /** @return 执行优先级 */
    public int getPriority() {
        return priority;
    }

    /** @param priority 执行优先级 */
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
