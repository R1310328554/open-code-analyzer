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
 *
 */

package org.keycloak.constants;

import org.keycloak.representations.idm.RoleRepresentation;

/**
 * Keycloak OID4VCI（可验证凭证）相关的内部常量。
 * <p>含领域属性名、角色、认证会话 note 等；协议规范常量见 {@link org.keycloak.OID4VCConstants}。</p>
 *
 * @author Pascal Knüppel
 */
public final class OID4VCIConstants {

    // 工具类，禁止实例化
    private OID4VCIConstants() {}

    /** OID4VC 协议标识符。 */
    public static final String OID4VC_PROTOCOL = "oid4vc";

    /** c_nonce 生命周期（秒）领域属性键。 */
    public static final String C_NONCE_LIFETIME_IN_SECONDS = "vc.c-nonce-lifetime-seconds";

    public static final String TIME_CLAIMS_STRATEGY = "oid4vci.time.claims.strategy";
    public static final String TIME_RANDOMIZE_WINDOW_SECONDS = "oid4vci.time.randomize.window.seconds";
    public static final String TIME_ROUND_UNIT = "oid4vci.time.round.unit";

    // --- 密钥绑定/凭证构建 ---
    /** 凭证来源端点属性键。 */
    public static final String SOURCE_ENDPOINT = "source_endpoint";

    // 逗号分隔的可信 IdP 别名，用于密钥 attestation 证明校验
    /** 证明者信任 IdP 列表领域属性键。 */
    public static final String OID4VCI_ATTESTER_TRUST_IDPS_ATTR = "oid4vci.attester_trust_idps";

    public static final RoleRepresentation CREDENTIAL_OFFER_CREATE =
            new RoleRepresentation("credential-offer-create", "Allow credential offer creation", false);

    /** 必需操作提供者 ID：可验证凭证发放。 */
    public static final String VERIFIABLE_CREDENTIAL_OFFER_PROVIDER_ID = "verifiable_credential_offer";

    /** 认证会话 note：凭证发放 nonce 句柄。 */
    public static final String CREDENTIAL_OFFER_NONCE = "credential_offer_nonce";

    /** 认证会话 note：凭证发放是否由管理员发起（true）或用户 AIA（false/缺失）。 */
    public static final String IS_ADMIN_INITIATED = "is_admin_initiated";

    /** 凭证发放邮件模板属性：凭证范围显示名。 */
    public static final String EMAIL_TEMPLATE_ATTR_CREDENTIAL_SCOPE_DISPLAY_NAME = "credentialScopeDisplayName";

    /** 批量凭证发放批次大小配置键。 */
    public static final String BATCH_CREDENTIAL_ISSUANCE_BATCH_SIZE = "oid4vci.batch_credential_issuance.batch_size";
    /** 批量发放默认最大批次大小。 */
    public static final int BATCH_CREDENTIAL_ISSUANCE_DEFAULT_MAX_BATCH_SIZE = 2;
}
