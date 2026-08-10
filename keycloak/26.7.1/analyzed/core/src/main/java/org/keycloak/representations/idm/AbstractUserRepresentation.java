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

package org.keycloak.representations.idm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.json.StringListMapDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 用户表示的抽象基类，封装用户名、姓名、邮箱、自定义属性及启用状态等公共字段。
 * <p>
 * 被 {@code UserRepresentation} 等 REST/Admin API 模型继承。
 */
public abstract class AbstractUserRepresentation {

    /** 用户名属性键。 */
    public static String USERNAME = "username";
    /** 名（firstName）属性键。 */
    public static String FIRST_NAME = "firstName";
    /** 姓（lastName）属性键。 */
    public static String LAST_NAME = "lastName";
    /** 邮箱属性键。 */
    public static String EMAIL = "email";
    /** 区域设置（locale）属性键。 */
    public static String LOCALE = "locale";

    /** 用户唯一 ID。 */
    protected String id;
    /** 用户名。 */
    protected String username;
    /** 名。 */
    protected String firstName;
    /** 姓。 */
    protected String lastName;
    /** 邮箱地址。 */
    protected String email;
    /** 邮箱是否已验证。 */
    protected Boolean emailVerified;
    /** 自定义属性（键 → 值列表）。 */
    @JsonDeserialize(using = StringListMapDeserializer.class)
    protected Map<String, List<String>> attributes;
    /** 用户 Profile 元数据。 */
    private UserProfileMetadata userProfileMetadata;
    /** 账户是否启用。 */
    protected Boolean enabled;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 返回除根属性（username、email 等）外的自定义属性。
     *
     * @return 用户自定义属性映射
     */
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    /**
     * 返回包含根属性在内的全部用户属性。
     *
     * @return 完整属性映射
     */
    @JsonIgnore
    public Map<String, List<String>> getRawAttributes() {
        Map<String, List<String>> attrs = new HashMap<>(Optional.ofNullable(attributes).orElse(new HashMap<>()));

        if (username != null)
            attrs.put(USERNAME, Collections.singletonList(getUsername()));
        else
            attrs.remove(USERNAME);

        if (email != null)
            attrs.put(EMAIL, Collections.singletonList(getEmail()));
        else
            attrs.remove(EMAIL);

        if (lastName != null)
            attrs.put(LAST_NAME, Collections.singletonList(getLastName()));

        if (firstName != null)
            attrs.put(FIRST_NAME, Collections.singletonList(getFirstName()));

        return attrs;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    /**
     * 设置单个属性值（链式调用）。
     *
     * @param name 属性名
     * @param value 属性值，{@code null} 时清空
     * @param <R> 子类型
     * @return 当前实例
     */
    @SuppressWarnings("unchecked")
    public <R extends AbstractUserRepresentation> R singleAttribute(String name, String value) {
        if (this.attributes == null) this.attributes=new HashMap<>();
        attributes.put(name, (value == null ? Collections.emptyList() : Arrays.asList(value)));
        return (R) this;
    }

    /**
     * 返回指定属性的第一个值。
     *
     * @param key 属性名
     * @return 第一个值，不存在时返回 {@code null}
     */
    public String firstAttribute(String key) {
        return this.attributes == null ? null : this.attributes.get(key) == null ? null : this.attributes.get(key).isEmpty()? null : this.attributes.get(key).get(0);
    }

    public void setUserProfileMetadata(UserProfileMetadata userProfileMetadata) {
        this.userProfileMetadata = userProfileMetadata;
    }

    public UserProfileMetadata getUserProfileMetadata() {
        return userProfileMetadata;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
