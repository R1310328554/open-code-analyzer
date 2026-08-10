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

package org.keycloak.testsuite.actions;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 占位必需操作工厂，用于集成测试验证必需操作 SPI 的最小实现。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DummyRequiredActionFactory implements RequiredActionFactory {

    /** 提供者在 SPI 中的标识符。 */
    public static final String PROVIDER_ID = "dummy-action";

    /** {@inheritDoc} 管理控制台展示名称。 */
    @Override
    public String getDisplayText() {
        return "Dummy Action";
    }

    /** {@inheritDoc} 创建始终立即成功的匿名 {@link RequiredActionProvider}。 */
    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return new RequiredActionProvider() {
            @Override
            public void evaluateTriggers(RequiredActionContext context) {

            }

            @Override
            public void requiredActionChallenge(RequiredActionContext context) {
                // 无额外挑战，直接成功
                context.success();
            }

            @Override
            public void processAction(RequiredActionContext context) {

            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
