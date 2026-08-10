/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.keycloak.authorization.jpa.entities.PolicyEntity;
import org.keycloak.authorization.jpa.entities.ResourceEntity;
import org.keycloak.authorization.jpa.entities.ScopeEntity;
import org.keycloak.authorization.model.AbstractAuthorizationModel;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.jpa.JpaModel;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Logic;

/**
 * {@link PolicyEntity} 的 JPA 适配器，将授权策略领域模型映射到持久化实体。
 * <p>
 * 读写操作委托给底层 {@link PolicyEntity}；修改前通过 {@link AbstractAuthorizationModel#throwExceptionIfReadonly()}
 * 校验只读模式。关联策略、资源与作用域通过 Store 懒加载并包装为对应 Adapter。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PolicyAdapter extends AbstractAuthorizationModel implements Policy, JpaModel<PolicyEntity> {
    /** 底层 JPA 策略实体。 */
    private PolicyEntity entity;
    /** 当前会话的 EntityManager。 */
    private EntityManager em;
    /** 授权 Store 工厂，用于解析关联对象。 */
    private StoreFactory storeFactory;

    /** 用已有实体构造策略适配器。 */
    public PolicyAdapter(PolicyEntity entity, EntityManager em, StoreFactory storeFactory) {
        super(storeFactory);
        this.entity = entity;
        this.em = em;
        this.storeFactory = storeFactory;
    }

    @Override
    public PolicyEntity getEntity() {
        return entity;
    }

    @Override
    public String getId() {
        return entity.getId();
    }

    @Override
    public String getType() {
        return entity.getType();
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
    public Logic getLogic() {
        return entity.getLogic();
    }

    @Override
    public void setLogic(Logic logic) {
        throwExceptionIfReadonly();
        entity.setLogic(logic);
    }

    /** 返回策略配置的不可变副本，避免调用方直接修改实体内部 Map。 */
    @Override
    public Map<String, String> getConfig() {
        Map<String, String> result = new HashMap<String, String>();
        if (entity.getConfig() != null) result.putAll(entity.getConfig());
        return Collections.unmodifiableMap(result);
    }

    @Override
    public void setConfig(Map<String, String> config) {
        throwExceptionIfReadonly();
        if (entity.getConfig() == null) {
            entity.setConfig(new HashMap<>());
        } else {
            entity.getConfig().clear();
        }
        entity.getConfig().putAll(config);
    }

    @Override
    public void removeConfig(String name) {
        throwExceptionIfReadonly();
        if (entity.getConfig() == null) {
            return;
        }
        entity.getConfig().remove(name);
    }

    @Override
    public void putConfig(String name, String value) {
        throwExceptionIfReadonly();
        if (entity.getConfig() == null) {
            entity.setConfig(new HashMap<>());
        }
        entity.getConfig().put(name, value);

    }

    @Override
    public String getName() {
        return entity.getName();
    }

    @Override
    public void setName(String name) {
        throwExceptionIfReadonly();
        entity.setName(name);

    }

    @Override
    public String getDescription() {
        return entity.getDescription();
    }

    @Override
    public void setDescription(String description) {
        throwExceptionIfReadonly();
        entity.setDescription(description);

    }

    @Override
    public ResourceServer getResourceServer() {
        return storeFactory.getResourceServerStore().findById(entity.getResourceServer().getId());
    }

    @Override
    public Set<Policy> getAssociatedPolicies() {
        Set<Policy> result = new HashSet<>();
        for (PolicyEntity policy : entity.getAssociatedPolicies()) {
            result.add(new PolicyAdapter(policy, em, storeFactory));
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Set<Resource> getResources() {
        Set<Resource> set = new HashSet<>();
        ResourceServer resourceServer = getResourceServer();
        for (ResourceEntity res : entity.getResources()) {
            set.add(storeFactory.getResourceStore().findById(resourceServer, res.getId()));
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Set<Scope> getScopes() {
        Set<Scope> set = new HashSet<>();
        ResourceServer resourceServer = getResourceServer();
        for (ScopeEntity res : entity.getScopes()) {
            set.add(storeFactory.getScopeStore().findById(resourceServer, res.getId()));
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public void addScope(Scope scope) {
        throwExceptionIfReadonly();
        entity.getScopes().add(ScopeAdapter.toEntity(em, scope));
    }

    @Override
    public void removeScope(Scope scope) {
        throwExceptionIfReadonly();
        entity.getScopes().remove(ScopeAdapter.toEntity(em, scope));

    }

    @Override
    public void addAssociatedPolicy(Policy associatedPolicy) {
        throwExceptionIfReadonly();
        entity.getAssociatedPolicies().add(toEntity(em, associatedPolicy));
    }

    @Override
    public void removeAssociatedPolicy(Policy associatedPolicy) {
        throwExceptionIfReadonly();
        entity.getAssociatedPolicies().remove(toEntity(em, associatedPolicy));

    }

    @Override
    public void addResource(Resource resource) {
        throwExceptionIfReadonly();
        entity.getResources().add(ResourceAdapter.toEntity(em, resource));
    }

    @Override
    public void removeResource(Resource resource) {
        throwExceptionIfReadonly();
        entity.getResources().remove(ResourceAdapter.toEntity(em, resource));
    }

    @Override
    public void setOwner(String owner) {
        throwExceptionIfReadonly();
        entity.setOwner(owner);
    }

    @Override
    public String getOwner() {
        return entity.getOwner();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Policy)) return false;

        Policy that = (Policy) o;
        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * 将 {@link Policy} 转为 {@link PolicyEntity}：已是本适配器则直接取实体，否则按 ID 获取懒加载引用。
     */
    public static PolicyEntity toEntity(EntityManager em, Policy policy) {
        if (policy instanceof PolicyAdapter) {
            return ((PolicyAdapter)policy).getEntity();
        } else {
            return em.getReference(PolicyEntity.class, policy.getId());
        }
    }
}
