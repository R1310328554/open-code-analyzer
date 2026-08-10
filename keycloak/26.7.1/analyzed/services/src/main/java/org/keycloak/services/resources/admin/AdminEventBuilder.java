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
package org.keycloak.services.resources.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Time;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

import static org.keycloak.models.utils.StripSecretsUtils.stripSecrets;

/**
 * 管理事件（Admin Event）构建器。
 * <p>收集操作类型、资源路径、认证详情与表示体，写入 {@link EventStoreProvider} 并通知已注册监听器。</p>
 */
public class AdminEventBuilder {

    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(AdminEventBuilder.class);
    /** 管理 API 认证上下文 */
    protected final AdminAuth auth;
    /** 操作来源 IP 地址 */
    protected final String ipAddress;
    /** 事件所属领域 */
    protected final RealmModel realm;
    /** 正在构建的管理事件对象 */
    protected final AdminEvent adminEvent;
    /** 已注册的事件监听器映射（工厂 ID → 实例） */
    protected final Map<String, EventListenerProvider> listeners;
    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 事件存储提供者（领域启用 admin events 时） */
    protected EventStoreProvider store;

    /** 使用客户端连接远程地址构造管理事件构建器。 */
    public AdminEventBuilder(RealmModel realm, AdminAuth auth, KeycloakSession session, ClientConnection clientConnection) {
        this(realm, auth, session, clientConnection.getRemoteHost(), null);
    }

    /** 内部构造器，可复用已有 {@link AdminEvent} 快照。 */
    protected AdminEventBuilder(RealmModel realm, AdminAuth auth, KeycloakSession session, String ipAddress, AdminEvent adminEvent) {
        this.realm = realm;
        this.listeners = new HashMap<>();
        updateStore(session);
        addListeners(session);
        this.auth = auth;
        this.ipAddress = ipAddress;
        if (adminEvent != null) {
            this.adminEvent = new AdminEvent(adminEvent);
        } else {
            this.adminEvent = new AdminEvent();
            // 以下方法仅写入 adminEvent 字段，不触发发送
            realm(realm);
            authRealm(auth.getRealm());
            authClient(auth.getClient());
            authUser(auth.getUser());
            authIpAddress(ipAddress);
        }
        this.session = session;
    }

    /**
     * 绑定到新会话的 {@link AdminEventBuilder} 副本（嵌套事务等场景）。
     * @param session 新 Keycloak 会话
     * @return 新的构建器实例
     */
    public AdminEventBuilder clone(KeycloakSession session) {
        RealmModel newEventRealm = session.realms().getRealm(realm.getId());
        RealmModel newAuthRealm = session.realms().getRealm(this.auth.getRealm().getId());
        UserModel newAuthUser = session.users().getUserById(newAuthRealm, this.auth.getUser().getId());
        ClientModel newAuthClient = session.clients().getClientById(newAuthRealm, this.auth.getClient().getId());

        return new AdminEventBuilder(
                newEventRealm,
                new AdminAuth(newAuthRealm, this.auth.getToken(), newAuthUser, newAuthClient),
                session,
                ipAddress,
                adminEvent
        );
    }

    /** 设置事件关联的领域。 */
    public AdminEventBuilder realm(RealmModel realm) {
        adminEvent.setRealmId(realm.getId());
        adminEvent.setRealmName(realm.getName());
        return this;
    }

    /**
     * 领域事件配置变更后刷新存储与监听器（如 updateRealmEventsConfig 之后）。
     * @param session Keycloak 会话
     * @return 当前构建器
     */
    public AdminEventBuilder refreshRealmEventsConfig(KeycloakSession session) {
        return this.updateStore(session).addListeners(session);
    }

    /** 若领域启用 admin events 则懒加载 {@link EventStoreProvider}。 */
    protected AdminEventBuilder updateStore(KeycloakSession session) {
        if (realm.isAdminEventsEnabled() && store == null) {
            this.store = session.getProvider(EventStoreProvider.class);
            if (store == null) {
                ServicesLogger.LOGGER.noEventStoreProvider();
            }
        }
        return this;
    }

    /** 按领域配置注册全局与领域级事件监听器。 */
    protected AdminEventBuilder addListeners(KeycloakSession session) {
        HashSet<String> realmListeners = new HashSet<>(realm.getEventsListenersStream().toList());
        session.getKeycloakSessionFactory().getProviderFactoriesStream(EventListenerProvider.class)
                .filter(providerFactory -> realmListeners.contains(providerFactory.getId()) || ((EventListenerProviderFactory) providerFactory).isGlobal())
                .forEach(providerFactory -> {
                    realmListeners.remove(providerFactory.getId());
                    if (!listeners.containsKey(providerFactory.getId())) {
                        listeners.put(providerFactory.getId(), ((EventListenerProviderFactory) providerFactory).create(session));
                    }
                });
        realmListeners.forEach(ServicesLogger.LOGGER::providerNotFound);
        return this;
    }

    /** 设置操作类型（CREATE/UPDATE/DELETE 等）。 */
    public AdminEventBuilder operation(OperationType operationType) {
        adminEvent.setOperationType(operationType);
        return this;
    }

    /** 设置标准资源类型。 */
    public AdminEventBuilder resource(ResourceType resourceType){
        adminEvent.setResourceType(resourceType);
        return this;
    }

    /**
     * 设置自定义资源类型字符串（非 {@link ResourceType} 枚举值）。
     */
    /** {@inheritDoc} */
    public AdminEventBuilder resource(String resourceType){
        adminEvent.setResourceTypeAsString(resourceType);
        return this;
    }

    /** 设置认证详情中的认证领域。 */
    public AdminEventBuilder authRealm(RealmModel realm) {
        AuthDetails authDetails = adminEvent.getAuthDetails();
        if(authDetails == null) {
            authDetails =  new AuthDetails();
            authDetails.setRealmId(realm.getId());
        } else {
            authDetails.setRealmId(realm.getId());
        }
        authDetails.setRealmName(realm.getName());
        adminEvent.setAuthDetails(authDetails);
        return this;
    }

    /** 设置认证详情中的客户端 ID。 */
    public AdminEventBuilder authClient(ClientModel client) {
        AuthDetails authDetails = adminEvent.getAuthDetails();
        if(authDetails == null) {
            authDetails =  new AuthDetails();
            authDetails.setClientId(client.getId());
        } else {
            authDetails.setClientId(client.getId());
        }
        adminEvent.setAuthDetails(authDetails);
        return this;
    }

    /** 设置认证详情中的用户 ID。 */
    public AdminEventBuilder authUser(UserModel user) {
        AuthDetails authDetails = adminEvent.getAuthDetails();
        if(authDetails == null) {
            authDetails =  new AuthDetails();
            authDetails.setUserId(user.getId());
        } else {
            authDetails.setUserId(user.getId());
        }
        adminEvent.setAuthDetails(authDetails);
        return this;
    }

    /** 设置认证详情中的 IP 地址。 */
    public AdminEventBuilder authIpAddress(String ipAddress) {
        AuthDetails authDetails = adminEvent.getAuthDetails();
        if(authDetails == null) {
            authDetails =  new AuthDetails();
            authDetails.setIpAddress(ipAddress);
        } else {
            authDetails.setIpAddress(ipAddress);
        }
        adminEvent.setAuthDetails(authDetails);
        return this;
    }

    /** 拼接路径元素设置资源路径。 */
    public AdminEventBuilder resourcePath(String... pathElements) {
        StringBuilder sb = new StringBuilder();
        for (String element : pathElements) {
            sb.append("/");
            sb.append(element);
        }
        if (pathElements.length > 0) sb.deleteCharAt(0); // remove leading '/'

        adminEvent.setResourcePath(sb.toString());
        return this;
    }

    /** 从 {@link UriInfo} 提取领域相对资源路径。 */
    public AdminEventBuilder resourcePath(UriInfo uriInfo) {
        String path = getResourcePath(uriInfo);
        adminEvent.setResourcePath(path);
        return this;
    }

    /** 设置资源路径并在末尾附加资源 ID。 */
    public AdminEventBuilder resourcePath(UriInfo uriInfo, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append(getResourcePath(uriInfo));
        if (!sb.toString().endsWith("/")) {
            sb.append("/");
        }
        sb.append(id);
        adminEvent.setResourcePath(sb.toString());
        return this;
    }

    /** 计算相对于 {@code /realms/{realm}/} 的资源路径。 */
    protected String getResourcePath(UriInfo uriInfo) {
        String path = uriInfo.getPath();

        StringBuilder sb = new StringBuilder();
        sb.append("/realms/");
        sb.append(realm.getName());
        sb.append("/");
        String realmRelative = sb.toString();

        return path.substring(path.indexOf(realmRelative) + realmRelative.length());
    }

    /** 序列化操作表示体（自动剥离密钥字段）。 */
    public AdminEventBuilder representation(Object value) {
        if (value == null || value.equals("")) {
            return this;
        }

        stripSecretsFromRepresentation(value);

        try {
            adminEvent.setRepresentation(JsonSerialization.writeValueAsString(value));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /** 从表示体中移除敏感密钥信息。 */
    protected void stripSecretsFromRepresentation(Object value){
        stripSecrets(session, value);
    }

    /** 添加事件详情键值对（忽略空白值）。 */
    public AdminEventBuilder detail(String key, String value) {
        if (StringUtil.isBlank(value)) {
            return this;
        }

        if (adminEvent.getDetails() == null) {
            adminEvent.setDetails(new HashMap<>());
        }

        adminEvent.getDetails().put(key, value);

        return this;
    }

    /** @return 当前构建的管理事件 */
    public AdminEvent getEvent() {
        return adminEvent;
    }

    /** 标记操作成功并发送事件。 */
    public void success() {
        send();
    }

    /** 复制事件、写入时间戳与 ID，分发至存储与监听器。 */
    protected void send() {
        boolean includeRepresentation = realm.isAdminEventsDetailsEnabled();

        // 复制事件对象，同一构建器可复用于后续操作
        AdminEvent eventCopy = new AdminEvent(adminEvent);
        eventCopy.setTime(Time.currentTimeMillis());
        eventCopy.setId(UUID.randomUUID().toString());

        if (store != null) {
            store.onEvent(eventCopy, includeRepresentation);
        }

        if (listeners != null) {
            for (EventListenerProvider l : listeners.values()) {
                try {
                    l.onEvent(eventCopy, includeRepresentation);
                } catch (Throwable t) {
                    ServicesLogger.LOGGER.failedToSendType(t, l);
                }
            }
        }
    }

}
