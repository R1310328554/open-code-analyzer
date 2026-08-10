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

package org.keycloak.testsuite.domainextension.spi.impl;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.testsuite.domainextension.spi.ExampleService;
import org.keycloak.testsuite.domainextension.spi.ExampleServiceProviderFactory;

/**
 * {@link ExampleServiceProviderFactory} 的测试套件实现，负责创建 {@link ExampleServiceImpl} 实例。
 */
public class ExampleServiceProviderFactoryImpl implements ExampleServiceProviderFactory {

    /** {@inheritDoc} 为给定会话创建示例服务实例。 */
    @Override
    public ExampleService create(KeycloakSession session) {
        return new ExampleServiceImpl(session);
    }

    /** {@inheritDoc} 初始化工厂配置。 */
    @Override
    public void init(Scope config) {

    }

    /** {@inheritDoc} 会话工厂就绪后的回调。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} 关闭工厂并释放资源。 */
    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回工厂标识 {@code exampleServiceImpl}。 */
    @Override
    public String getId() {
        return "exampleServiceImpl";
    }

}
