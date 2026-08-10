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

package org.keycloak.authentication.requiredactions.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.userprofile.UserProfileContext;

/**
 * Abstraction, which allows to display updateProfile page in various contexts (Required action of already existing user, or first identity provider
 * login when user doesn't yet exists in Keycloak DB)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
/**
 * 更新档案上下文接口：封装用户名、邮箱、姓名及自定义属性的读写。
 */
public interface UpdateProfileContext {
    
    /** @return 用户档案上下文类型（如 UPDATE_PROFILE） */
    UserProfileContext getUserProfileContext();

    /** @return 领域是否允许编辑用户名 */
    boolean isEditUsernameAllowed();

    /** @return 当前用户名 */
    String getUsername();

    /** 设置用户名。 */
    void setUsername(String username);

    /** @return 是否允许编辑邮箱 */
    boolean isEditEmailAllowed();

    /** @return 当前邮箱 */
    String getEmail();

    /** 设置邮箱。 */
    void setEmail(String email);

    /** @return 名 */
    String getFirstName();

    /** 设置名。 */
    void setFirstName(String firstName);

    /** @return 姓 */
    String getLastName();

    /** 设置姓。 */
    void setLastName(String lastName);

    /** @return 全部用户属性映射 */
    Map<String, List<String>> getAttributes();

    /** 设置单值属性。 */
    void setSingleAttribute(String name, String value);

    /** 设置多值属性。 */
    void setAttribute(String key, List<String> value);

    /** @return 指定属性的首个值 */
    String getFirstAttribute(String name);

    /**
     * @deprecated 请改用 {@link #getAttributeStream(String) getAttributeStream}。
     */
    @Deprecated
    default List<String> getAttribute(String key) {
        return this.getAttributeStream(key).collect(Collectors.toList());
    }

    /**
     * 获取指定属性名的全部值。
     *
     * @param name the name of the attribute.
     * @return a non-null {@link Stream} of attribute values.
     */
    Stream<String> getAttributeStream(String name);
}
