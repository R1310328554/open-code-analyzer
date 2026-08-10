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
package org.keycloak.representations.idm.authorization;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 授权模式中的资源类型定义，描述类型标识、可用作用域及作用域别名。
 */
public class ResourceType {

    /** 资源类型标识。 */
    private final String type;
    /** 该类型允许的作用域集合。 */
    private final Set<String> scopes;
    /** 作用域别名映射（别名 → 实际作用域集合）。 */
    private final Map<String, Set<String>> scopeAliases;
    /** 关联的组类型标识。 */
    private final String groupType;

    /** Jackson 反序列化构造器。 */
    @JsonCreator
    public ResourceType(@JsonProperty("type") String type, @JsonProperty("scopes") Set<String> scopes) {
        this(type, scopes, Collections.emptyMap());
    }

    /** 指定类型、作用域与别名的构造器。 */
    public ResourceType(String type, Set<String> scopes, Map<String, Set<String>> scopeAliases) {
        this(type, scopes, scopeAliases, null);
    }

    /** 完整构造器。 */
    public ResourceType(String type, Set<String> scopes, Map<String, Set<String>> scopeAliases, String groupType) {
        this.type = type;
        this.scopes = Collections.unmodifiableSet(scopes);
        this.scopeAliases = scopeAliases;
        this.groupType = groupType;
    }

    /** @return 资源类型标识 */
    public String getType() {
        return type;
    }

    /** @return 可用作用域集合（不可变） */
    public Set<String> getScopes() {
        return Collections.unmodifiableSet(scopes);
    }

    /** @return 作用域别名映射（不可变） */
    public Map<String, Set<String>> getScopeAliases() {
        return Collections.unmodifiableMap(scopeAliases);
    }

    /** @return 关联组类型标识 */
    public String getGroupType() {
        return groupType;
    }
}
