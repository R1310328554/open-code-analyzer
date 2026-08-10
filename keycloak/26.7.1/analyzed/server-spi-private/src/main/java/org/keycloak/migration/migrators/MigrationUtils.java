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

package org.keycloak.migration.migrators;

import java.util.stream.Collectors;

import org.keycloak.Config;
import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperContainerModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserConsentModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * 迁移过程共用的静态工具方法。
 * <p>包括管理角色补全、OTP 必需操作重命名、协议映射器升级与离线令牌兼容等。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class MigrationUtils {

    /** 在 master 与 realm-management 客户端上添加管理角色并挂入复合角色。 */
    public static void addAdminRole(RealmModel realm, String roleName) {
        ClientModel client = realm.getMasterAdminClient();
        if (client != null && client.getRole(roleName) == null) {
            RoleModel role = client.addRole(roleName);
            role.setDescription("${role_" + roleName + "}");

            client.getRealm().getRole(AdminRoles.ADMIN).addCompositeRole(role);
        }

        if (!realm.getName().equals(Config.getAdminRealm())) {
            client = realm.getClientByClientId(Constants.REALM_MANAGEMENT_CLIENT_ID);
            if (client != null && client.getRole(roleName) == null) {
                RoleModel role = client.addRole(roleName);
                role.setDescription("${role_" + roleName + "}");

                client.getRole(AdminRoles.REALM_ADMIN).addCompositeRole(role);
            }
        }
    }

    /** 将旧版 "Configure Totp" 必需操作显示名更新为 "Configure OTP"。 */
    public static void updateOTPRequiredAction(RequiredActionProviderModel otpAction) {
        if (otpAction == null) return;
        if (!UserModel.RequiredAction.CONFIGURE_TOTP.name().equals(otpAction.getProviderId())) return;
        if (!"Configure Totp".equals(otpAction.getName())) return;

        otpAction.setName("Configure OTP");
    }
    
    /** 为缺少 userinfo.token.claim 的映射器同步 id.token.claim 配置。 */
    public static void updateProtocolMappers(ProtocolMapperContainerModel client) {
        client.getProtocolMappersStream()
                .filter(mapper -> !mapper.getConfig().containsKey("userinfo.token.claim") && mapper.getConfig().containsKey("id.token.claim"))
                .peek(mapper -> mapper.getConfig().put("userinfo.token.claim", mapper.getConfig().get("id.token.claim")))
                .collect(Collectors.toSet()).stream() // to avoid ConcurrentModificationException
                .forEach(client::updateProtocolMapper);
    }


    // 4.0 之前无 clientScopeIds 的离线令牌被使用时调用
    /** 为旧版离线令牌自动补全 offline_access 与用户同意记录。 */
    public static void migrateOldOfflineToken(KeycloakSession session, RealmModel realm, ClientModel client, UserModel user) throws OAuthErrorException {
        ClientScopeModel offlineScope = KeycloakModelUtils.getClientScopeByName(realm, OAuth2Constants.OFFLINE_ACCESS);
        if (offlineScope == null) {
            throw new OAuthErrorException(OAuthErrorException.INVALID_GRANT, "Offline Access scope not found");
        }

        if (client.isConsentRequired()) {
            // Automatically add consents for client and for offline_access. We know that both were defacto approved by user already and offlineSession is still valid
            UserConsentModel consent = session.users().getConsentByClient(realm, user.getId(), client.getId());
            if (consent != null) {
                if (client.isDisplayOnConsentScreen()) {
                    consent.addGrantedClientScope(client);
                }
                if (offlineScope.isDisplayOnConsentScreen()) {
                    consent.addGrantedClientScope(offlineScope);
                }
                session.users().updateConsent(realm, user.getId(), consent);
            }
        }
    }

    /** 将客户端认证类型设为当前默认实现。 */
    public static void setDefaultClientAuthenticatorType(ClientModel s) {
        s.setClientAuthenticatorType(KeycloakModelUtils.getDefaultClientAuthenticatorType());
    }

    /** 是否为 OIDC 协议且非 bearer-only 的客户端。 */
    public static boolean isOIDCNonBearerOnlyClient(ClientModel c) {
        return (c.getProtocol() == null || "openid-connect".equals(c.getProtocol())) && !c.isBearerOnly();
    }
}
