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
package org.keycloak.protocol.oidc.rar.model;

import java.util.Objects;

import org.keycloak.models.ClientScopeModel;

/**
 * 客户端范围中间表示。
 * <p>在 OAuth scope 与 RAR {@link AuthorizationDetails} 之间传递已匹配的 {@link ClientScopeModel}、参数化 scope 参数及原始请求 scope 字符串。</p>
 *
 * @author <a href="mailto:dgozalob@redhat.com">Daniel Gozalo</a>
 */
public class IntermediaryScopeRepresentation {
    /** 匹配的客户端范围模型 */
    final private ClientScopeModel scope;
    /** 授权请求中的原始 scope 字符串 */
    final private String requestedScopeString;
    /** 参数化 scope 提取的参数值（静态 scope 为 null） */
    final private String parameter;
    /** 是否为参数化 scope */
    final private boolean isParameterized;

    /**
     * 参数化 scope 构造。
     * @param scope 客户端范围
     * @param parameter 提取的参数
     * @param requestedScopeString 请求的 scope 字符串
     */
    public IntermediaryScopeRepresentation(ClientScopeModel scope, String parameter, String requestedScopeString) {
        this.scope = scope;
        this.parameter = parameter;
        this.isParameterized = scope.isParameterizedScope();
        this.requestedScopeString = requestedScopeString;
    }

    /** 静态 scope 构造 @param scope 客户端范围 */
    public IntermediaryScopeRepresentation(ClientScopeModel scope) {
        this.scope = scope;
        this.isParameterized = false;
        this.parameter = null;
        this.requestedScopeString = scope.getName();
    }

    /** @return 客户端范围模型 */
    public ClientScopeModel getScope() {
        return scope;
    }

    /** @return 参数化 scope 参数 */
    public String getParameter() {
        return parameter;
    }

    /** @return 是否为参数化 scope */
    public boolean isParameterized() {
        return isParameterized;
    }

    /** @return 原始请求 scope 字符串 */
    public String getRequestedScopeString() {
        return requestedScopeString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntermediaryScopeRepresentation that = (IntermediaryScopeRepresentation) o;
        return isParameterized == that.isParameterized && Objects.equals(scope.getName(), that.scope.getName()) && Objects.equals(parameter, that.parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope.getName(), parameter, isParameterized);
    }
}
