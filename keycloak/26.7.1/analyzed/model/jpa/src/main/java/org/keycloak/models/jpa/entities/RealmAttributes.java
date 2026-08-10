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

package org.keycloak.models.jpa.entities;

/**
 * Realm 扩展属性键名常量，对应 {@link RealmAttributeEntity} 中的 name 字段。
 * <p>
 * 未在 {@link RealmEntity} 中单独建列的配置均通过此处的键持久化。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface RealmAttributes {

    /** HTML 格式展示名。 */
    String DISPLAY_NAME_HTML = "displayNameHtml";

    /** 管理员生成的 action token 有效期（秒）。 */
    String ACTION_TOKEN_GENERATED_BY_ADMIN_LIFESPAN = "actionTokenGeneratedByAdminLifespan";

    /** 用户生成的 action token 有效期（秒）。 */
    String ACTION_TOKEN_GENERATED_BY_USER_LIFESPAN = "actionTokenGeneratedByUserLifespan";

    // KEYCLOAK-7688 Offline Session Max for Offline Token
    /** 是否启用离线会话最大寿命限制。 */
    String OFFLINE_SESSION_MAX_LIFESPAN_ENABLED = "offlineSessionMaxLifespanEnabled";

    /** 离线会话最大寿命（秒）。 */
    String OFFLINE_SESSION_MAX_LIFESPAN = "offlineSessionMaxLifespan";

    /** 客户端会话空闲超时（秒）。 */
    String CLIENT_SESSION_IDLE_TIMEOUT = "clientSessionIdleTimeout";
    /** 客户端会话最大寿命（秒）。 */
    String CLIENT_SESSION_MAX_LIFESPAN = "clientSessionMaxLifespan";
    /** 客户端离线会话空闲超时（秒）。 */
    String CLIENT_OFFLINE_SESSION_IDLE_TIMEOUT = "clientOfflineSessionIdleTimeout";
    /** 客户端离线会话最大寿命（秒）。 */
    String CLIENT_OFFLINE_SESSION_MAX_LIFESPAN = "clientOfflineSessionMaxLifespan";
    /** WebAuthn RP 实体名称。 */
    String WEBAUTHN_POLICY_RP_ENTITY_NAME = "webAuthnPolicyRpEntityName";
    /** WebAuthn 签名算法列表。 */
    String WEBAUTHN_POLICY_SIGNATURE_ALGORITHMS = "webAuthnPolicySignatureAlgorithms";

    /** WebAuthn RP ID。 */
    String WEBAUTHN_POLICY_RP_ID = "webAuthnPolicyRpId";
    /** WebAuthn 认证传达偏好。 */
    String WEBAUTHN_POLICY_ATTESTATION_CONVEYANCE_PREFERENCE = "webAuthnPolicyAttestationConveyancePreference";
    /** WebAuthn 认证器附加方式。 */
    String WEBAUTHN_POLICY_AUTHENTICATOR_ATTACHMENT = "webAuthnPolicyAuthenticatorAttachment";
    /** WebAuthn 是否要求 resident key（旧字段）。 */
    String WEBAUTHN_POLICY_REQUIRE_RESIDENT_KEY = "webAuthnPolicyRequireResidentKey";
    /** WebAuthn resident key 策略。 */
    String WEBAUTHN_POLICY_RESIDENT_KEY = "webAuthnPolicyResidentKey";
    /** WebAuthn 用户验证要求。 */
    String WEBAUTHN_POLICY_USER_VERIFICATION_REQUIREMENT = "webAuthnPolicyUserVerificationRequirement";
    /** WebAuthn 注册超时（毫秒）。 */
    String WEBAUTHN_POLICY_CREATE_TIMEOUT = "webAuthnPolicyCreateTimeout";
    /** WebAuthn 是否禁止重复注册同一认证器。 */
    String WEBAUTHN_POLICY_AVOID_SAME_AUTHENTICATOR_REGISTER = "webAuthnPolicyAvoidSameAuthenticatorRegister";
    /** WebAuthn 可接受的 AAGUID 列表。 */
    String WEBAUTHN_POLICY_ACCEPTABLE_AAGUIDS = "webAuthnPolicyAcceptableAaguids";
    /** WebAuthn 额外允许的 origin。 */
    String WEBAUTHN_POLICY_EXTRA_ORIGINS = "webAuthnPolicyExtraOrigins";
    /** WebAuthn 是否启用 Passkeys。 */
    String WEBAUTHN_POLICY_PASSKEYS_ENABLED = "webAuthnPolicyPasskeysEnabled";
    /** WebAuthn 调解（mediation）策略。 */
    String WEBAUTHN_POLICY_MEDIATION = "webAuthnPolicyMediation";

    /** 管理事件过期时间（秒）。 */
    String ADMIN_EVENTS_EXPIRATION = "adminEventsExpiration";

    /** 首次 Broker 登录认证流 ID。 */
    String FIRST_BROKER_LOGIN_FLOW_ID = "firstBrokerLoginFlowId";

    /** 是否启用可验证凭证（Verifiable Credentials）。 */
    String VERIFIABLE_CREDENTIALS_ENABLED = "verifiableCredentialsEnabled";

    /** 是否启用组织（Organizations）功能。 */
    String ORGANIZATIONS_ENABLED = "organizationsEnabled";
    /** 是否启用管理权限细粒度控制。 */
    String ADMIN_PERMISSIONS_ENABLED = "adminPermissionsEnabled";
    /** 管理权限专用客户端 ID。 */
    String ADMIN_PERMISSIONS_CLIENT_ID = "adminPermissionsClientId";
    /** 是否启用 SCIM API。 */
    String SCIM_API_ENABLED = "scimApiEnabled";
}
