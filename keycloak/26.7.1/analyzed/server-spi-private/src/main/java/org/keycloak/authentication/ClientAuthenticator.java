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

package org.keycloak.authentication;

import org.keycloak.provider.Provider;

/**
 * 客户端认证器 SPI：在认证流程中校验请求中的客户端凭证。
 * <p>须同时实现 {@link ClientAuthenticatorFactory}；适配器侧需实现 {@link org.keycloak.protocol.oidc.client.authentication.ClientCredentialsProvider} 向请求附加凭证。</p>
 *
 * This interface is for users that want to add custom client authenticators to an authentication flow.
 * You must implement this interface as well as a ClientAuthenticatorFactory.
 *
 * This interface is for verifying client credentials from request. On the adapter side, you must also implement org.keycloak.protocol.oidc.client.authentication.ClientCredentialsProvider , which is supposed
 * to add the client credentials to the request, which will ClientAuthenticator verify on server side
 *
 * @see org.keycloak.authentication.authenticators.client.ClientIdAndSecretAuthenticator
 * @see org.keycloak.authentication.authenticators.client.JWTClientAuthenticator
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientAuthenticator extends Provider {

    /**
     * 客户端认证器入口：检查 HTTP 请求是否满足要求，否则通过 context.challenge 返回挑战。
     *
     * Initial call for the authenticator.  This method should check the current HTTP request to determine if the request
     * satisfies the ClientAuthenticator's requirements.  If it doesn't, it should send back a challenge response by calling
     * the ClientAuthenticationFlowContext.challenge(Response).
     *
     * @param context
     */
    void authenticateClient(ClientAuthenticationFlowContext context);

}
