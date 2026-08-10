/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.provider.ProviderEvent;

/**
 * 组织模型：表示 Realm 内的组织实体，管理成员、域名与 IdP 关联。
 */
public interface OrganizationModel {

    /** 组织标识用户属性键。 */
    String ORGANIZATION_ATTRIBUTE = "kc.org";
    /** 用户是否可切换组织的属性键。 */
    String ORGANIZATION_SWITCHABLE_ATTRIBUTE = "kc.org.switchable";
    /** 组织名称用户属性键。 */
    String ORGANIZATION_NAME_ATTRIBUTE = "kc.org.name";
    /** 组织域名用户属性键。 */
    String ORGANIZATION_DOMAIN_ATTRIBUTE = "kc.org.domain";
    /** 排除域名用户属性键。 */
    String ORGANIZATION_EXCLUDED_DOMAIN_ATTRIBUTE = "kc.org.excluded.domains";
    /** 组织别名属性键。 */
    String ALIAS = "alias";
    /** 组织未知时在登录页隐藏 IdP 的配置键。 */
    String HIDE_IDP_ON_LOGIN_WHEN_ORGANIZATION_UNKNOWN = "kc.org.broker.login.hide-when-org-unknown";
    /** 在其他组织已关联时在登录页显示 IdP 的配置键。 */
    String SHOW_IDP_ON_LOGIN_WHEN_LINKED_ELSEWHERE = "kc.org.broker.login.show-when-linked-elsewhere";

    /** IdP 重定向模式：按邮箱域名匹配触发自动重定向。 */
    enum IdentityProviderRedirectMode {
        /** 邮箱域名匹配时重定向 */ EMAIL_MATCH("kc.org.broker.redirect.mode.email-matches");

        private final String key;

        IdentityProviderRedirectMode(String key) {
            this.key = key;
        }

        public boolean isSet(IdentityProviderModel broker) {
            return Boolean.parseBoolean(broker.getConfig().get(key));
        }

        public String getKey() {
            return key;
        }
    }

    /** 组织成员关系变更事件的基接口。 */
    interface OrganizationMembershipEvent extends ProviderEvent {
        OrganizationModel getOrganization();
        UserModel getUser();
        KeycloakSession getSession();
    }

    /** 用户加入组织事件。 */
    interface OrganizationMemberJoinEvent extends OrganizationMembershipEvent {
        static void fire(OrganizationModel organization, UserModel user, KeycloakSession session) {
            session.getKeycloakSessionFactory().publish(new OrganizationModel.OrganizationMemberJoinEvent() {
                @Override
                public UserModel getUser() {
                    return user;
                }

                @Override
                public OrganizationModel getOrganization() {
                    return organization;
                }

                @Override
                public KeycloakSession getSession() {
                    return session;
                }
            });
        }
    }

    /** 用户离开组织事件。 */
    interface OrganizationMemberLeaveEvent extends OrganizationMembershipEvent {
        static void fire(OrganizationModel organization, UserModel user, KeycloakSession session) {
            session.getKeycloakSessionFactory().publish(new OrganizationModel.OrganizationMemberLeaveEvent() {
                @Override
                public UserModel getUser() {
                    return user;
                }

                @Override
                public OrganizationModel getOrganization() {
                    return organization;
                }

                @Override
                public KeycloakSession getSession() {
                    return session;
                }
            });
        }
    }

    /** 组织删除事件。 */
    interface OrganizationRemovedEvent extends ProviderEvent {
        OrganizationModel getOrganization();
        KeycloakSession getKeycloakSession();

        static void fire(OrganizationModel organization, KeycloakSession session) {
            session.getKeycloakSessionFactory().publish(new OrganizationRemovedEvent() {
                @Override
                public OrganizationModel getOrganization() {
                    return organization;
                }

                @Override
                public KeycloakSession getKeycloakSession() {
                    return session;
                }
            });
        }
    }

    /** @return 组织唯一标识符 */
    String getId();

    /** @param name 组织名称 */
    void setName(String name);

    /** @return 组织名称 */
    String getName();

    /** @return 组织别名 */
    String getAlias();

    /** @param alias 组织别名 */
    void setAlias(String alias);

    /** @return 组织是否启用 */
    boolean isEnabled();

    /** @param enabled 是否启用组织 */
    void setEnabled(boolean enabled);

    /** @return 组织描述 */
    String getDescription();

    /** @param description 组织描述 */
    void setDescription(String description);

    /** @return 重定向 URL */
    String getRedirectUrl();

    /** @param redirectUrl 重定向 URL */
    void setRedirectUrl(String redirectUrl);

    /** @return 组织属性映射 */
    Map<String, List<String>> getAttributes();

    /** @param attributes 组织属性映射 */
    void setAttributes(Map<String, List<String>> attributes);

    /** @return 组织域名流 */
    Stream<OrganizationDomainModel> getDomains();

    /** @param domains 组织域名集合 */
    void setDomains(Set<OrganizationDomainModel> domains);

    /** @return 关联的身份提供方流 */
    Stream<IdentityProviderModel> getIdentityProviders();

    /** @param user 用户
     * @return 该用户是否由组织管理 */
    boolean isManaged(UserModel user);

    /** @param user 用户
     * @return 用户是否为组织成员 */
    boolean isMember(UserModel user);
}
