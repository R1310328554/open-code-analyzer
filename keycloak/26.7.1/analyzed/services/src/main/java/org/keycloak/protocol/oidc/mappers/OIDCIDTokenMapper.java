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

package org.keycloak.protocol.oidc.mappers;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.IDToken;

/**
 * OIDC ID Token 映射器接口：在 ID Token 签发前注入或修改声明。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface OIDCIDTokenMapper {

    /**
     * 转换 ID Token，按映射器配置写入声明。
     * @param token 当前 ID Token
     * @param mappingModel 协议映射器配置
     * @param session Keycloak 会话
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @return 更新后的 ID Token
     */
    IDToken transformIDToken(IDToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                               UserSessionModel userSession, ClientSessionContext clientSession);
}
