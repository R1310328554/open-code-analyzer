/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import org.jboss.logging.Logger;

/**
 * Subject（sub）映射器：向令牌写入标准 {@code sub} 声明（用户 ID）。
 * <p>优先级较高，便于后续映射器（如成对 sub）覆盖。</p>
 *
 * @author <a href="mailto:ggrazian@redhat.com">Giuseppe Graziano</a>
 */
public class SubMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, TokenIntrospectionTokenMapper {


    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "oidc-sub-mapper";

    private static final Logger logger = Logger.getLogger(SubMapper.class);

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, SubMapper.class);
    }

    /** {@inheritDoc} 返回各令牌类型的包含开关 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 控制台显示名：Subject (sub) */
    @Override
    public String getDisplayType() {
        return "Subject (sub)";
    }

    /** {@inheritDoc} 归类为令牌映射器 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** {@inheritDoc} 添加 sub 声明 */
    @Override
    public String getHelpText() {
        return "Add Subject (sub) claim";
    }

    /** 将当前用户 ID 设为 sub 声明 */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {
        if (userSession != null && userSession.getUser() != null) {
            token.subject(userSession.getUser().getId());
        }
    }

    /** {@inheritDoc} sub 映射器基础优先级 */
    @Override
    public int getPriority() {
        return ProtocolMapperUtils.SUB_MAPPER;
    }

    /**
     * 工厂方法：创建 sub 映射器配置。
     * @param accessToken 是否包含于 Access Token
     * @param introspectionEndpoint 是否包含于内省响应
     */
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        if (introspectionEndpoint) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "true");
        mapper.setConfig(config);
        return mapper;
    }

}
