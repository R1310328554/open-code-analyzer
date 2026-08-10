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

package org.keycloak.exportimport;

import java.io.IOException;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.util.JsonSerialization;

/**
 * Keycloak 内置客户端描述转换器：识别并解析含 {@code clientId} 的 JSON 客户端表示。
 * <p>同时实现 {@link ClientDescriptionConverterFactory} 与 {@link ClientDescriptionConverter}，工厂 ID 为 {@code keycloak}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakClientDescriptionConverter implements ClientDescriptionConverterFactory, ClientDescriptionConverter {

    /** 提供者工厂标识。 */
    public static final String ID = "keycloak";

    /** 判断描述是否为含 {@code clientId} 字段的 JSON 对象。 */
    @Override
    public boolean isSupported(String description) {
        description = description.trim();
        return (description.startsWith("{") && description.endsWith("}") && description.contains("\"clientId\""));
    }

    /** 将 JSON 字符串反序列化为 {@link ClientRepresentation}。 */
    @Override
    public ClientRepresentation convertToInternal(String description) {
        try {
            return JsonSerialization.readValue(description, ClientRepresentation.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 无状态单例，直接返回自身。 */
    @Override
    public ClientDescriptionConverter create(KeycloakSession session) {
        return this;
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

    /** @return 工厂 ID {@code keycloak} */
    @Override
    public String getId() {
        return ID;
    }

}
