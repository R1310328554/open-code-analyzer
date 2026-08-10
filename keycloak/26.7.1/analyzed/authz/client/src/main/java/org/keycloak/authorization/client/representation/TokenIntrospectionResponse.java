/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.client.representation;

import java.util.List;

import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.idm.authorization.Permission;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 令牌内省（introspection）响应，包含活跃状态与已授予的 {@link Permission} 列表。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class TokenIntrospectionResponse extends JsonWebToken {

    @JsonProperty
    private Boolean active;

    private List<Permission> permissions;

    /** 令牌是否仍处于有效/活跃状态。 */
    public Boolean getActive() {
        return this.active;
    }

    /** 内省结果中携带的权限集合。 */
    public List<Permission> getPermissions() {
        return this.permissions;
    }
}
