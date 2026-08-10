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
package org.keycloak.authorization.identity;

import org.keycloak.authorization.attribute.Attributes;

/**
 * 安全身份抽象：表示已认证的主体（用户或非人类实体），在策略评估中作为许可/拒绝的目标。
 * <p>通过 {@link #getAttributes()} 暴露角色、声明等属性，供策略条件引用。</p>
 *
 * <p>Represents a security identity, which can be a person or non-person entity that was previously authenticated.
 *
 * <p>An {@link Identity} plays an important role during the evaluation of policies as they represent the entity to which one or more permissions
 * should be granted or not, providing additional information and attributes that can be relevant to the different
 * access control methods involved during the evaluation of policies.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface Identity {

    /**
     * 返回身份唯一标识。
     *
     * Returns the unique identifier of this identity.
     *
     * @return the unique identifier of this identity
     */
    String getId();

    /**
     * 返回与身份关联的属性或声明。
     *
     * Returns the attributes or claims associated with this identity.
     *
     * @return the attributes or claims associated with this identity
     */
    Attributes getAttributes();

    /**
     * 是否拥有指定领域角色。
     *
     * Indicates if this identity is granted with a realm role with the given <code>roleName</code>.
     *
     * @param roleName the name of the role
     *
     * @return true if the identity has the given role. Otherwise, it returns false.
     */
    default boolean hasRealmRole(String roleName) {
        return getAttributes().containsValue("kc.realm.roles", roleName);
    }

    /**
     * 是否拥有指定客户端下的角色。
     *
     * Indicates if this identity is granted with a client role with the given <code>roleName</code>.
     *
     * @param clientId the client id
     * @param roleName the name of the role
     *
     * @return true if the identity has the given role. Otherwise, it returns false.
     */
    default boolean hasClientRole(String clientId, String roleName) {
        return getAttributes().containsValue("kc.client." + clientId + ".roles", roleName);
    }

    /**
     * 是否拥有给定客户端角色列表中的任一角色。
     *
     * Indicates if this identity is granted with a client role of one of the given <code>roleNames</code>.
     *
     * @param clientId the client id
     * @param roleNames list
     *
     * @return true if the identity has any of the given roles. Otherwise, it returns false.
     */
    default boolean hasOneClientRole(String clientId, String... roleNames) {
        for (String adminRole : roleNames) {
            if (hasClientRole(clientId, adminRole)) return true;
        }
        return false;
    }

}
