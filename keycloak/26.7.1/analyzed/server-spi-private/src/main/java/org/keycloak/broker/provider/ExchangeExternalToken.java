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
package org.keycloak.broker.provider;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.TokenExchangeContext;
import org.keycloak.protocol.oidc.TokenExchangeProvider;

/**
 * 外部令牌交换 SPI：将由本 IdP 签发的令牌交换为本地领域令牌。
 * <p>实现者识别 issuer、执行 {@link TokenExchangeContext} 交换并在完成后更新用户会话。</p>
 *
 * Exchange a token crafted by this provider for a local realm token.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ExchangeExternalToken {
    /** 判断给定 issuer 与表单参数是否由本提供者处理。 */
    boolean isIssuer(String issuer, MultivaluedMap<String, String> params);
    /** 执行外部令牌交换，返回 {@link BrokeredIdentityContext}。 */
    BrokeredIdentityContext exchangeExternal(TokenExchangeProvider tokenExchangeProvider, TokenExchangeContext tokenExchangeContext);

    /** 交换完成后的收尾逻辑（如写入会话备注）。 */
    void exchangeExternalComplete(UserSessionModel userSession, BrokeredIdentityContext context, MultivaluedMap<String, String> params);
}
