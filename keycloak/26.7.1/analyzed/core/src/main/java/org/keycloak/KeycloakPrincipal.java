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

package org.keycloak;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.Principal;

import org.keycloak.common.util.DelegatingSerializationFilter;

/**
 * 表示已认证 Keycloak 用户的 {@link Principal}，关联 {@link KeycloakSecurityContext} 安全上下文。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class KeycloakPrincipal<T extends KeycloakSecurityContext> implements Principal, Serializable {
    /** 主体名称（通常为用户名）。 */
    protected final String name;
    /** 关联的 Keycloak 安全上下文。 */
    protected final T context;

    /**
     * @param name 主体名称
     * @param context 安全上下文
     */
    public KeycloakPrincipal(String name, T context) {
        this.name = name;
        this.context = context;
    }

    /** 返回关联的安全上下文。 */
    public T getKeycloakSecurityContext() {
        return context;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeycloakPrincipal that = (KeycloakPrincipal) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    /** 反序列化时使用白名单过滤，仅允许 Keycloak 相关类。 */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        DelegatingSerializationFilter.builder()
                .addAllowedClass(KeycloakPrincipal.class)
                .addAllowedClass(KeycloakSecurityContext.class)
                .addAllowedPattern("org.keycloak.adapters.RefreshableKeycloakSecurityContext")
                .setFilter(in);

        in.defaultReadObject();
    }
}
