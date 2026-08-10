/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.protocol.oidc.grants.ciba;

import org.keycloak.crypto.SignatureProvider;
import org.keycloak.jose.jws.Algorithm;
import org.keycloak.models.CibaConfig;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.validation.ValidationContext;

import static org.keycloak.common.util.UriUtils.checkUrl;

/**
 * CIBA 客户端配置校验器。
 * <p>校验后台令牌投递模式、客户端通知端点 URL 及后台认证请求签名算法。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CibaClientValidation {

    /** 校验上下文，承载错误与待校验客户端 */
    private final ValidationContext<ClientModel> context;

    /** @param context 客户端校验上下文 */
    public CibaClientValidation(ValidationContext<ClientModel> context) {
        this.context = context;
    }

    /** 执行 CIBA 相关客户端属性校验，错误写入 {@link ValidationContext} */
    public void validate() {
        ClientModel client = context.getObjectToValidate();

        // Check only ping mode and poll mode allowed
        CibaConfig cibaConfig = client.getRealm().getCibaPolicy();
        String cibaMode = cibaConfig.getBackchannelTokenDeliveryMode(client);
        if (!CibaConfig.CIBA_SUPPORTED_MODES.contains(cibaMode)) {
            context.addError("cibaBackchannelTokenDeliveryMode", "Unsupported requested CIBA Backchannel Token Delivery Mode", "invalidCibaBackchannelTokenDeliveryMode");
        }

        // Check clientNotificationEndpoint URL configured for ping mode
        if (CibaConfig.CIBA_PING_MODE.equals(cibaMode)) {
            if (cibaConfig.getBackchannelClientNotificationEndpoint(client) == null) {
                context.addError("cibaBackchannelClientNotificationEndpoint", "CIBA Backchannel Client Notification Endpoint must be set for the CIBA ping mode", "missingCibaBackchannelClientNotificationEndpoint");
            }
        }

        // Validate clientNotificationEndpoint URL itself
        try {
            checkUrl(client.getRealm().getSslRequired(), cibaConfig.getBackchannelClientNotificationEndpoint(client), "backchannel_client_notification_endpoint");
        } catch (RuntimeException re) {
            context.addError("cibaBackchannelClientNotificationEndpoint", re.getMessage(), "invalidBackchannelClientNotificationEndpoint");
        }

        Algorithm alg = cibaConfig.getBackchannelAuthRequestSigningAlg(client);
        if (alg != null && !isSupportedBackchannelAuthenticationRequestSigningAlg(context.getSession(), alg.name())) {
            context.addError("cibaBackchannelAuthRequestSigningAlg", "Unsupported requested CIBA Backchannel Authentication Request Signing Algorithm", "invalidCibaBackchannelAuthRequestSigningAlg");
        }
    }

    /** 判断 CIBA 后台认证请求签名算法是否受支持（仅非对称算法，none 亦允许） */
    private static boolean isSupportedBackchannelAuthenticationRequestSigningAlg(KeycloakSession session, String alg) {
        // Consider removing 'none' . Not sure if we should allow him based on the CIBA specification...
        if (Algorithm.none.name().equals(alg)) {
            return true;
        }

        // Only asymmetric algorithms supported for CIBA signed request according to the specification
        SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, alg);
        return signatureProvider.isAsymmetricAlgorithm();
    }

}
