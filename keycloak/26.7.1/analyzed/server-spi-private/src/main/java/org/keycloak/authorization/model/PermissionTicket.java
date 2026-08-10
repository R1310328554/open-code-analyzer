/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.model;

/**
 * 权限票（Permission Ticket）模型，表示 UMA 授权流程中的权限请求/授予记录。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface PermissionTicket {

    /** 权限票查询过滤选项。 */
    public static enum FilterOption {
        ID("id"),
        RESOURCE_ID("resource.id"),
        RESOURCE_NAME("resource.name"),
        SCOPE_ID("scope.id"),
        SCOPE_IS_NULL("scope_is_null"),
        OWNER("owner"),
        GRANTED("granted"),
        REQUESTER("requester"),
        REQUESTER_IS_NULL("requester_is_null"),
        POLICY_IS_NOT_NULL("policy_is_not_null"),
        POLICY_ID("policy.id"),
        /** 管理员过滤：忽略 owner/requester 限制，返回全部权限票。
         * a special filter option to ignore owner and requester checks in order to return any ticket, including those
         * that the user is not the owner or requester
         */
        IS_ADMIN("is_admin");

        private final String name;

        FilterOption(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 返回本实例的唯一标识。
     *
     * Returns the unique identifier for this instance.
     *
     * @return the unique identifier for this instance
     */
    String getId();

    /**
     * 返回资源属主标识。
     *
     * Returns the resource's owner, which is usually an identifier that uniquely identifies the resource's owner.
     *
     * @return the owner of this resource
     */
    String getOwner();

    /** 返回权限请求方标识。 */
    String getRequester();

    /**
     * 返回关联的 {@link Resource}。
     *
     * Returns the {@link Resource} associated with this instance
     *
     * @return the {@link Resource} associated with this instance
     */
    Resource getResource();

    /**
     * 返回关联的 {@link Scope}。
     *
     * Returns the {@link Scope} associated with this instance
     *
     * @return the {@link Scope} associated with this instance
     */
    Scope getScope();

    /** 是否已授予权限。 */
    boolean isGranted();

    /** 创建时间戳。 */
    Long getCreatedTimestamp();

    /** 授予时间戳。 */
    Long getGrantedTimestamp();
    /** 设置授予时间戳。 */
    void setGrantedTimestamp(Long millis);

    /**
     * 返回所属 {@link ResourceServer}。
     *
     * Returns the {@link ResourceServer} where this policy belongs to.
     *
     * @return a resource server
     */
    ResourceServer getResourceServer();

    /** 返回关联的 {@link Policy}。 */
    Policy getPolicy();

    /** 设置关联的 {@link Policy}。 */
    void setPolicy(Policy policy);
}
