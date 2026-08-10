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

import java.util.Set;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

import static org.keycloak.models.utils.KeycloakModelUtils.runJobInTransaction;

/**
 * 邮件事件监听器：将配置的用户事件通过 {@link EmailTemplateProvider} 发送通知邮件。
 * <p>仅处理已验证邮箱的用户；实际发信在事务提交后异步执行，避免与主事务耦合。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class EmailEventListenerProvider implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(EmailEventListenerProvider.class);

    /** 当前 Keycloak 会话。 */
    private KeycloakSession session;
    /** Realm 查询提供者。 */
    private RealmProvider model;
    /** 需要发送邮件的事件类型集合。 */
    private Set<EventType> includedEvents;
    /** 事务提交后发信的延迟事务包装。 */
    private EventListenerTransaction tx = new EventListenerTransaction(null, this::sendEmail);
    /** 会话工厂，用于独立事务中发信。 */
    private final KeycloakSessionFactory sessionFactory;

    /** @param session 当前会话 @param includedEvents 需发送邮件的事件类型集合 */
    public EmailEventListenerProvider(KeycloakSession session, Set<EventType> includedEvents) {
        this.session = session;
        this.model = session.realms();
        this.includedEvents = includedEvents;
        this.session.getTransactionManager().enlistAfterCompletion(tx);
        this.sessionFactory = session.getKeycloakSessionFactory();
    }

    @Override
    /** 收集需发信的用户事件，在事务提交后批量发送。 */
    public void onEvent(Event event) {
        if (includedEvents.contains(event.getType())) {
            if (event.getRealmId() != null && event.getUserId() != null) {
                tx.addEvent(event);
            }
        }
    }
    
    /** 在独立事务中渲染并发送事件通知邮件。 */
    private void sendEmail(Event event) {
        HttpRequest request = session.getContext().getHttpRequest();

        runJobInTransaction(sessionFactory, new KeycloakSessionTask() {
            @Override
            public void run(KeycloakSession session) {
                KeycloakContext context = session.getContext();
                RealmModel realm = session.realms().getRealm(event.getRealmId());

                context.setRealm(realm);

                String clientId = event.getClientId();

                if (clientId != null) {
                    ClientModel client = realm.getClientByClientId(clientId);
                    context.setClient(client);
                }

                context.setHttpRequest(request);

                UserModel user = session.users().getUserById(realm, event.getUserId());

                if (user != null && user.getEmail() != null && user.isEmailVerified()) {
                    try {
                        EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
                        emailTemplateProvider.setRealm(realm).setUser(user).sendEvent(event);
                    } catch (EmailException e) {
                        log.error("Failed to send type mail", e);
                    }
                }
            }
        });
    }

    @Override
    /** 管理事件不发送邮件（空实现）。 */
    public void onEvent(AdminEvent event, boolean includeRepresentation) {

    }

    @Override
    /** 关闭监听器（无资源需释放）。 */
    public void close() {
    }

}
