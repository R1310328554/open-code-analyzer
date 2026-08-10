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

package org.keycloak.testsuite.rest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.adapters.action.LogoutAction;
import org.keycloak.representations.adapters.action.PushNotBeforeAction;
import org.keycloak.representations.adapters.action.TestAvailabilityAction;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * SAML 测试应用 {@link RealmResourceProvider} 工厂，维护 SAML 适配器动作队列。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TestSamlApplicationResourceProviderFactory implements RealmResourceProviderFactory {

    /** 管理端登出动作队列。 */
    private final BlockingQueue<LogoutAction> adminLogoutActions = new LinkedBlockingDeque<>();
    /** Push-not-before 动作队列。 */
    private final BlockingQueue<PushNotBeforeAction> pushNotBeforeActions = new LinkedBlockingDeque<>();
    /** 可用性测试动作队列。 */
    private final BlockingQueue<TestAvailabilityAction> testAvailabilityActions = new LinkedBlockingDeque<>();

    /** {@inheritDoc} 创建带共享 SAML 动作队列的 {@link TestSamlApplicationResourceProvider}。 */
    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new TestSamlApplicationResourceProvider(session, adminLogoutActions, pushNotBeforeActions, testAvailabilityActions);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** {@inheritDoc} 提供者 ID 为 {@code saml-app}。 */
    @Override
    public String getId() {
        return "saml-app";
    }
}
