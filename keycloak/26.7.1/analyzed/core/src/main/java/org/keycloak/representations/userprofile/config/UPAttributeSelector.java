/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.representations.userprofile.config;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 属性可见性选择器配置，按 OAuth 范围决定属性是否在 UI 中展示。
 *
 * @author Vlastimil Elias <velias@redhat.com>
 *
 */
public class UPAttributeSelector implements Cloneable {

    /** 触发可见性的 OAuth 范围集合；为空表示始终展示。 */
    private Set<String> scopes;

    /** 默认构造函数，供 JSON 反序列化与反射使用。 */
    public UPAttributeSelector() {
        // 供反射使用
    }

    /**
     * 以范围集合构造可见性选择器。
     *
     * @param scopes OAuth 范围集合
     */
    public UPAttributeSelector(Set<String> scopes) {
        this.scopes = scopes;
    }

    /** @return 可见性范围集合 */
    public Set<String> getScopes() {
        return scopes;
    }

    /** @param scopes 可见性范围集合 */
    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    @Override
    public String toString() {
        return "UPAttributeSelector [scopes=" + scopes + "]";
    }

    /**
     * 深拷贝当前可见性选择器（集合内容独立复制）。
     *
     * @return 独立的选择器副本
     */
    @Override
    protected UPAttributeSelector clone() {
        return new UPAttributeSelector(scopes == null ? null : new HashSet<>(scopes));
    }

    @Override
    public int hashCode() {
        return Objects.hash(scopes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final UPAttributeSelector other = (UPAttributeSelector) obj;
        return Objects.equals(this.scopes, other.scopes);
    }
}
