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

package org.keycloak.authentication.authenticators.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCClientSecretConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.util.BasicAuthHelper;
import org.keycloak.utils.StringUtil;


/**
 * Validates client based on "client_id" and "client_secret" sent either in request parameters or in "Authorization: Basic" header .
 *
 * See org.keycloak.adapters.authentication.ClientIdAndSecretAuthenticator for the adapter
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientIdAndSecretAuthenticator extends AbstractClientAuthenticator {

    /** Provider ID：client-secret。 */
    public static final String PROVIDER_ID = "client-secret";

    @Override
    /** 从 Basic 头或表单参数提取 client_id/client_secret 并校验。 */
    public void authenticateClient(ClientAuthenticationFlowContext context) {
        String client_id = null;
        String clientSecret = null;

        String authorizationHeader = context.getHttpRequest().getHttpHeaders().getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        MediaType mediaType = context.getHttpRequest().getHttpHeaders().getMediaType();
        boolean hasFormData = mediaType != null && mediaType.isCompatible(MediaType.APPLICATION_FORM_URLENCODED_TYPE);

        MultivaluedMap<String, String> formData = hasFormData ? context.getHttpRequest().getDecodedFormParameters() : null;

        String clientSecretRetrievalUsedMethod = null; // Tracks how was client_secret obtained

        if (authorizationHeader != null) {
            String[] usernameSecret = BasicAuthHelper.RFC6749.parseHeader(authorizationHeader);
            if (usernameSecret != null) {
                client_id = usernameSecret[0];
                clientSecret = usernameSecret[1];
                clientSecretRetrievalUsedMethod = OIDCLoginProtocol.CLIENT_SECRET_BASIC;
            } else {

                // 若请求已含 client_id 则不返回 401（避免 IE 等自动发送 Negotiate 头干扰公开客户端）
                if (formData != null && !formData.containsKey(OAuth2Constants.CLIENT_ID)) {
                    Response challengeResponse = Response.status(Response.Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + context.getRealm().getName() + "\"").build();
                    context.challenge(challengeResponse);
                    return;
                }
            }
        }

        if (formData != null) {
            // 即使存在 Basic 挑战，仍检查表单是否显式设置了 client_id，
            // so we can also support clients overriding flows and using challenges (e.g: basic) to authenticate their users
            if (formData.containsKey(OAuth2Constants.CLIENT_ID)) {
                client_id = formData.getFirst(OAuth2Constants.CLIENT_ID);
            }
            if (formData.containsKey(OAuth2Constants.CLIENT_SECRET)) {
                clientSecret = formData.getFirst(OAuth2Constants.CLIENT_SECRET);
                clientSecretRetrievalUsedMethod = OIDCLoginProtocol.CLIENT_SECRET_POST;
            }
        }

        if (client_id == null) {
            client_id = context.getSession().getAttribute("client_id", String.class);
        }

        if (client_id == null) {
            Response challengeResponse = ClientAuthUtil.errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_client", "Missing client_id parameter");
            context.challenge(challengeResponse);
            return;
        }

        context.getEvent().client(client_id);

        ClientModel client = context.getSession().clients().getClientByClientId(context.getRealm(), client_id);
        if (client == null) {
            context.failure(AuthenticationFlowError.CLIENT_NOT_FOUND, null);
            return;
        }

        context.setClient(client);

        if (!client.isEnabled()) {
            context.failure(AuthenticationFlowError.CLIENT_DISABLED, null);
            return;
        }

        // 公开客户端跳过 client_secret 校验
        if (client.isPublicClient()) {
            context.success();
            return;
        }

        if (clientSecret == null) {
            Response challengeResponse = ClientAuthUtil.errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "unauthorized_client", "Invalid client or Invalid client credentials");
            context.challenge(challengeResponse);
            return;
        }

        if (client.getSecret() == null) {
            reportFailedAuth(context);
            return;
        }

        OIDCClientSecretConfigWrapper wrapper = OIDCClientSecretConfigWrapper.fromClientModel(client);

        String clientSecretAllowedMethod = wrapper.getClientSecretAuthenticationAllowedMethod();
        if (StringUtil.isNotBlank(clientSecretAllowedMethod) && !clientSecretAllowedMethod.equals(clientSecretRetrievalUsedMethod)) {
            Response challengeResponse = ClientAuthUtil.errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "unauthorized_client", "Invalid method used to get client secret. Client requires method '"
                    + clientSecretAllowedMethod + "' to obtain client secret from the request");
            context.failure(AuthenticationFlowError.INVALID_CLIENT_CREDENTIALS, challengeResponse);
            return;
        }

        if (!wrapper.validateSecret(context.getSession(), clientSecret)) {
            if (!wrapper.validateRotatedSecret(context.getSession(), clientSecret)){
                reportFailedAuth(context);
                return;
            }
        }

        if (wrapper.isClientSecretExpired()){
            reportFailedAuth(context);
            return;
        }

        context.success();
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Client Id and Secret";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    /** @return 帮助说明：基于 client_id 与 client_secret 的客户端认证 */
    public String getHelpText() {
        return "Validates client based on 'client_id' and 'client_secret' sent either in request parameters or in 'Authorization: Basic' header";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new LinkedList<>();
    }

    @Override
    public List<ProviderConfigProperty> getConfigPropertiesPerClient() {
        // This impl doesn't use generic screen in admin console, but has its own screen. So no need to return anything here
        return Collections.emptyList();
    }

    @Override
    /** @return 适配器所需的 client secret 配置（优先从 vault 读取） */
    public Map<String, Object> getAdapterConfiguration(KeycloakSession session, ClientModel client) {
        Map<String, Object> result = new HashMap<>();
        String secret = client.getSecret();
        result.put(CredentialRepresentation.SECRET, session.vault().getStringSecret(secret).get().orElse(secret));
        return result;
    }

    @Override
    /** @return Provider ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Set<String> getProtocolAuthenticatorMethods(String loginProtocol) {
        if (loginProtocol.equals(OIDCLoginProtocol.LOGIN_PROTOCOL)) {
            Set<String> results = new LinkedHashSet<>();
            results.add(OIDCLoginProtocol.CLIENT_SECRET_BASIC);
            results.add(OIDCLoginProtocol.CLIENT_SECRET_POST);
            return results;
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public String getProtocolAuthenticatorMethod(ClientRepresentation client) {
        String clientSecretAllowedMethod = OIDCClientSecretConfigWrapper.fromClientRepresentation(client).getClientSecretAuthenticationAllowedMethod();
        return clientSecretAllowedMethod == null ? super.getProtocolAuthenticatorMethod(client) : clientSecretAllowedMethod;
    }

    @Override
    public void setClientAuthenticationMethod(ClientRepresentation client, String protocolAuthMethod) {
        client.setClientAuthenticatorType(getId());
        if (protocolAuthMethod != null) {
            OIDCClientSecretConfigWrapper.fromClientRepresentation(client).setClientSecretAuthenticationAllowedMethod(protocolAuthMethod);
        }
    }

    @Override
    /** @return 本认证器支持客户端密钥 */
    public boolean supportsSecret() {
        return true;
    }

    /** 记录无效客户端凭证并返回 401 错误。 */
    private void reportFailedAuth(ClientAuthenticationFlowContext context) {
        Response challengeResponse = ClientAuthUtil.errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "unauthorized_client", "Invalid client or Invalid client credentials");
        context.failure(AuthenticationFlowError.INVALID_CLIENT_CREDENTIALS, challengeResponse);
    }
}
