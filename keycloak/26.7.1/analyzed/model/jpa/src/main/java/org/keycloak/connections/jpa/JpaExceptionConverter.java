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
package org.keycloak.connections.jpa;

import jakarta.persistence.PersistenceException;

import org.keycloak.provider.ExceptionConverter;

/**
 * JPA 异常转换器：将 {@link PersistenceException} 映射为 Keycloak 领域层可理解的 {@link org.keycloak.models.ModelException}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class JpaExceptionConverter implements ExceptionConverter {

    /** 仅处理 JPA 持久化异常，其余类型返回 null 交由其他转换器处理。 */
    @Override
    public Throwable convert(Throwable e) {
        if (!(e instanceof PersistenceException)) return null;
        return PersistenceExceptionConverter.convert(e);
    }

    /** 转换器标识，供 SPI 注册与查找使用。 */
    @Override
    public String getId() {
        return "jpa";
    }
}
