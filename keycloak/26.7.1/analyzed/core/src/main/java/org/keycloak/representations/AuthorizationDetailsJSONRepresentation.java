/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.representations;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.keycloak.util.AuthorizationDetailsParser;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 富授权请求（RAR）中 {@code authorization_details} 数组单条对象的 JSON 表示（RFC 9396）。
 * <p>
 * 请求与响应共用此基类；{@link #getType()} 区分授权细节类型，扩展数据存入 {@link #getCustomData()}。
 *
 * @author <a href="mailto:dgozalob@redhat.com">Daniel Gozalo</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9396#section-2">Request parameter "authorization_details"</a>
 */
public class AuthorizationDetailsJSONRepresentation implements Serializable {

    /** Keycloak 内部：静态 OAuth2 作用域的 RAR 类型 URI。 */
    public static final String STATIC_SCOPE_RAR_TYPE = "https://keycloak.org/auth-type/static-oauth2-scope";

    /** Keycloak 内部：参数化 OAuth2 作用域的 RAR 类型 URI。 */
    public static final String PARAMETERIZED_SCOPE_RAR_TYPE = "https://keycloak.org/auth-type/parameterized-oauth2-scope";

    /** @deprecated 请改用 {@link #PARAMETERIZED_SCOPE_RAR_TYPE}；为兼容历史数据保留。 */
    @Deprecated
    public static final String DYNAMIC_SCOPE_RAR_TYPE = "https://keycloak.org/auth-type/dynamic-oauth2-scope";

    @JsonProperty("type")
    private String type;
    @JsonProperty("locations")
    private List<String> locations;
    @JsonProperty("actions")
    private List<String> actions;
    @JsonProperty("datatypes")
    private List<String> datatypes;
    @JsonProperty("identifier")
    private String identifier;
    @JsonProperty("privileges")
    private List<String> privileges;

    private final Map<String, Object> customData = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public List<String> getDatatypes() {
        return datatypes;
    }

    public void setDatatypes(List<String> datatypes) {
        this.datatypes = datatypes;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<String> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<String> privileges) {
        this.privileges = privileges;
    }

    @JsonAnyGetter
    public Map<String, Object> getCustomData() {
        return customData;
    }

    @JsonAnySetter
    public void setCustomData(String key, Object value) {
        this.customData.put(key, value);
    }

    @Override
    public String toString() {
        return "AuthorizationDetailsJSONRepresentation{" +
                "type='" + type + '\'' +
                ", locations=" + locations +
                ", actions=" + actions +
                ", datatypes=" + datatypes +
                ", identifier='" + identifier + '\'' +
                ", privileges=" + privileges +
                ", customData=" + customData +
                '}';
    }

    /**
     * 按 {@link #getType()} 选择解析器，将当前对象转换为指定子类型。
     *
     * @param clazz 目标子类型
     * @return 解析成功后的子类型实例
     */
    public <T extends AuthorizationDetailsJSONRepresentation> T asSubtype(Class<T> clazz) {
        return AuthorizationDetailsParser.parseToSubtype(this, clazz);
    }

    @JsonIgnore
    public String getScopeNameFromCustomData() {
        if (isParameterizedScopeRarType(this.getType()) || this.getType().equalsIgnoreCase(STATIC_SCOPE_RAR_TYPE)) {
            List<String> accessList = (List<String>) this.customData.get("access");
            if (accessList.isEmpty()) {
                throw new RuntimeException("A RAR Scope representation should never have an empty access property");
            }
            return accessList.get(0);
        }
        return null;
    }

    @JsonIgnore
    public String getParameterizedScopeParamFromCustomData() {
        if(isParameterizedScopeRarType(this.getType())) {
            return (String) this.customData.get("scope_parameter");
        }
        return null;
    }

    private static boolean isParameterizedScopeRarType(String type) {
        return type.equalsIgnoreCase(PARAMETERIZED_SCOPE_RAR_TYPE) || type.equalsIgnoreCase(DYNAMIC_SCOPE_RAR_TYPE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizationDetailsJSONRepresentation that = (AuthorizationDetailsJSONRepresentation) o;
        return Objects.equals(type, that.type) && Objects.equals(locations, that.locations) && Objects.equals(actions, that.actions) && Objects.equals(datatypes, that.datatypes) && Objects.equals(identifier, that.identifier) && Objects.equals(privileges, that.privileges) && Objects.equals(customData, that.customData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, locations, actions, datatypes, identifier, privileges, customData);
    }


}
