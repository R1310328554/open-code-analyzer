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
 */
package org.keycloak.broker.oidc.mappers;

import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.ConfigConstants;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import org.jboss.logging.Logger;

/**
 * Claim 到角色映射器抽象基类：按 OIDC claim 条件授予或撤销 Keycloak 角色。
 * <p>处理首次导入与后续同步，并避免多映射器重复授予同一角色。</p>
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>,
 * <a href="mailto:daniel.fesenmeyer@bosch.io">Daniel Fesenmeyer</a>
 */
public abstract class AbstractClaimToRoleMapper extends AbstractClaimMapper {

    private static final Logger LOG = Logger.getLogger(AbstractClaimToRoleMapper.class);

    /** 首次导入：claim 匹配时授予配置的角色。 */
    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        RoleModel role = getRole(session, realm, mapperModel);
        if (role == null) {
            return;
        }

        if (applies(mapperModel, context)) {
            user.grantRole(role);
        }
    }

    /** 旧版同步：claim 不匹配时撤销角色映射。 */
    @Override
    public void updateBrokeredUserLegacy(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        RoleModel role = getRole(session, realm, mapperModel);
        if (role == null) {
            return;
        }

        if (!applies(mapperModel, context)) {
            user.deleteRoleMapping(role);
        }
    }

    /** 同步更新角色映射，跳过已由其他映射器授予的角色。 */
    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        RoleModel role = getRole(session, realm, mapperModel);
        if (role == null) {
            return;
        }

        String roleName = mapperModel.getConfig().get(ConfigConstants.ROLE);
        // KEYCLOAK-8730：若前序映射器已授予同角色则跳过，避免误删有效角色
        if (!context.hasMapperGrantedRole(roleName)) {
            if (applies(mapperModel, context)) {
                context.addMapperGrantedRole(roleName);
                user.grantRole(role);
            } else {
                user.deleteRoleMapping(role);
            }
        }
    }


    /**
     * 子类实现：claim 满足映射条件时返回 {@code true}。
     *
     * @param mapperModel {@link IdentityProviderMapperModel} 引用
     * @param context {@link BrokeredIdentityContext} 引用
     * @return 可应用映射时为 {@code true}
     */
    protected abstract boolean applies(final IdentityProviderMapperModel mapperModel, final BrokeredIdentityContext context);

    /**
     * 解析映射器配置的角色名，返回对应 {@link RoleModel}；未找到时返回 {@code null}。
     *
     * @param session {@link KeycloakSession}
     * @param realm realm 引用
     * @param mapperModel 含角色配置的 {@link IdentityProviderMapperModel}
     * @return 对应 {@link RoleModel}，未找到时为 {@code null}
     */
    private RoleModel getRole(KeycloakSession session, final RealmModel realm, final IdentityProviderMapperModel mapperModel) {
        String roleName = mapperModel.getConfig().get(ConfigConstants.ROLE);
        RoleModel role = KeycloakModelUtils.getRoleFromString(session, realm, roleName);

        if (role == null) {
            LOG.warnf("Unable to find role '%s' referenced by mapper '%s' on realm '%s'.", roleName,
                    mapperModel.getName(), realm.getName());
        }

        return role;
    }
}
