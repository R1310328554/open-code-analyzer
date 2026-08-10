/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oid4vc.issuance.credentialoffer;

import java.util.List;

import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * OID4VCI 凭证发放（Credential Offer）Provider 接口。
 *
 * <p>通常由签发方创建 Credential Offer 并传递给钱包，可通过 {@code credential_offer_uri}、
 * 二维码（跨设备流程）、钱包提供的 {@code credential_offer_endpoint} 或其他渠道（邮件、消息等）交付。</p>
 *
 * <p>规范未详细说明如何向签发方请求 Credential Offer。Keycloak 提供 {@code /create-credential-offer}
 * 端点，所需权限因发放类型而异；管理控制台也可能提供其他创建方式。</p>
 *
 * <p>根据给定 {@code credential_configuration_id} 与签发用户的 Credential Offer 策略，
 * 所需权限可能足够宽松以支持「自签发」Credential Offer。</p>
 *
 * <p>钱包通过 Credential Offer 获得一组 {@code credential_configuration_ids} 对应的授权。</p>
 *
 * <p>Credential Offer 有两种授权类型：</p>
 * <ul>
 *     <li>{@code authorization_code} 授权码</li>
 *     <li>{@code pre-authorized_code} 预授权码</li>
 * </ul>
 *
 * <h4>Authorization Code Grant</h4>
 *
 * <p>使用 {@code authorization_code} 时，钱包可在 Authorization Request 的 {@code authorization_details}
 * 中引用某个 {@code credential_configuration_id}。该 grant 还包含不透明 {@code issuer_state}，
 * 钱包必须在 Authorization Request 中携带，以便签发方关联先前的 Credential Offer。</p>
 *
 * <p>钱包也可仅带 {@code scope}、不含 {@code authorization_details} 与 {@code issuer_state} 发起授权，
 * 从而完全绕过 Credential Offer 流程——授权服务器仍可能返回 Authorization Code，
 * 进而用于换取访问令牌并访问该 {@code scope} 关联的凭证。</p>
 *
 * <p>授权服务器是否放行、凭证端点是否返回目标凭证，取决于生效的 Credential 策略；
 * 策略可能要求已有 Credential Offer，从而禁止仅凭 scope 请求凭证。</p>
 *
 * <h4>Pre-Authorized Code Grant</h4>
 *
 * <p>预授权 grant 允许钱包直接在 Token Request 中使用，无需额外授权步骤，
 * 适用于无法使用 {@code authorization_code} 的场景。由于是 bearer 型 grant，
 * 可直接访问 {@code credential_configuration_ids} 引用的凭证，创建方必须谨慎选择传递渠道。</p>
 *
 * <p>作为第二因素，签发方可同时创建 {@code tx_code}，通过独立于预授权码的渠道传达。</p>
 *
 * https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-offer
 *
 * @author <a href="mailto:tdiesler@ibm.com">Thomas Diesler</a>
 */
public interface CredentialOfferProvider extends Provider {

    /**
     * 创建凭证发放状态。
     * @param user 当前登录/签发用户
     * @param grantType 授权类型（{@code authorization_code} 或 {@code pre-authorized_code}）
     * @param credentialConfigurationIds 凭证配置标识列表
     * @param targetClientId 目标客户端 ID（可选）
     * @param targetUserId 目标用户名（可选）
     * @param expireAt 过期时间（Unix 秒，可选）
     * @return 新建的 {@link CredentialOfferState}
     */
    CredentialOfferState createCredentialOffer(
            UserModel user,
            String grantType,
            List<String> credentialConfigurationIds,
            String targetClientId,
            String targetUserId,
            Integer expireAt
    );

    @Override
    default void close() {
    }
}
