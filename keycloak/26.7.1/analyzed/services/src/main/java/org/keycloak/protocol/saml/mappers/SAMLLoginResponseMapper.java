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

package org.keycloak.protocol.saml.mappers;

import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;

/**
 * SAML 登录响应映射器接口。
 * <p>实现此接口的协议映射器可在 SAML 登录响应签发阶段对 {@link ResponseType} 进行转换。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface SAMLLoginResponseMapper {


    /**
     * 转换 SAML 登录响应。
     * @param response SAML 响应
     * @param mappingModel 映射器配置
     * @param session Keycloak 会话
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @return 转换后的 SAML 响应
     */
    ResponseType transformLoginResponse(ResponseType response, ProtocolMapperModel mappingModel, KeycloakSession session,
                                        UserSessionModel userSession, ClientSessionContext clientSessionCtx);
}
