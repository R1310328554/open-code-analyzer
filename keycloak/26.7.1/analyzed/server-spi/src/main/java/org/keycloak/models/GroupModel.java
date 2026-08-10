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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.provider.ProviderEvent;

/**
 * 用户组模型：表示 Realm 或组织内的层级组结构，支持角色映射与属性。
 * <p>继承 {@link RoleMapperModel} 与 {@link Model}，提供子组、事件与组织关联。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface GroupModel extends RoleMapperModel, Model {

    /** 组类型：Realm 级组或组织级组。 */
    enum Type {
        /** Realm 级组 */ REALM(0),
        /** 组织级组 */ ORGANIZATION(1);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public static Type valueOf(int value) {
            Type[] values = values();

            for (int i = 0; i < values.length; i++) {
                if (values[i].value == value) {
                    return values[i];
                }
            }

            throw new IllegalArgumentException("No type found with value " + value);
        }

        public int intValue() {
            return value;
        }
    }

    /** 组生命周期相关事件的基接口。 */
    interface GroupEvent extends ProviderEvent {
        RealmModel getRealm();
        GroupModel getGroup();
        KeycloakSession getKeycloakSession();
    }

    /** 组创建事件。 */
    interface GroupCreatedEvent extends GroupEvent {
        static void fire(GroupModel group, KeycloakSession session) {
            session.getKeycloakSessionFactory().publish(new GroupCreatedEvent() {
                @Override
                public RealmModel getRealm() {
                    return session.getContext().getRealm();
                }

                @Override
                public GroupModel getGroup() {
                    return group;
                }

                @Override
                public KeycloakSession getKeycloakSession() {
                    return session;
                }
            });
        }
    }

    /** 组删除事件。 */
    interface GroupRemovedEvent extends GroupEvent {

    }

    /** 组更新事件。 */
    interface GroupUpdatedEvent extends GroupEvent {
        static void fire(GroupModel group, KeycloakSession session) {
            session.getKeycloakSessionFactory().publish(new GroupUpdatedEvent() {
                @Override
                public RealmModel getRealm() {
                    return session.getContext().getRealm();
                }

                @Override
                public GroupModel getGroup() {
                    return group;
                }

                @Override
                public KeycloakSession getKeycloakSession() {
                    return session;
                }
            });
        }
    }

    /** 用户加入组事件。 */
    interface GroupMemberJoinEvent extends GroupEvent {
        static void fire(GroupModel group, UserModel user, KeycloakSession session) {
            session.getKeycloakSessionFactory().publish(new GroupMemberJoinEvent() {
                @Override
                public RealmModel getRealm() {
                    return session.getContext().getRealm();
                }

                @Override
                public GroupModel getGroup() {
                    return group;
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

    /** 用户离开组事件。 */
    interface GroupMemberLeaveEvent extends GroupEvent {
        static void fire(GroupModel group, UserModel user, KeycloakSession session) {
            session.getKeycloakSessionFactory().publish(new GroupMemberLeaveEvent() {
                @Override
                public RealmModel getRealm() {
                    return session.getContext().getRealm();
                }

                @Override
                public GroupModel getGroup() {
                    return group;
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

    /** 组路径变更事件（移动组层级时触发）。 */
    interface GroupPathChangeEvent extends GroupEvent {
        String getNewPath();
        String getPreviousPath();

        static void fire(GroupModel group, String newPath, String previousPath, KeycloakSession session) {
            session.getKeycloakSessionFactory().publish(new GroupPathChangeEvent() {
                @Override
                public RealmModel getRealm() {
                    return session.getContext().getRealm();
                }

                @Override
                public GroupModel getGroup() {
                    return group;
                }

                @Override
                public KeycloakSession getKeycloakSession() {
                    return session;
                }

                @Override
                public String getNewPath() {
                    return newPath;
                }

                @Override
                public String getPreviousPath() {
                    return previousPath;
                }
            });
        }
    }

    Comparator<GroupModel> COMPARE_BY_NAME = Comparator.comparing(GroupModel::getName);

    /**
     * 获取组创建时间戳；该功能引入前创建的组可能为 {@code null}。
     * Get timestamp of group creation. May be null for groups created before this feature introduction.
     */
    default Long getCreatedTimestamp() {
        return null;
    }

    default void setCreatedTimestamp(Long timestamp) {
    }

    /**
     * 获取组最后修改时间戳；自该功能引入后未修改的组可能为 {@code null}。
     * Get timestamp of last group modification. May be null for groups that have not been modified
     * since this feature was introduced.
     */
    default Long getLastModifiedTimestamp() {
        return null;
    }

    default void setLastModifiedTimestamp(Long timestamp) {
    }

    /** @return 组唯一标识符 */
    String getId();

    /** @return 组名称 */
    String getName();

    /** @param name 组名称 */
    void setName(String name);

    /** @return 组描述 */
    String getDescription();

    /** @param description 组描述 */
    void setDescription(String description);

    /**
     * 设置指定属性的单一值，并移除该属性的其他已有值。
     * Set single value of specified attribute. Remove all other existing values
     *
     * @param name
     * @param value
     */
    void setSingleAttribute(String name, String value);

    void setAttribute(String name, List<String> values);

    void removeAttribute(String name);

    /**
     * 获取指定属性的第一个值；无值时返回 {@code null}，多值时不抛异常。
     * @param name
     * @return null if there is not any value of specified attribute or first value otherwise. Don't throw exception if there are more values of the attribute
     */
    String getFirstAttribute(String name);


    /**
     * 以流形式返回匹配给定名称的组属性值。
     * Returns group attributes that match the given name as a stream.
     * @param name {@code String} Name of the attribute to be used as a filter.
     * @return Stream of all attribute values or empty stream if there are not any values. Never return {@code null}.
     */
    Stream<String> getAttributeStream(String name);

    Map<String, List<String>> getAttributes();

    GroupModel getParent();
    String getParentId();

    /**
     * 返回父组下所有子组的流，按组名排序。
     * Returns all sub groups for the parent group as a stream.
     * The stream is sorted by the group name.
     *
     * @return Stream of {@link GroupModel}. Never returns {@code null}.
     */
    Stream<GroupModel> getSubGroupsStream();

    /**
     * 返回匹配模糊搜索的子组流（分页），按组名排序。
     * Returns all sub groups for the parent group matching the fuzzy search as a stream, paginated.
     * Stream is sorted by the group name.
     *
     * @param search searched string. If empty or {@code null} all subgroups are returned.
     * @return Stream of {@link GroupModel}. Never returns {@code null}.
     */
    default Stream<GroupModel> getSubGroupsStream(String search, Integer firstResult, Integer maxResults) {
       return getSubGroupsStream(search, false, firstResult, maxResults);
    }

    /**
     * 返回父组下所有子组的流（分页）。
     * Returns all sub groups for the parent group as a stream, paginated.
     *
     * @param firstResult First result to return. Ignored if negative or {@code null}.
     * @param maxResults Maximum number of results to return. Ignored if negative or {@code null}.
     * @return
     */
    default Stream<GroupModel> getSubGroupsStream(Integer firstResult, Integer maxResults) {
        return getSubGroupsStream(null, firstResult, maxResults);
    }

    /**
     * 返回匹配搜索条件的子组流（分页），按组名排序。
     * Returns all subgroups for the parent group matching the search as a stream, paginated.
     * Stream is sorted by the group name.
     *
     * @param search search string. If empty or {@code null} all subgroups are returned.
     * @param exact toggles fuzzy searching
     * @param firstResult First result to return. Ignored if negative or {@code null}.
     * @param maxResults Maximum number of results to return. Ignored if negative or {@code null}.
     * @return Stream of {@link GroupModel}. Never returns {@code null}.
     */
    default Stream<GroupModel> getSubGroupsStream(String search, Boolean exact, Integer firstResult, Integer maxResults) {
        Stream<GroupModel> allSubgroupsGroups = getSubGroupsStream().filter(group -> {
            if (search == null || search.isEmpty()) return true;
            if (Boolean.TRUE.equals(exact)) {
                return group.getName().equals(search);
            } else {
                return group.getName().toLowerCase().contains(search.toLowerCase());
            }
        });

        // Copied over from StreamsUtil from server-spi-private which is not available here
        if (firstResult != null && firstResult > 0) {
            allSubgroupsGroups = allSubgroupsGroups.skip(firstResult);
        }

        if (maxResults != null && maxResults >= 0) {
            allSubgroupsGroups = allSubgroupsGroups.limit(maxResults);
        }

        return allSubgroupsGroups;
    }

    /**
     * 返回该组下子组的数量。
     * Returns the number of groups contained beneath this group.
     *
     * @return The number of groups beneath this group. Never returns {@code null}.
     */
    default Long getSubGroupsCount() {
        return getSubGroupsStream().count();
    }

    /**
     * 设置父组；同时需在父组上调用 {@code addChild}，若无父组则在 {@link RealmModel} 上调用。
     * You must also call addChild on the parent group, addChild on RealmModel if there is no parent group
     *
     * @param group
     */
    void setParent(GroupModel group);

    /**
     * 添加子组，并自动在子组上调用 {@code setParent()}。
     * Automatically calls setParent() on the subGroup
     *
     * @param subGroup
     */
    void addChild(GroupModel subGroup);

    /**
     * Automatically calls setParent() on the subGroup
     *
     * @param subGroup
     */
    /**
     * 移除子组，并自动在子组上调用 {@code setParent()}。
     * Automatically calls setParent() on the subGroup
     *
     * @param subGroup
     */
    void removeChild(GroupModel subGroup);

    default boolean escapeSlashesInGroupPath() {
        return GroupProvider.DEFAULT_ESCAPE_SLASHES;
    }

    default Type getType() {
        return Type.REALM;
    }

    /**
     * 返回组所属组织；{@link Type#REALM} 类型组返回 {@code null}。
     * @return Organization this group belongs to, or null if the group is of {@link Type#REALM}.
     */
    OrganizationModel getOrganization();
}
