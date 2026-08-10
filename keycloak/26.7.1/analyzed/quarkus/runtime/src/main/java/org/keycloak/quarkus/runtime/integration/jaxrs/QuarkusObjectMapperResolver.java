/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.integration.jaxrs;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.services.util.ObjectMapperResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Quarkus CDI 环境下的 Jackson {@link ObjectMapper} 解析器，
 * 将 Keycloak 全局 JSON 序列化配置暴露为可注入 Bean。
 */
@Provider
@ApplicationScoped
public class QuarkusObjectMapperResolver extends ObjectMapperResolver {

    /** 向 CDI 容器提供 Keycloak 配置的 {@link ObjectMapper} 单例。 */
    @Produces
    public ObjectMapper getObjectMapper() {
        return mapper;
    }
}
