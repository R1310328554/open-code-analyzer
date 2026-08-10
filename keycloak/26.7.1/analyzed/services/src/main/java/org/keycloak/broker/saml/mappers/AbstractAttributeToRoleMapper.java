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
package org.keycloak.broker.saml.mappers;

import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
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
 * SAML 属性到角色映射器抽象基类：按 SAML 断言属性条件授予或撤销 Keycloak 角色。
 * <p>处理首次导入与后续同步，并避免多映射器重复授予同一角色。</p>
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>,
 * <a href="mailto:daniel.fesenmeyer@bosch.io">Daniel Fesenmeyer</a>
 */
public abstract class AbstractAttributeToRoleMapper extends AbstractIdentityProviderMapper {

    private static final Logger LOG = Logger.getLogger(AbstractAttributeToRoleMapper.class);

    /** 首次导入：SAML 属性匹配时授予配置的角色。 */
    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        RoleModel role = this.getRole(session, realm, mapperModel);
        if (role == null) {
            return;
        }

        if (this.applies(mapperModel, context)) {
            user.grantRole(role);
        }
    }

    /** 同步更新角色映射，跳过已由其他映射器授予的角色。 */
    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        RoleModel role = this.getRole(session, realm, mapperModel);
        if (role == null) {
            return;
        }

        String roleName = mapperModel.getConfig().get(ConfigConstants.ROLE);
        // KEYCLOAK-8730：若前序映射器已授予同角色则跳过，避免误删有效角色
        if (!context.hasMapperGrantedRole(roleName)) {
            if (this.applies(mapperModel, context)) {
                context.addMapperGrantedRole(roleName);
                if ((!role.isClientRole() && user.getRealmRoleMappingsStream().noneMatch(r -> r.equals(role)))
                    || (role.isClientRole() && user.getClientRoleMappingsStream(session.clients().getClientById(realm, role.getContainerId())).noneMatch(r -> r.equals(role)))) {
                    user.grantRole(role);
                }
            } else {
                if ((!role.isClientRole() && user.getRealmRoleMappingsStream().anyMatch(r -> r.equals(role)))
                    || (role.isClientRole() && user.getClientRoleMappingsStream(session.clients().getClientById(realm, role.getContainerId())).anyMatch(r -> r.equals(role)))) {
                    user.deleteRoleMapping(role);
                }
            }
        }
    }

    /**
     * 子类实现：SAML 属性满足映射条件时返回 {@code true}。
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
    /** 按配置解析 realm 或 client 角色，找不到时记录警告。 */
    private RoleModel getRole(KeycloakSession session, final RealmModel realm, final IdentityProviderMapperModel mapperModel) {
        String roleName = mapperModel.getConfig().get(ConfigConstants.ROLE);
        RoleModel role = KeycloakModelUtils.getRoleFromString(session, realm, roleName);
        if (role == null) {
            LOG.warnf("Unable to find role '%s' for mapper '%s' on realm '%s'.", roleName, mapperModel.getName(),
                    realm.getName());
        }
        return role;
    }
}
