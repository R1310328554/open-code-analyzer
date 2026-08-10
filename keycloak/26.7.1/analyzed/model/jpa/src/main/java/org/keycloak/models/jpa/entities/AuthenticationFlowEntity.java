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

import java.util.Collection;
import java.util.LinkedList;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.Nationalized;

/**
 * 认证流 JPA 实体，映射 AUTHENTICATION_FLOW 表。
 * <p>
 * 可嵌套：顶层流（browser、saml 等）或作为 execution 引用的子流。
 * {@link #executions} 按 priority 排序的执行步骤集合。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Table(name="AUTHENTICATION_FLOW")
@Entity
public class AuthenticationFlowEntity {
    /** 认证流 UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;

    /** 所属 realm。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REALM_ID")
    protected RealmEntity realm;

    /** Admin Console 中展示/引用的别名。 */
    @Column(name="ALIAS")
    protected String alias;

    /** 流类型 provider ID（如 basic-flow、client-flow）。 */
    @Column(name="PROVIDER_ID")
    protected String providerId;

    /** 流描述（支持 Unicode）。 */
    @Nationalized
    @Column(name="DESCRIPTION")
    protected String description;

    /** 是否为 realm 级顶层绑定流（非嵌套子流）。 */
    @Column(name="TOP_LEVEL")
    protected boolean topLevel;

    /** 内置流不可删除，仅可调整 execution。 */
    @Column(name="BUILT_IN")
    protected boolean builtIn;


    /** 本流包含的 execution 步骤；删除流时级联移除。 */
    @OneToMany(fetch = FetchType.LAZY, cascade ={CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "parentFlow")
    Collection<AuthenticationExecutionEntity> executions = new LinkedList<>();
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<AuthenticationExecutionEntity> getExecutions() {
        if (executions == null) {
            executions  = new LinkedList<>();
        }
        return executions;
    }

    public void setExecutions(Collection<AuthenticationExecutionEntity> executions) {
        this.executions = executions;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof AuthenticationFlowEntity)) return false;

        AuthenticationFlowEntity that = (AuthenticationFlowEntity) o;

        if (!id.equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
