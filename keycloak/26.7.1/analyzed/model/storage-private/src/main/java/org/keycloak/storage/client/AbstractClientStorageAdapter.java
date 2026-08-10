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
package org.keycloak.storage.client;

import java.util.Collections;
import java.util.Map;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;

/**
 * 客户端存储适配器抽象基类：为 {@link ClientStorageProvider} 的 {@link ClientModel} 实现提供通用方法。
 * <p>
 * 包含部分方法的默认实现，如联邦 ID 生成、登出节点注册占位等。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractClientStorageAdapter extends UnsupportedOperationsClientStorageAdapter {
    protected KeycloakSession session;
    protected RealmModel realm;
    protected ClientStorageProviderModel component;
    private StorageId storageId;


    /** 构造客户端存储适配器并绑定会话、领域与组件配置。 */
    public AbstractClientStorageAdapter(KeycloakSession session, RealmModel realm, ClientStorageProviderModel component) {
        this.session = session;
        this.realm = realm;
        this.component = component;
    }

    /**
     * 基于 {@link #getClientId()} 生成联邦存储 ID。
     *
     * @return 联邦客户端 ID
     */
    @Override
    public String getId() {
        if (storageId == null) {
            storageId = new StorageId(component.getId(), getClientId());
        }
        return storageId.getId();
    }

    @Override
    public final RealmModel getRealm() {
        return realm;
    }


    /**
     * 遗留特性，当前无调用方，始终返回 false。
     *
     * @return 是否需要代理认证
     */
    @Override
    public boolean isSurrogateAuthRequired() {
        return false;
    }

    /**
     * 遗留特性，当前无调用方，空实现。
     */
    @Override
    public void setSurrogateAuthRequired(boolean surrogateAuthRequired) {
        // 无操作，不支持此特性
    }

    /**
     * 用于登出流程；当前为空实现，子类可在可持久化处覆盖。
     *
     * @return 已注册节点映射
     */
    @Override
    public Map<String, Integer> getRegisteredNodes() {
        return Collections.EMPTY_MAP;
    }

    /**
     * 用于登出流程；当前为空实现，子类可在可持久化处覆盖。
     */
    @Override
    public void registerNode(String nodeHost, int registrationTime) {
        // 无操作
    }

    /**
     * 用于登出流程；当前为空实现，子类可在可持久化处覆盖。
     */
    @Override
    public void unregisterNode(String nodeHost) {
        // 无操作
    }

    /**
     * 子类覆盖时应调用 super.updateClient()，以触发客户端更新事件。
     */
    @Override
    public void updateClient() {
        session.getKeycloakSessionFactory().publish(new ClientModel.ClientUpdatedEvent() {

            @Override
            public ClientModel getUpdatedClient() {
                return AbstractClientStorageAdapter.this;
            }

            @Override
            public KeycloakSession getKeycloakSession() {
                return session;
            }
        });

    }


}
