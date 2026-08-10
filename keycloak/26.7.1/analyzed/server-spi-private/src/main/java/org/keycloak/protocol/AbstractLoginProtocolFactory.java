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

package org.keycloak.protocol;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;
import org.keycloak.representations.idm.ClientScopeRepresentation;

/**
 * 登录协议工厂的抽象基类：处理 realm/客户端默认 Client Scope 的创建与绑定。
 * <p>监听 {@link ClientModel.ClientProtocolUpdatedEvent}，在客户端协议变更时自动附加默认 scope。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractLoginProtocolFactory implements LoginProtocolFactory {

    @Override
    public void init(Config.Scope config) {
    }

    /** 注册客户端协议更新事件监听器，自动为新协议客户端添加默认 scope。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.register(new ProviderEventListener() {
            @Override
            public void onEvent(ProviderEvent event) {
                if (event instanceof ClientModel.ClientProtocolUpdatedEvent) {
                    ClientModel client = ((ClientModel.ClientProtocolUpdatedEvent)event).getClient();
                    addDefaultClientScopes(client.getRealm(), client);
                    addDefaults(client);
                }
            }
        });
    }


    /**
     * 为新 realm 创建本协议的默认 Client Scope，可选地同步到已有客户端。
     * @param newRealm 新创建的 realm
     * @param addScopesToExistingClients 是否将 scope 附加到 realm 内已有客户端
     */
    @Override
    public void createDefaultClientScopes(RealmModel newRealm, boolean addScopesToExistingClients) {
        createDefaultClientScopesImpl(newRealm);

        // 同时为 realm 内已有客户端附加默认 client scope
        if (addScopesToExistingClients) {
            addDefaultClientScopes(newRealm, newRealm.getClientsStream());
        }
    }

    /**
     * 子类实现：创建本协议的默认 Client Scope（通常在新建 realm 时调用）。
     */
    protected abstract void createDefaultClientScopesImpl(RealmModel newRealm);


    /** 为单个新客户端附加与本协议匹配的默认/可选 Client Scope。 */
    protected void addDefaultClientScopes(RealmModel realm, ClientModel newClient) {
        addDefaultClientScopes(realm, Stream.of(newClient));
    }

    /** 批量为客户端流附加与本协议匹配的默认与可选 Client Scope。 */
    protected void addDefaultClientScopes(RealmModel realm, Stream<ClientModel> newClients) {
        Set<ClientScopeModel> defaultClientScopes = realm.getDefaultClientScopesStream(true)
                .filter(clientScope -> Objects.equals(getId(), clientScope.getProtocol()))
                .collect(Collectors.toSet());

        Set<ClientScopeModel> nonDefaultClientScopes = realm.getDefaultClientScopesStream(false)
                .filter(clientScope -> Objects.equals(getId(), clientScope.getProtocol()))
                .collect(Collectors.toSet());

        Consumer<ClientModel> addDefault = c -> c.addClientScopes(defaultClientScopes, true);
        Consumer<ClientModel> addNonDefault = c -> c.addClientScopes(nonDefaultClientScopes, false);

        if (!defaultClientScopes.isEmpty() && !nonDefaultClientScopes.isEmpty())
            newClients.forEach(addDefault.andThen(addNonDefault));
        else if (!defaultClientScopes.isEmpty())
            newClients.forEach(addDefault);
        else if (!nonDefaultClientScopes.isEmpty())
            newClients.forEach(addNonDefault);
    }

    /** 子类实现：为客户端设置协议相关的默认配置。 */
    protected abstract void addDefaults(ClientModel clientModel);

    @Override
    public void addClientScopeDefaults(ClientScopeRepresentation clientModel) {
        // 默认无额外 scope 默认值
    }

    @Override
    public void close() {

    }
}
