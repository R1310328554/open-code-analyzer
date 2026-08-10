/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oidc.rar;

import java.util.List;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.UserSessionModel;
import org.keycloak.provider.Provider;
import org.keycloak.representations.AuthorizationDetailsJSONRepresentation;

/**
 * 授权详情（{@code authorization_details}）处理器接口，遵循 RAR（Rich Authorization Requests）规范。
 * <p>支持在授权请求与令牌请求中处理不同类型的 authorization_details（如可验证凭证签发场景）。</p>
 *
 * @author <a href="mailto:Forkim.Akwichek@adorsys.com">Forkim Akwichek</a>
 */
public interface AuthorizationDetailsProcessor<ADR extends AuthorizationDetailsJSONRepresentation> extends Provider {

    /** 检查当前运行上下文中是否支持本处理器。 */

    boolean isSupported();

    /**
     * @return 本处理器支持的 authorization_details {@code type} 声明值，通常对应 {@link AuthorizationDetailsProcessorFactory} 的 providerId
     */
    String getSupportedType();

    /**
     * @return 令牌响应中可创建的 {@link AuthorizationDetailsJSONRepresentation} 子类 Java 类型
     */
    Class<ADR> getSupportedResponseJavaType();

    /** 校验单条授权详情是否符合支持的凭证类型及其他约束。 */

    ADR validateAuthorizationDetail(AuthorizationDetailsJSONRepresentation authzDetail) throws InvalidAuthorizationDetailsException;

    /**
     * 处理 authorization_details 参数中的单条成员；无法处理时返回 {@code null}。
     *
     * @param userSession                   the user session
     * @param clientSessionCtx              the client session context
     * @param authorizationDetailsMember the authorization_details member (usually one member from the list) sent in the "authorization_details" request parameter
     * @return authorization details response if this processor can handle the parameter, null if the parameter is incompatible with this processor
     */
    ADR process(UserSessionModel userSession,
                ClientSessionContext clientSessionCtx,
                AuthorizationDetailsJSONRepresentation authorizationDetailsMember) throws InvalidAuthorizationDetailsException;

    /**
     * 请求中缺少 authorization_details 时调用，允许处理器仍生成响应。
     *
     * @param userSession      the user session
     * @param clientSessionCtx the client session context
     * @return authorization details response if this processor can handle current request in case that authorization_details parameter was not provided
     */
    List<ADR> handleMissingAuthorizationDetails(UserSessionModel userSession,
                                                ClientSessionContext clientSessionCtx) throws InvalidAuthorizationDetailsException;

    /**
     * 授权请求中使用了 authorization_details 但令牌请求中缺失时调用，处理已存储的授权详情。
     *
     * @param userSession       the user session
     * @param clientSessionCtx  the client session context
     * @param storedAuthDetailsMember the parsed member (usually one member of the list) from the authorization_details parameter that were stored during the authorization request
     * @return authorization details response if this processor can handle the stored authorization_details, null if the processor cannot handle the stored authorization_details
     */
    ADR processStoredAuthorizationDetails(UserSessionModel userSession,
                                          ClientSessionContext clientSessionCtx,
                                          AuthorizationDetailsJSONRepresentation storedAuthDetailsMember) throws InvalidAuthorizationDetailsException;

    /**
     * 授权详情处理完成、令牌响应创建前的钩子，用于后处理（如创建状态对象）。
     *
     * @param userSession      the user session
     * @param clientSessionCtx the client session context
     * @param authorizationDetailsResponse The response object of the proper type, which is supposed to be processed by this processor.
     */
    void afterAuthorizationDetailsProcessed(UserSessionModel userSession,
                                            ClientSessionContext clientSessionCtx,
                                            ADR authorizationDetailsResponse);


    /**
     * 在令牌响应中发送前清理授权详情（见 keycloak#50079）。
     *
     * @param authzDetail The typed authorization detail
     * @return A sanitized clone of the authorization detail
     */
    default ADR sanitizeBeforeSendingTokenResponse(ADR authzDetail) {
        return authzDetail;
    }

    /**
     * @param authzDetailsResponse 全部授权详情列表（可能含其他 type）
     * @return 仅包含本处理器对应 type 的子列表
     */
    default List<ADR> getSupportedAuthorizationDetails(List<AuthorizationDetailsJSONRepresentation> authzDetailsResponse) {
        if (authzDetailsResponse == null) {
            return null;
        }
        return authzDetailsResponse.stream()
                .filter(authDetailsResponse -> getSupportedType().equals(authDetailsResponse.getType()))
                .map(authDetailsResponse -> authDetailsResponse.asSubtype(getSupportedResponseJavaType()))
                .toList();
    }
}
