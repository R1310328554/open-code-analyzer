/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
import org.keycloak.models.GroupModel;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * SAML 属性到组映射器抽象基类：按 SAML 断言属性条件将用户加入或移出指定组。
 * @author <a href="mailto:denis.bernard@avanade.com">Denis Bernard</a>,
 */
public abstract class AbstractAttributeToGroupMapper extends AbstractIdentityProviderMapper {

    /** 首次导入用户时，若 SAML 属性匹配则将用户加入配置的组。 */
    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        GroupModel group = KeycloakModelUtils.getGroupForIdpMapper(session, realm, mapperModel, context);
        if (group == null) {
            return;
        }

        if (this.applies(mapperModel, context)) {
            user.joinGroup(group);
        }
    }

    /** 同步更新：按属性变化加入或离开组，避免重复处理已分配组。 */
    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        GroupModel group = KeycloakModelUtils.getGroupForIdpMapper(session, realm, mapperModel, context);
        if (group == null) {
            return;
        }

        String groupId = group.getId();
        if (!context.hasMapperAssignedGroup(groupId)) {
            if (this.applies(mapperModel, context)) {
                context.addMapperAssignedGroup(groupId);
                user.joinGroup(group);
            } else {
                user.leaveGroup(group);
            }
        }
    }

    /**
     * 子类实现：当用户 SAML 属性满足映射条件时返回 {@code true}。
     *
     * @param mapperModel {@link IdentityProviderMapperModel} 引用
     * @param context {@link BrokeredIdentityContext} 引用
     * @return 可应用映射时为 {@code true}
     */
    protected abstract boolean applies(final IdentityProviderMapperModel mapperModel, final BrokeredIdentityContext context);

}
