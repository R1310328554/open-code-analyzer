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

package org.keycloak.authorization.policy.provider.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.ResourceServerStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientModel.ClientRemovedEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.authorization.ClientPolicyRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.util.JsonSerialization;

/**
 * <p>客户端（client）策略类型的 {@link PolicyProviderFactory}。
 *
 * <p>管理策略与 realm 客户端 ID 的绑定，并在客户端删除时同步清理策略配置。
 */
public class ClientPolicyProviderFactory implements PolicyProviderFactory<ClientPolicyRepresentation> {

    private ClientPolicyProvider provider = new ClientPolicyProvider(this::toRepresentation);

    @Override
    /** 管理控制台显示名称。 */
    public String getName() {
        return "Client";
    }

    @Override
    /** 策略分组：Identity Based。 */
    public String getGroup() {
        return "Identity Based";
    }

    @Override
    /** 基于 {@link AuthorizationProvider} 创建单例 {@link ClientPolicyProvider}。 */
    public PolicyProvider create(AuthorizationProvider authorization) {
        return provider;
    }

    @Override
    /** 将策略配置中的客户端 ID 集合转为 {@link ClientPolicyRepresentation}。 */
    public ClientPolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        ClientPolicyRepresentation representation = new ClientPolicyRepresentation();
        representation.setClients(getClients(policy));
        return representation;
    }

    @Override
    /** 返回策略表示类型。 */
    public Class<ClientPolicyRepresentation> getRepresentationType() {
        return ClientPolicyRepresentation.class;
    }

    @Override
    /** 创建策略时解析客户端 ID 并写入配置。 */
    public void onCreate(Policy policy, ClientPolicyRepresentation representation, AuthorizationProvider authorization) {
        updateClients(policy, representation.getClients(), authorization);
    }

    @Override
    /** 更新策略时同步客户端 ID 列表。 */
    public void onUpdate(Policy policy, ClientPolicyRepresentation representation, AuthorizationProvider authorization) {
        updateClients(policy, representation.getClients(), authorization);
    }

    @Override
    /** 导入策略时从现有配置恢复客户端绑定。 */
    public void onImport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        updateClients(policy, getClients(policy), authorization);
    }

    @Override
    /** 导出时将内部客户端 ID 转为 clientId 字符串列表。 */
    public void onExport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        ClientPolicyRepresentation userRep = toRepresentation(policy, authorization);
        Map<String, String> config = new HashMap<>();

        try {
            RealmModel realm = authorization.getRealm();
            config.put("clients", JsonSerialization.writeValueAsString(userRep.getClients().stream().map(id -> realm.getClientById(id).getClientId()).collect(Collectors.toList())));
        } catch (IOException cause) {
            throw new RuntimeException("Failed to export user policy [" + policy.getName() + "]", cause);
        }

        representation.setConfig(config);
    }

    @Override
    /** 基于 {@link KeycloakSession} 创建单例 {@link ClientPolicyProvider}。 */
    public PolicyProvider create(KeycloakSession session) {
        return provider;
    }

    @Override
    /** SPI 初始化（无配置项）。 */
    public void init(Config.Scope config) {

    }

    @Override
    /** 注册客户端删除事件监听器，自动从策略中移除已删客户端。 */
    public void postInit(KeycloakSessionFactory factory) {
        factory.register(event -> {
            if (event instanceof ClientRemovedEvent) {
                KeycloakSession keycloakSession = ((ClientRemovedEvent) event).getKeycloakSession();
                AuthorizationProvider provider = keycloakSession.getProvider(AuthorizationProvider.class);
                StoreFactory storeFactory = provider.getStoreFactory();
                PolicyStore policyStore = storeFactory.getPolicyStore();
                ClientModel removedClient = ((ClientRemovedEvent) event).getClient();
                ResourceServerStore resourceServerStore = storeFactory.getResourceServerStore();
                ResourceServer resourceServer = resourceServerStore.findByClient(removedClient);

                if (resourceServer != null) {
                    policyStore.findByType(resourceServer, getId()).forEach(policy -> {
                        List<String> clients = new ArrayList<>();

                        for (String clientId : getClients(policy)) {
                            if (!clientId.equals(removedClient.getId())) {
                                clients.add(clientId);
                            }
                        }

                        try {
                            if (clients.isEmpty()) {
                                policyStore.delete(policy.getId());
                            } else {
                                policy.putConfig("clients", JsonSerialization.writeValueAsString(clients));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Error while synchronizing clients with policy [" + policy.getName() + "].", e);
                        }
                    });
                }
            }
        });
    }

    @Override
    /** 关闭工厂（无资源释放）。 */
    public void close() {

    }

    @Override
    /** 返回策略类型 ID {@code client}。 */
    public String getId() {
        return "client";
    }

    /** 将 clientId 或内部 ID 解析为 realm 客户端内部 ID 并序列化到策略配置。 */
    private void updateClients(Policy policy, Set<String> clients, AuthorizationProvider authorization) {
        RealmModel realm = authorization.getRealm();

        if (clients == null) {
            throw new RuntimeException("No client provided.");
        }

        Set<String> updatedClients = new HashSet<>();

        for (String id : clients) {
            ClientModel client = realm.getClientByClientId(id);

            if (client == null) {
                client = realm.getClientById(id);
            }

            if (client == null) {
                throw new RuntimeException("Error while updating policy [" + policy.getName()  + "]. Client [" + id + "] could not be found.");
            }

            updatedClients.add(client.getId());
        }

        try {
            policy.putConfig("clients", JsonSerialization.writeValueAsString(updatedClients));
        } catch (IOException cause) {
            throw new RuntimeException("Failed to serialize clients", cause);
        }
    }

    /** 从策略 config 的 {@code clients} JSON 字段读取客户端 ID 集合。 */
    private Set<String> getClients(Policy policy) {
        String clients = policy.getConfig().get("clients");

        if (clients != null) {
            try {
                return JsonSerialization.readValue(clients, Set.class);
            } catch (IOException e) {
                throw new RuntimeException("Could not parse clients [" + clients + "] from policy config [" + policy.getName() + "].", e);
            }
        }

        return Collections.emptySet();
    }
}
