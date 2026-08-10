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

package org.keycloak.models;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.provider.ProviderEvent;

/**
 * 角色模型：表示 Realm 或客户端角色，支持复合角色、属性与 Provider 事件。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RoleModel {

    /** 角色重命名事件。 */
    interface RoleNameChangeEvent extends ProviderEvent {
        RealmModel getRealm();
        String getNewName();
        String getPreviousName();

        /**
         * @return the Client ID of the client, for a client role; {@code null}, for a realm role
         */
        String getClientId();
        KeycloakSession getKeycloakSession();
    }

    /** 角色相关 Provider 事件基接口。 */
    interface RoleEvent extends ProviderEvent {
        RealmModel getRealm();
        RoleModel getRole();
        KeycloakSession getKeycloakSession();
    }

    /** 角色授予用户事件。 */
    interface RoleGrantedEvent extends RoleModel.RoleEvent {
        static void fire(RoleModel role, UserModel user, KeycloakSession session) {
            session.getKeycloakSessionFactory().publish(new RoleModel.RoleGrantedEvent() {
                @Override
                public RealmModel getRealm() {
                    return session.getContext().getRealm();
                }

                @Override
                public RoleModel getRole() {
                    return role;
                }

                @Override
                public UserModel getUser() {
                    return user;
                }

                @Override
                public KeycloakSession getKeycloakSession() {
                    return session;
                }
            });
        }

        UserModel getUser();
    }

    /** 角色从用户撤销事件。 */
    interface RoleRevokedEvent extends RoleModel.RoleEvent {
        static void fire(RoleModel role, UserModel user, KeycloakSession session) {
            session.getKeycloakSessionFactory().publish(new RoleModel.RoleRevokedEvent() {
                @Override
                public RealmModel getRealm() {
                    return session.getContext().getRealm();
                }

                @Override
                public RoleModel getRole() {
                    return role;
                }

                @Override
                public UserModel getUser() {
                    return user;
                }

                @Override
                public KeycloakSession getKeycloakSession() {
                    return session;
                }
            });
        }

        UserModel getUser();
    }

    /** @return 角色名称 */
    String getName();

    /** @return 角色描述 */
    String getDescription();

    /** @param description 角色描述 */
    void setDescription(String description);

    /** @return 角色唯一 ID */
    String getId();

    /** @param name 新角色名称 */
    void setName(String name);

    /** @return 是否为复合角色 */
    boolean isComposite();

    /** @param role 待添加的复合子角色 */
    void addCompositeRole(RoleModel role);

    /** @param role 待移除的复合子角色 */
    void removeCompositeRole(RoleModel role);

    /**
     * 以流形式返回全部复合子角色。
     * Returns all composite roles as a stream.
     * @return Stream of {@link RoleModel}. Never returns {@code null}.
     */
    default Stream<RoleModel> getCompositesStream() {
        return getCompositesStream(null, null, null);
    }

    /**
     * 返回名称包含给定搜索串的复合子角色分页流。
     * Returns a paginated stream of composite roles of {@code this} role that contain given string in its name.
     *
     * @param search Case-insensitive search string
     * @param first Index of the first result to return. Ignored if negative or {@code null}.
     * @param max Maximum number of results to return. Ignored if negative or {@code null}.
     * @return A stream of requested roles ordered by the role name
     */
    Stream<RoleModel> getCompositesStream(String search, Integer first, Integer max);

    /** @return 是否为客户端角色 */
    boolean isClientRole();

    /** @return 所属容器（Realm 或 Client）ID */
    String getContainerId();

    /** @return 所属角色容器 */
    RoleContainerModel getContainer();

    /** @param role 待检查角色
     * @return 是否包含该角色（含复合） */
    boolean hasRole(RoleModel role);

    /** @param name 属性名
     * @param value 属性值 */
    void setSingleAttribute(String name, String value);

    /** @param name 属性名
     * @param values 属性值列表 */
    void setAttribute(String name, List<String> values);

    /** @param name 待删除属性名 */
    void removeAttribute(String name);

    default String getFirstAttribute(String name) {
        return getAttributeStream(name).findFirst().orElse(null);
    }

    /**
     * 以流形式返回匹配给定名称的角色属性值。
     * Returns all role's attributes that match the given name as a stream.
     * @param name {@code String} Name of an attribute to be used as a filter.
     * @return Stream of {@code String}. Never returns {@code null}.
     */
    Stream<String> getAttributeStream(String name);

    /** @return 全部角色属性映射 */
    Map<String, List<String>> getAttributes();
}
