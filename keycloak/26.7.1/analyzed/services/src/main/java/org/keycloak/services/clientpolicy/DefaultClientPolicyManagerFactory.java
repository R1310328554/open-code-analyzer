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
 *
 */

package org.keycloak.services.clientpolicy;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import org.jboss.logging.Logger;

/**
 * 默认 {@link ClientPolicyManagerFactory}：创建 {@link DefaultClientPolicyManager} 实例。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultClientPolicyManagerFactory implements ClientPolicyManagerFactory {

    private static final Logger logger = Logger.getLogger(DefaultClientPolicyManagerFactory.class);

    /** @param session Keycloak 会话 @return DefaultClientPolicyManager 实例 */
    @Override
    public ClientPolicyManager create(KeycloakSession session) {
        return new DefaultClientPolicyManager(session);
    }

    /** SPI 初始化（无配置项） @param config 配置作用域 */
    @Override
    public void init(Config.Scope config) {

    }

    /** 工厂后置初始化 @param factory 会话工厂 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** 关闭工厂（无资源需释放） */
    @Override
    public void close() {

    }

    /** @return 提供方 ID {@code default} */
    @Override
    public String getId() {
        return "default";
    }
}
