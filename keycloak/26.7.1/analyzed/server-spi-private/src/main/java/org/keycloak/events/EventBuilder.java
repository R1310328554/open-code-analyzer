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

package org.keycloak.events;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.tracing.TracingAttributes;
import org.keycloak.tracing.TracingProvider;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.StatusCode;
import org.jboss.logging.Logger;

/**
 * 用户事件构建器：组装 {@link Event} 并分发至事件存储与监听器。
 * <p>成功事件默认在事务提交时持久化；失败事件（{@link #error(String)}）通常立即写入。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class EventBuilder {

    private static final Logger log = Logger.getLogger(EventBuilder.class);

    private final KeycloakSession session;
    private EventStoreProvider store;
    private List<EventListenerProvider> listeners;
    private RealmModel realm;
    private Event event;
    private Boolean storeImmediately;
    private final boolean isEventsEnabled;

    /** 从客户端连接自动填充 IP 地址。 */
    public EventBuilder(RealmModel realm, KeycloakSession session, ClientConnection clientConnection) {
        this(realm, session);
        ipAddress(clientConnection.getRemoteHost());
    }

    /** 为指定领域创建事件构建器，并按领域配置解析存储与监听器。 */
    public EventBuilder(RealmModel realm, KeycloakSession session) {
        this.session = session;
        this.realm = realm;
        this.isEventsEnabled = realm.isEventsEnabled();

        event = new Event();

        this.store = this.isEventsEnabled ? getEventStoreProvider(session) : null;
        this.listeners = getEventListeners(session, realm);

        realm(realm);
    }

    private static EventStoreProvider getEventStoreProvider(KeycloakSession session) {
        EventStoreProvider store = session.getProvider(EventStoreProvider.class);
        if (store == null) {
            log.error("Events enabled, but no event store provider configured");
        }

        return store;
    }

    private static List<EventListenerProvider> getEventListeners(KeycloakSession session, RealmModel realm) {
        HashSet<String> realmListeners = new HashSet<>(realm.getEventsListenersStream().toList());
        List<EventListenerProvider> result = session.getKeycloakSessionFactory().getProviderFactoriesStream(EventListenerProvider.class)
                .filter(providerFactory -> realmListeners.contains(providerFactory.getId()) || ((EventListenerProviderFactory) providerFactory).isGlobal())
                .map(providerFactory -> {
                    realmListeners.remove(providerFactory.getId());
                    return session.getProvider(EventListenerProvider.class, providerFactory.getId());
                })
                .toList();
        if (!realmListeners.isEmpty()) {
            log.error("Event listeners " + realmListeners + " registered, but provider not found");
        }
        return result;
    }

    private EventBuilder(KeycloakSession session, EventStoreProvider store, List<EventListenerProvider> listeners, RealmModel realm, Event event) {
        this.listeners = listeners;
        this.realm = realm;
        this.event = event;
        this.session = session;
        this.store = store;
        this.isEventsEnabled = realm.isEventsEnabled();
    }

    /** 设置事件所属领域。 */
    public EventBuilder realm(RealmModel realm) {
        event.setRealmId(realm == null ? null : realm.getId());
        event.setRealmName(realm == null ? null : realm.getName());
        return this;
    }

    /** 从客户端模型提取 clientId。 */
    public EventBuilder client(ClientModel client) {
        event.setClientId(client == null ? null : client.getClientId());
        return this;
    }

    /** 直接设置 clientId。 */
    public EventBuilder client(String clientId) {
        event.setClientId(clientId);
        return this;
    }

    /** 从用户模型提取 userId。 */
    public EventBuilder user(UserModel user) {
        event.setUserId(user == null ? null : user.getId());
        return this;
    }

    /** 直接设置 userId。 */
    public EventBuilder user(String userId) {
        event.setUserId(userId);
        return this;
    }

    /** 从用户会话提取 sessionId。 */
    public EventBuilder session(UserSessionModel session) {
        event.setSessionId(session == null ? null : session.getId());
        return this;
    }

    /** 直接设置 sessionId。 */
    public EventBuilder session(String sessionId) {
        event.setSessionId(sessionId);
        return this;
    }

    /** 设置客户端 IP 地址。 */
    public EventBuilder ipAddress(String ipAddress) {
        event.setIpAddress(ipAddress);
        return this;
    }

    /** 设置事件类型。 */
    public EventBuilder event(EventType e) {
        event.setType(e);
        return this;
    }

    /** 追加单条 detail；空值被忽略。 */
    public EventBuilder detail(String key, String value) {
        if (value == null || value.equals("")) {
            return this;
        }

        if (event.getDetails() == null) {
            event.setDetails(new HashMap<>());
        }
        event.getDetails().put(key, value);
        return this;
    }

    /**
     * 将集合中非 null 元素以 {@code ::} 连接后写入 detail。
     *
     * @param key of the detail
     * @param values, can be null
     * @return builder for chaining
     */
    public EventBuilder detail(String key, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return this;
        }
        return detail(key, values.stream().filter(Objects::nonNull).collect(Collectors.joining("::")));
    }

    /**
     * 将 Stream 中非 null 元素以 {@code ::} 连接后写入 detail。
     *
     * @param key of the detail
     * @param values, can be null
     * @return builder for chaining
     */
    public EventBuilder detail(String key, Stream<String> values) {
        if (values == null) {
            return this;
        }
        return detail(key, values.filter(Objects::nonNull).collect(Collectors.joining("::")));
    }

    /**
     * 控制事件写入时机。
     * <p>默认：{@link #success()} 在会话事务提交时写入；{@link #error(String)} 立即写入并通知监听器。</p>
     * @param forcedValue {@code true} 立即写入事件存储；{@code false} 延迟至事务提交
     * @return 当前构建器
     */
    public EventBuilder storeImmediately(boolean forcedValue) {
        this.storeImmediately = forcedValue;
        return this;
    }

    /** 移除指定 detail 键。 */
    public EventBuilder removeDetail(String key) {
        if (event.getDetails() != null) {
            event.getDetails().remove(key);
        }
        return this;
    }

    /** @return 正在组装的 {@link Event} 实例 */
    public Event getEvent() {
        return event;
    }

    /** 标记事件成功并按默认策略发送。 */
    public void success() {
        send(this.storeImmediately == null ? false : this.storeImmediately);
    }

    /**
     * 标记事件失败：自动追加 {@code _ERROR} 后缀至类型并记录错误码。
     * @param error {@link Errors} 中的错误码
     */
    public void error(String error) {
        if (Objects.isNull(event.getType())) {
            throw new IllegalStateException("Attempted to define event error without first setting the event type");
        }

        if (!event.getType().name().endsWith("_ERROR")) {
            event.setType(EventType.valueOf(event.getType().name() + "_ERROR"));
        }
        event.setError(error);
        send(this.storeImmediately == null ? true : this.storeImmediately);
    }

    /** 克隆构建器及其内部事件快照。 */
    @Override
    public EventBuilder clone() {
        return new EventBuilder(session, store, listeners, realm, event.clone());
    }

    private void send(boolean sendImmediately) {
        event.setTime(Time.currentTimeMillis());
        event.setId(UUID.randomUUID().toString());

        Set<String> eventTypes = realm.getEnabledEventTypesStream().collect(Collectors.toSet());
        if (sendImmediately) {
            KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), session.getContext(), innerSession -> {
                EventStoreProvider store = this.isEventsEnabled ? getEventStoreProvider(innerSession) : null;
                List<EventListenerProvider> listeners = getEventListeners(innerSession, realm);

                sendNow(store, eventTypes, listeners);
            });
        } else {
            sendNow(this.store, eventTypes, this.listeners);
        }
    }

    private void sendNow(EventStoreProvider targetStore, Set<String> eventTypes, List<EventListenerProvider> targetListeners) {
        if (targetStore != null) {
            if (eventTypes.isEmpty() && event.getType().isSaveByDefault() || eventTypes.contains(event.getType().name())) {
                targetStore.onEvent(event);
            }
        }

        traceEvent();

        for (EventListenerProvider l : targetListeners) {
            try {
                l.onEvent(event);
            } catch (Throwable t) {
                log.error("Failed to send type to " + l, t);
            }
        }
    }

    private void traceEvent() {
        var tracing = session.getProvider(TracingProvider.class);
        var span = tracing.getCurrentSpan();

        if (span.isRecording()) {
            final var ab = Attributes.builder();
            ab.put(TracingAttributes.EVENT_ID, event.getId());
            ab.put(TracingAttributes.REALM_ID, event.getRealmId());
            ab.put(TracingAttributes.REALM_NAME, event.getRealmName());
            ab.put(TracingAttributes.CLIENT_ID, event.getClientId());
            ab.put(TracingAttributes.USER_ID, event.getUserId());
            ab.put(TracingAttributes.SESSION_ID, event.getSessionId());
            ab.put("ipAddress", event.getIpAddress());
            ab.put(TracingAttributes.EVENT_ERROR, event.getError());

            var details = event.getDetails();
            if (details != null) {
                details.forEach((k, v) -> ab.put(TracingAttributes.KC_PREFIX + "details." + k, v));
            }
            if (event.getType().name().endsWith("_ERROR")) {
                span.setStatus(StatusCode.ERROR);
            }
            span.addEvent(event.getType().name(), ab.build(), event.getTime(), TimeUnit.MILLISECONDS);
        }
    }

}
