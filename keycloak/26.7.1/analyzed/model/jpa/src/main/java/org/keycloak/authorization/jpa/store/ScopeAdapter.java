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

import jakarta.persistence.EntityManager;

import org.keycloak.authorization.jpa.entities.ScopeEntity;
import org.keycloak.authorization.model.AbstractAuthorizationModel;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.jpa.JpaModel;

/**
 * {@link ScopeEntity} 的 JPA 适配器，表示资源上的授权作用域（如 read、write）。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ScopeAdapter extends AbstractAuthorizationModel implements Scope, JpaModel<ScopeEntity> {
    /** 底层 JPA 作用域实体。 */
    private ScopeEntity entity;
    /** 当前会话的 EntityManager。 */
    private EntityManager em;
    /** 授权 Store 工厂。 */
    private StoreFactory storeFactory;

    /** 用已有实体构造作用域适配器。 */
    public ScopeAdapter(ScopeEntity entity, EntityManager em, StoreFactory storeFactory) {
        super(storeFactory);
        this.entity = entity;
        this.em = em;
        this.storeFactory = storeFactory;
    }

    @Override
    public ScopeEntity getEntity() {
        return entity;
    }

    @Override
    public String getId() {
        return entity.getId();
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
    public String getDisplayName() {
        return entity.getDisplayName();
    }

    @Override
    public void setDisplayName(String name) {
        throwExceptionIfReadonly();
        entity.setDisplayName(name);
    }

    @Override
    public String getIconUri() {
        return entity.getIconUri();
    }

    @Override
    public void setIconUri(String iconUri) {
        throwExceptionIfReadonly();
        entity.setIconUri(iconUri);

    }

    @Override
    public ResourceServer getResourceServer() {
        return storeFactory.getResourceServerStore().findById(entity.getResourceServer().getId());
    }

    /** 将 {@link Scope} 转为 {@link ScopeEntity}。 */
    public static ScopeEntity toEntity(EntityManager em, Scope scope) {
        if (scope instanceof ScopeAdapter) {
            return ((ScopeAdapter)scope).getEntity();
        } else {
            return em.getReference(ScopeEntity.class, scope.getId());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Scope)) return false;

        Scope that = (Scope) o;
        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

}
