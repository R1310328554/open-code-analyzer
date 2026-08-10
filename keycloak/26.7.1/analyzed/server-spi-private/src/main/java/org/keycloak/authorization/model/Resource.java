/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 受保护资源模型，通常由资源服务器内的一组策略保护。
 *
 * Represents a resource, which is usually protected by a set of policies within a resource server.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface Resource {

    /** 资源查询过滤选项。 */
    public static enum FilterOption {
        ID("id"),
        NAME("name"),
        EXACT_NAME("name"),
        OWNER("owner"),
        TYPE("type"),
        URI("uri"),
        URI_NOT_NULL("uri_not_null"),
        OWNER_MANAGED_ACCESS("ownerManagedAccess"),
        SCOPE_ID("scopes.id");

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
     * 返回资源名称。
     *
     * Returns the resource's name.
     *
     * @return the name of this resource
     */
    String getName();

    /**
     * 设置资源唯一名称。
     *
     * Sets a name for this resource. The name must be unique.
     *
     * @param name the name of this resource
     */
    void setName(String name);

    /**
     * 返回面向终端用户的友好名称；未定义时返回 {@link #getName()}。
     *
     * Returns the end user friendly name for this resource. If not defined, value for {@link #getName()} is returned.
     *
     * @return the friendly name for this resource
     */
    String getDisplayName();

    /**
     * 设置面向终端用户的友好名称。
     *
     * Sets an end user friendly name for this resource.
     *
     * @param name the name of this resource
     */
    void setDisplayName(String name);

    /**
     * 返回标识本资源的 URI 集合。
     *
     * Returns a {@link List} containing all {@link java.net.URI} that uniquely identify this resource.
     *
     * @return a {@link List} if {@link java.net.URI} for this resource or empty list if not defined.
     */
    Set<String> getUris();

    /**
     * 设置标识本资源的 URI 列表。
     *
     * Sets a list of {@link java.net.URI} that uniquely identify this resource.
     *
     * @param uri an {@link java.net.URI} for this resource
     */
    void updateUris(Set<String> uri);


    /**
     * 返回资源类型字符串。
     *
     * Returns a string representing the type of this resource.
     *
     * @return the type of this resource or null if not defined
     */
    String getType();

    /**
     * 设置资源类型。
     *
     * Sets a string representing the type of this resource.
     *
     * @param type the type of this resource or null if not defined
     */
    void setType(String type);

    /**
     * 返回与本资源关联的 {@link Scope} 列表。
     *
     * Returns a {@link List} containing all the {@link Scope} associated with this resource.
     *
     * @return a list with all scopes associated with this resource
     */
     List<Scope> getScopes();

    /**
     * 返回资源图标 URI。
     *
     * Returns an icon {@link java.net.URI} for this resource.
     *
     * @return a uri for an icon
     */
    String getIconUri();

    /**
     * 设置资源图标 URI。
     *
     * Sets an icon {@link java.net.URI} for this resource.
     *
     * @param iconUri an uri for an icon
     */
    void setIconUri(String iconUri);

    /**
     * 返回所属 {@link ResourceServer}。
     *
     * Returns the {@link ResourceServer} to where this resource belongs to.
     *
     * @return the resource server associated with this resource
     */
     ResourceServer getResourceServer();

    /**
     * 返回资源属主标识。
     *
     * Returns the resource's owner, which is usually an identifier that uniquely identifies the resource's owner.
     *
     * @return the owner of this resource
     */
    String getOwner();

    /**
     * 指示资源是否可由属主自行管理（UMA）。
     *
     * Indicates if this resource can be managed by the resource owner.
     *
     * @return {@code true} if this resource can be managed by the resource owner. Otherwise, {@code false}.
     */
    boolean isOwnerManagedAccess();

    /**
     * 设置是否允许属主自行管理。
     *
     * Sets if this resource can be managed by the resource owner.
     *
     * @param ownerManagedAccess {@code true} indicates that this resource can be managed by the resource owner.
     */
    void setOwnerManagedAccess(boolean ownerManagedAccess);

    /**
     * 更新与本资源关联的范围集合。
     *
     * Update the set of scopes associated with this resource.
     *
     * @param scopes the list of scopes to update
     */
    void updateScopes(Set<Scope> scopes);

    /**
     * 返回资源属性映射。
     *
     * Returns the attributes associated with this resource.
     *
     * @return a map holding the attributes associated with this resource
     */
    Map<String, List<String>> getAttributes();

    /**
     * 返回指定属性的第一个值。
     *
     * Returns the first value of an attribute with the given <code>name</code>
     *
     * @param name of the attribute
     * @return the first value of an attribute
     */
    String getSingleAttribute(String name);

    /**
     * 返回指定属性的全部值。
     *
     * Returns the values of an attribute with the given <code>name</code>
     *
     * @param name of the attribute
     * @return the values of an attribute
     */
    List<String> getAttribute(String name);

    /**
     * 设置指定名称与值的属性。
     *
     * Sets an attribute with the given <code>name</code> and <code>values</code>.
     *
     * @param name the attribute name
     * @param values the attribute values
     */
    void setAttribute(String name, List<String> values);

    void removeAttribute(String name);
}
