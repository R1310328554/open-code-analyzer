/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.services.metrics.events;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import org.jboss.logging.Logger;

/**
 * 基于 Micrometer 的用户事件指标监听器：在事务提交后将 Keycloak {@link Event}
 * 计数为 {@code keycloak.user} Counter，并按配置附加 realm/idp/client/error 等标签。
 */
public class MicrometerUserEventMetricsEventListenerProvider implements EventListenerProvider {

    private static final Logger logger = Logger.getLogger(MicrometerUserEventMetricsEventListenerProvider.class);

    /** Micrometer 标签：realm 名称。 */
    static final String REALM_TAG = "realm";
    /** Micrometer 标签：身份提供者。 */
    static final String IDP_TAG = "idp";
    /** Micrometer 标签：客户端 ID。 */
    static final String CLIENT_ID_TAG = "client.id";
    /** Micrometer 标签：错误码。 */
    static final String ERROR_TAG = "error";
    /** Micrometer 标签：事件类型（小写、无 _ERROR 后缀）。 */
    private static final String EVENT_TAG = "event";

    private final boolean withIdp;
    private final boolean withRealm;
    private final boolean withClientId;
    private final HashSet<String> events;

    /** 事务完成后批量计数用户事件。 */
    private final EventListenerTransaction tx =
            new EventListenerTransaction(null, this::countEvent);
    private final Meter.MeterProvider<Counter> meterProvider;

    public MicrometerUserEventMetricsEventListenerProvider(KeycloakSession session, boolean withIdp, boolean withRealm, boolean withClientId, HashSet<String> events, Meter.MeterProvider<Counter> meterProvider) {
        this.withIdp = withIdp;
        this.withRealm = withRealm;
        this.withClientId = withClientId;
        this.events = events;
        this.meterProvider = meterProvider;
        session.getTransactionManager().enlistAfterCompletion(tx);
    }

    @Override
    public void onEvent(Event event) {
        tx.addEvent(event);
    }

    /** 按配置标签集合递增 user 事件 Counter。 */
    private void countEvent(Event event) {
        logger.debugf("Received user event of type %s in realm %s",
                event.getType().name(), event.getRealmName());

        String eventTag = format(event.getType());
        if (events != null && !events.contains(eventTag)) {
            return;
        }

        List<Tag> tags = new ArrayList<>(5);

        tags.add(Tag.of(EVENT_TAG, eventTag));
        addTag(tags, ERROR_TAG, getError(event));

        if (withRealm) {
            addTag(tags, REALM_TAG, event.getRealmName());
        }

        if (withIdp) {
            addTag(tags, IDP_TAG, getIdentityProvider(event));
        }

        if (withClientId) {
            addTag(tags, CLIENT_ID_TAG, getClientId(event));
        }

        meterProvider.withTags(tags).increment();
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // 管理事件暂不采集指标
    }

    private String getIdentityProvider(Event event) {
        String identityProvider = null;
        if (event.getDetails() != null) {
            identityProvider = event.getDetails().get(Details.IDENTITY_PROVIDER);
        }
        return identityProvider;
    }


    private String getClientId(Event event) {
        // CLIENT_NOT_FOUND 事件不使用真实 clientId 作为标签，避免指标基数爆炸
        return Errors.CLIENT_NOT_FOUND.equals(event.getError()) ? "unknown" : event.getClientId();
    }

    private String getError(Event event) {
        String error = event.getError();
        if (error == null && event.getType().name().endsWith("_ERROR")) {
            error = "unknown";
        }
        return error;
    }

    private void addTag(List<Tag> tags, String tagName, String value) {
        tags.add(Tag.of(tagName, value != null ? value : ""));
    }

    /** 将 {@link EventType} 格式化为指标 event 标签值（小写、去 _ERROR）。 */
    public static String format(EventType type) {
        // 去掉 _ERROR 后缀使同类事件共用 event 标签；仪表板通过 error 标签区分成败。
        String name = type.name();
        if (name.endsWith("_ERROR")) {
            name = name.substring(0, name.length() - "_ERROR".length());
        }
        return name.toLowerCase(Locale.ROOT);
    }

    @Override
    public void close() {
        // 无资源需释放
    }
}
