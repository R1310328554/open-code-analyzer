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

package org.keycloak.events.email;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * 邮件事件监听器 SPI 工厂。
 * <p>通过 {@code include-events}/{@code exclude-events} 配置需发送邮件的用户事件类型；默认包含登录失败、密码/TOTP/凭证变更等安全相关事件。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class EmailEventListenerProviderFactory implements EventListenerProviderFactory {

    /** 未显式配置时的默认事件类型集合。 */
    private static final Set<EventType> SUPPORTED_EVENTS = new HashSet<>();

    /** SPI 工厂标识：{@code email}。 */
    public static final String ID = "email";

    static {
        Collections.addAll(SUPPORTED_EVENTS, EventType.LOGIN_ERROR, EventType.UPDATE_PASSWORD, EventType.REMOVE_TOTP, EventType.UPDATE_TOTP, EventType.UPDATE_CREDENTIAL, EventType.REMOVE_CREDENTIAL);
    }

    /** 当前生效的需发信事件类型集合。 */
    private Set<EventType> includedEvents = new HashSet<>();

    @Override
    /** @param session 当前会话 @return 邮件事件监听器实例 */
    public EventListenerProvider create(KeycloakSession session) {
        return new EmailEventListenerProvider(session, includedEvents);
    }

    /** 追加需发信的事件类型（供测试或扩展使用）。 */
    public void addIncludedEvents(EventType... types) {
        includedEvents.addAll(Arrays.asList(types));
    }

    /** 移除需发信的事件类型（供测试或扩展使用）。 */
    public void removeIncludedEvents(EventType... types) {
        includedEvents.removeAll(Arrays.asList(types));
    }

    @Override
    /** 从配置解析 include/exclude 事件列表。 */
    public void init(Config.Scope config) {
        String[] include = config.getArray("include-events");
        if (include != null) {
            for (String i : include) {
                includedEvents.add(EventType.valueOf(i.toUpperCase()));
            }
        } else {
            includedEvents.addAll(SUPPORTED_EVENTS);
        }

        String[] exclude = config.getArray("exclude-events");
        if (exclude != null) {
            for (String e : exclude) {
                includedEvents.remove(EventType.valueOf(e.toUpperCase()));
            }
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    /** @return {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @return include/exclude 事件类型的配置元数据 */
    public List<ProviderConfigProperty> getConfigMetadata() {
        String[] supportedEvents = Arrays.stream(EventType.values())
                .map(EventType::name)
                .map(String::toLowerCase)
                .sorted(Comparator.naturalOrder())
                .toArray(String[]::new);
        return ProviderConfigurationBuilder.create()
                .property()
                .name("include-events")
                .type("string")
                .helpText("A comma-separated list of events that should be sent via email to the user's account.")
                .options(supportedEvents)
                .defaultValue("All events")
                .add()
                .property()
                .name("exclude-events")
                .type("string")
                .helpText("A comma-separated list of events that should not be sent via email to the user's account.")
                .options(supportedEvents)
                .add()
                .build();
    }
}
