/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.PolicyEnforcementMode;

/**
 * 资源服务器 JPA 实体，映射 RESOURCE_SERVER 表。
 * <p>与 OAuth 客户端一一对应，存储策略执行模式与决策策略等授权配置。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@Entity
@Table(name = "RESOURCE_SERVER")
public class ResourceServerEntity {

    /** 资源服务器 ID，通常等于客户端 ID。 */
    @Id
    @Column(name="ID", length = 36)
    private String id;

    /** 是否允许远程资源管理（UMA）。 */
    @Column(name = "ALLOW_RS_REMOTE_MGMT")
    private boolean allowRemoteResourceManagement;

    /** 策略执行模式：强制、宽松或禁用。 */
    @Column(name = "POLICY_ENFORCE_MODE")
    private PolicyEnforcementMode policyEnforcementMode = PolicyEnforcementMode.ENFORCING;

    /** 顶层决策策略，默认一致通过。 */
    @Column(name = "DECISION_STRATEGY")
    private DecisionStrategy decisionStrategy = DecisionStrategy.UNANIMOUS;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAllowRemoteResourceManagement() {
        return this.allowRemoteResourceManagement;
    }

    public void setAllowRemoteResourceManagement(boolean allowRemoteResourceManagement) {
        this.allowRemoteResourceManagement = allowRemoteResourceManagement;
    }

    public PolicyEnforcementMode getPolicyEnforcementMode() {
        return this.policyEnforcementMode;
    }

    public void setPolicyEnforcementMode(PolicyEnforcementMode policyEnforcementMode) {
        this.policyEnforcementMode = policyEnforcementMode;
    }

    /** 设置决策策略；不支持 CONSENSUS。 */
    public void setDecisionStrategy(DecisionStrategy decisionStrategy) {
        if (DecisionStrategy.CONSENSUS.equals(decisionStrategy)) {
            throw new IllegalArgumentException("Strategy " + decisionStrategy + " not supported");
        }
        this.decisionStrategy = decisionStrategy;
    }

    public DecisionStrategy getDecisionStrategy() {
        return decisionStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceServerEntity that = (ResourceServerEntity) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
