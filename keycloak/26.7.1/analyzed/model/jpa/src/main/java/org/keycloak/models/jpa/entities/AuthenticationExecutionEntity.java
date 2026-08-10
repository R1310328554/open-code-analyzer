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

package org.keycloak.models.jpa.entities;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import org.keycloak.models.AuthenticationExecutionModel;

/**
 * 认证执行步骤 JPA 实体，映射 AUTHENTICATION_EXECUTION 表。
 * <p>
 * 表示认证流中的一个执行节点：可为独立 authenticator 或嵌套子流引用。
 * {@link #priority} 决定同层执行顺序；{@link #requirement} 控制 REQUIRED/ALTERNATIVE 等语义。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name = "authenticationFlowExecution", query = "select authExec from AuthenticationExecutionEntity authExec where authExec.flowId = :flowId")
})
@Table(name="AUTHENTICATION_EXECUTION")
@Entity
public class AuthenticationExecutionEntity {
    /** 执行步骤 UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;

    /** 所属 realm。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REALM_ID")
    protected RealmEntity realm;

    /** 父认证流（executions 集合的 owning side）。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FLOW_ID")
    protected AuthenticationFlowEntity parentFlow;

    /** Authenticator SPI 实现 ID（非子流时有效）。 */
    @Column(name="AUTHENTICATOR")
    protected String authenticator;

    /** 关联的 AuthenticatorConfig 实体 ID。 */
    @Column(name="AUTH_CONFIG")
    protected String authenticatorConfig;

    /** 嵌套子流 ID（autheticatorFlow=true 时引用 AUTHENTICATION_FLOW）。 */
    @Column(name="AUTH_FLOW_ID")
    protected String flowId;

    /** 执行要求：REQUIRED / ALTERNATIVE / DISABLED / CONDITIONAL。 */
    @Column(name="REQUIREMENT")
    protected AuthenticationExecutionModel.Requirement requirement;

    /** 同层执行优先级，数值越小越先执行。 */
    @Column(name="PRIORITY")
    protected int priority;

    /** true 表示本节点引用子认证流而非单个 authenticator。 */
    @Column(name="AUTHENTICATOR_FLOW")
    private boolean autheticatorFlow;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RealmEntity getRealm() {
        return realm;
    }

    public void setRealm(RealmEntity realm) {
        this.realm = realm;
    }

    public String getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(String authenticator) {
        this.authenticator = authenticator;
    }

    public AuthenticationExecutionModel.Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(AuthenticationExecutionModel.Requirement requirement) {
        this.requirement = requirement;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isAutheticatorFlow() {
        return autheticatorFlow;
    }

    public void setAutheticatorFlow(boolean autheticatorFlow) {
        this.autheticatorFlow = autheticatorFlow;
    }

    public AuthenticationFlowEntity getParentFlow() {
        return parentFlow;
    }

    public void setParentFlow(AuthenticationFlowEntity flow) {
        this.parentFlow = flow;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getAuthenticatorConfig() {
        return authenticatorConfig;
    }

    public void setAuthenticatorConfig(String authenticatorConfig) {
        this.authenticatorConfig = authenticatorConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof AuthenticationExecutionEntity)) return false;

        AuthenticationExecutionEntity that = (AuthenticationExecutionEntity) o;

        if (!id.equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
