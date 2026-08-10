/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.InvalidationHandler.ObjectType;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderFactory;

/**
 * 两栖提供者工厂：同时充当独立 {@link ProviderFactory} 与 {@link ComponentFactory} 的基类。
 * <p>独立部署时行为与普通工厂一致；作为组件时按组件配置创建专用工厂实例，流程如下：</p>
 * <ul>
 * <li>组件工厂通过 {@link #init} 初始化，配置取自组件模型并转为 {@link Scope}，组件配置优先于工厂级配置。</li>
 * <li>实例创建统一走 {@link #create(KeycloakSession)}，每个组件对应独立工厂。</li>
 * <li>组件工厂在提供者工厂内缓存，类似会话工厂对提供者工厂的缓存机制。</li>
 * </ul>
 *
 * @see ComponentFactoryProviderFactory
 *
 * @author hmlnarik
 */
public interface AmphibianProviderFactory<ProviderType extends Provider> extends ProviderFactory<ProviderType>, ComponentFactory<ProviderType, ProviderType> {

    @Override
    ProviderType create(KeycloakSession session);

    @Override
    @Deprecated
    default ProviderType create(KeycloakSession session, ComponentModel model) {
        throw new UnsupportedOperationException("Use create(KeycloakSession) instead");
    }

    @Override
    default List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    default void onUpdate(KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
        String oldId = oldModel == null ? null : oldModel.getId();
        String newId = newModel == null ? null : newModel.getId();
        if (oldId != null) {
            if (newId == null || Objects.equals(oldId, newId)) {
                session.invalidate(ObjectType.COMPONENT, oldId);
            } else {
                session.invalidate(ObjectType.COMPONENT, oldId, newId);
            }
        } else if (newId != null) {
            session.invalidate(ObjectType.COMPONENT, newId);
        }
    }

    @Override
    default void preRemove(KeycloakSession session, RealmModel realm, ComponentModel model) {
        if (model != null && model.getId() != null) {
            session.invalidate(ObjectType.COMPONENT, model.getId());
        }
    }

    @Override
    default void close() {
    }
}
