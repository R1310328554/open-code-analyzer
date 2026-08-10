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

package org.keycloak.protocol.oidc;

import org.keycloak.Config;
import org.keycloak.exportimport.ClientDescriptionConverter;
import org.keycloak.exportimport.ClientDescriptionConverterFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * OIDC 客户端描述转换器工厂，识别含 {@code redirect_uris} 的 JSON 客户端元数据。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OIDCClientDescriptionConverterFactory implements ClientDescriptionConverterFactory {

    /** 工厂标识：openid-connect。 */
    public static final String ID = "openid-connect";

    /** @param description 客户端描述文本 @return 是否为 OIDC JSON 格式 */
    @Override
    public boolean isSupported(String description) {
        description = description.trim();
        return (description.startsWith("{") && description.endsWith("}") && description.contains("\"redirect_uris\""));
    }

    /** @param session Keycloak 会话 @return 转换器实例 */
    @Override
    public ClientDescriptionConverter create(KeycloakSession session) {
        return new OIDCClientDescriptionConverter(session);
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

    /** @return 工厂 ID */
    @Override
    public String getId() {
        return ID;
    }
}
