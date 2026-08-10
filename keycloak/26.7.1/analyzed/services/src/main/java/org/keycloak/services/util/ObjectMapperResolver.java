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

package org.keycloak.services.util;

import java.util.stream.Stream;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.StreamSerializer;

/**
 * JAX-RS {@link ContextResolver}，为 REST 端点提供配置好的 Jackson {@link ObjectMapper}。
 * <p>支持 {@link Stream} 序列化、NON_NULL 包含策略及 classpath 模块自动发现。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Provider
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {
    /** 共享 ObjectMapper 实例 */
    protected ObjectMapper mapper;

    /** 初始化时使用 {@link ObjectMapperInitializer} 中的预配置实例。 */
    public ObjectMapperResolver() {
        mapper = ObjectMapperInitializer.OBJECT_MAPPER;
    }

    /**
     * 创建支持 {@link Stream} 序列化的 ObjectMapper。
     * <p>可通过系统属性 {@code keycloak.jsonPrettyPrint} 启用格式化输出，
     * 通过 {@code keycloak.jsonEnableJacksonModuleDiscovery} 启用 classpath 模块发现。</p>
     */
    public static ObjectMapper createStreamSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = TypeFactory.unknownType();
        JavaType streamType = mapper.getTypeFactory().constructParametricType(Stream.class, type);

        SimpleModule module = new SimpleModule();
        module.addSerializer(new StreamSerializer(streamType, type));
        mapper.registerModule(module);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        if (Boolean.parseBoolean(System.getProperty("keycloak.jsonPrettyPrint", "false"))) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        // 允许发现 classpath 上的 Jackson 模块
        if (Boolean.parseBoolean(System.getProperty("keycloak.jsonEnableJacksonModuleDiscovery", "true"))) {
            mapper.findAndRegisterModules();
        }

        return mapper;
    }

    /** 返回适用于所有类型的共享 ObjectMapper。 */
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

    /** 延迟初始化 ObjectMapper 单例的 holder 类。 */
    private static class ObjectMapperInitializer {

        /** 预配置的共享 ObjectMapper */
        private static final ObjectMapper OBJECT_MAPPER = createStreamSerializer();
    }
}
