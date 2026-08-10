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

package org.keycloak.testsuite.domainextension.jpa;

import java.util.Collections;
import java.util.List;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

/**
 * 示例 JPA 实体提供方，向 Keycloak 注册自定义 {@link Company} 实体及数据库变更日志。
 *
 * @author <a href="mailto:erik.mulder@docdatapayments.com">Erik Mulder</a>
 */
public class ExampleJpaEntityProvider implements JpaEntityProvider {

    /** {@inheritDoc} 返回需纳入持久化上下文的实体类列表。 */
    @Override
    public List<Class<?>> getEntities() {
        return Collections.<Class<?>>singletonList(Company.class);
    }

    /** {@inheritDoc} 返回 Liquibase 变更日志资源路径。 */
    @Override
    public String getChangelogLocation() {
    	return "META-INF/example-changelog.xml";
    }
    
    @Override
    public void close() {
    }

    /** {@inheritDoc} 返回创建该提供方的工厂标识。 */
    @Override
    public String getFactoryId() {
        return ExampleJpaEntityProviderFactory.ID;
    }
}
