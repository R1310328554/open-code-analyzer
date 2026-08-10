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
package org.keycloak.testsuite.authorization;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderAdminService;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.authorization.AbstractPolicyRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

/**
 * 测试策略提供者工厂：创建空实现的测试策略，用于授权服务集成测试。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class TestPolicyProviderFactory implements PolicyProviderFactory {

    /** {@inheritDoc} 策略在管理控制台中的显示名称。 */
    @Override
    public String getName() {
        return "Test";
    }

    /** {@inheritDoc} 策略分组为测试套件专用。 */
    @Override
    public String getGroup() {
        return "Test Suite";
    }

    /** {@inheritDoc} 创建绑定授权提供者的 {@link TestPolicyProvider} 实例。 */
    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        return new TestPolicyProvider(authorization);
    }

    /** {@inheritDoc} 返回空的策略表示对象。 */
    @Override
    public AbstractPolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        return new PolicyRepresentation();
    }

    /** {@inheritDoc} 策略表示类型为 {@link PolicyRepresentation}。 */
    @Override
    public Class getRepresentationType() {
        return PolicyRepresentation.class;
    }

    /** {@inheritDoc} 不提供管理 REST 资源。 */
    @Override
    public PolicyProviderAdminService getAdminResource(ResourceServer resourceServer, AuthorizationProvider authorization) {
        return null;
    }

    /** {@inheritDoc} 通过会话创建时不返回实例。 */
    @Override
    public PolicyProvider create(KeycloakSession session) {
        return null;
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

    /** {@inheritDoc} 返回 {@code test} 标识符。 */
    @Override
    public String getId() {
        return "test";
    }

    /** 空实现的测试策略提供者，评估时不执行任何操作。 */
    private static class TestPolicyProvider implements PolicyProvider {

        /** 关联的授权提供者，供测试扩展使用。 */
        private final AuthorizationProvider authorization;

        /**
         * @param authorization 授权提供者实例
         */
        public TestPolicyProvider(AuthorizationProvider authorization) {
            this.authorization = authorization;
        }

        /** {@inheritDoc} 空实现，不授予也不拒绝。 */
        @Override
        public void evaluate(Evaluation evaluation) {

        }

        @Override
        public void close() {

        }
    }
}
