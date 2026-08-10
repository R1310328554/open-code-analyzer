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

import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;

/**
 * SAML AttributeStatement 映射器接口。
 * <p>实现此接口的协议映射器可在 SAML 断言构建阶段向 {@link AttributeStatementType} 写入属性。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface SAMLAttributeStatementMapper {

    /**
     * 转换 SAML AttributeStatement。
     * @param attributeStatement 目标属性语句
     * @param mappingModel 映射器配置
     * @param session Keycloak 会话
     * @param userSession 用户会话
     * @param clientSession 已认证客户端会话
     */
    void transformAttributeStatement(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel, KeycloakSession session,
                                        UserSessionModel userSession, AuthenticatedClientSessionModel clientSession);
}
