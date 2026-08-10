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
package org.keycloak.authorization.jpa.store;

import jakarta.persistence.EntityManager;

import org.keycloak.authorization.jpa.entities.ResourceServerEntity;
import org.keycloak.authorization.model.AbstractAuthorizationModel;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.jpa.JpaModel;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.PolicyEnforcementMode;

/**
 * {@link ResourceServerEntity} 的 JPA 适配器，对应 OAuth 客户端上的授权资源服务器配置。
 * <p>
 * 管理远程资源管理开关、策略执行模式与决策策略等客户端级授权设置。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResourceServerAdapter extends AbstractAuthorizationModel implements ResourceServer, JpaModel<ResourceServerEntity> {
    /** 底层 JPA 资源服务器实体。 */
    private ResourceServerEntity entity;
    /** 当前会话的 EntityManager。 */
    private EntityManager em;
    /** 授权 Store 工厂。 */
    private StoreFactory storeFactory;

    /** 用已有实体构造资源服务器适配器。 */
    public ResourceServerAdapter(ResourceServerEntity entity, EntityManager em, StoreFactory storeFactory) {
        super(storeFactory);
        this.entity = entity;
        this.em = em;
        this.storeFactory = storeFactory;
    }

    @Override
    public ResourceServerEntity getEntity() {
        return entity;
    }

    @Override
    public String getId() {
        return entity.getId();
    }

    @Override
    public boolean isAllowRemoteResourceManagement() {
        return entity.isAllowRemoteResourceManagement();
    }

    @Override
    public void setAllowRemoteResourceManagement(boolean allowRemoteResourceManagement) {
        throwExceptionIfReadonly();
        entity.setAllowRemoteResourceManagement(allowRemoteResourceManagement);

    }

    @Override
    public PolicyEnforcementMode getPolicyEnforcementMode() {
        return entity.getPolicyEnforcementMode();
    }

    @Override
    public void setPolicyEnforcementMode(PolicyEnforcementMode enforcementMode) {
        throwExceptionIfReadonly();
        entity.setPolicyEnforcementMode(enforcementMode);

    }

    @Override
    public DecisionStrategy getDecisionStrategy() {
        return entity.getDecisionStrategy();
    }

    @Override
    public void setDecisionStrategy(DecisionStrategy decisionStrategy) {
        throwExceptionIfReadonly();
        entity.setDecisionStrategy(decisionStrategy);
    }

    @Override
    public String getClientId() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ResourceServer)) return false;

        ResourceServer that = (ResourceServer) o;
        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /** 将 {@link ResourceServer} 转为 {@link ResourceServerEntity}。 */
    public static ResourceServerEntity toEntity(EntityManager em, ResourceServer resource) {
        if (resource instanceof ResourceServerAdapter) {
            return ((ResourceServerAdapter)resource).getEntity();
        } else {
            return em.getReference(ResourceServerEntity.class, resource.getId());
        }
    }


}
