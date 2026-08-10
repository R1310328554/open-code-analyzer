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
 */

package org.keycloak.testsuite.services.clientpolicy.executor;

import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;

import org.jboss.logging.Logger;

/**
 * 测试用客户端策略执行器，在指定 {@link ClientPolicyEvent} 上故意抛出异常。
 */
public class TestRaiseExceptionExecutor implements ClientPolicyExecutorProvider<TestRaiseExceptionExecutor.Configuration> {

    private static final Logger logger = Logger.getLogger(TestRaiseExceptionExecutor.class);

    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;
    /** 执行器运行时配置。 */
    private Configuration configuration;

    /** 创建执行器实例。 */
    public TestRaiseExceptionExecutor(KeycloakSession session) {
        this.session = session;
    }

    /** {@inheritDoc} 保存配置以供后续事件匹配。 */
    @Override
    public void setupConfiguration(TestRaiseExceptionExecutor.Configuration config) {
        this.configuration = config;
    }

    /** {@inheritDoc} 返回 {@link Configuration} 配置类。 */
    @Override
    public Class<Configuration> getExecutorConfigurationClass() {
        return Configuration.class;
    }

    /** 执行器配置，指定应触发异常的事件列表。 */
    public static class Configuration extends ClientPolicyExecutorConfigurationRepresentation {
        /** 需要抛出异常的策略事件列表。 */
        protected List<ClientPolicyEvent> events;

        /** 返回触发异常的事件列表。 */
        public List<ClientPolicyEvent> getEvents() {
            return events;
        }

        /** 设置触发异常的事件列表。 */
        public void setEvents(List<ClientPolicyEvent> events) {
            this.events = events;
        }
    }

    /** {@inheritDoc} 返回 {@link TestRaiseExceptionExecutorFactory#PROVIDER_ID}。 */
    @Override
    public String getProviderId() {
        return TestRaiseExceptionExecutorFactory.PROVIDER_ID;
    }

    /** {@inheritDoc} 在匹配事件上抛出 {@link ClientPolicyException}。 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        if (isThrowExceptionNeeded(context.getEvent())) throw new ClientPolicyException(context.getEvent().toString(), "Exception thrown intentionally");
    }

    /** 判断当前事件是否在配置的异常触发列表中。 */
    private boolean isThrowExceptionNeeded(ClientPolicyEvent event) {
        logger.tracev("Client Policy Trigger Event = {0}",  event);
        if (configuration != null && configuration.getEvents() != null && !configuration.getEvents().isEmpty()) {
            return configuration.getEvents().contains(event);
        }
        return false;
    }
}