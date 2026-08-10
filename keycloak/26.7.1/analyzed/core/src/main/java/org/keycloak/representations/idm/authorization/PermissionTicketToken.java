/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.keycloak.TokenIdGenerator;
import org.keycloak.json.StringListMapDeserializer;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.JsonWebToken;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 权限票据 JWT 令牌，封装一组 {@link Permission} 及可选声明，用于 UMA 授权流程。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PermissionTicketToken extends JsonWebToken {

    /** 票据中包含的权限条目列表。 */
    private final List<Permission> permissions;

    /** 附加声明，键对应字符串值列表。 */
    @JsonDeserialize(using = StringListMapDeserializer.class)
    private Map<String, List<String>> claims;

    /** 创建空权限列表的票据令牌。 */
    public PermissionTicketToken() {
        this(new ArrayList<Permission>());
    }

    /**
     * 基于访问令牌上下文创建权限票据。
     *
     * @param permissions 权限条目列表
     * @param audience 受众
     * @param accessToken 源访问令牌，用于复制主体与时效字段
     */
    public PermissionTicketToken(List<Permission> permissions, String audience, AccessToken accessToken) {
        if (accessToken != null) {
            id(TokenIdGenerator.generateId());
            subject(accessToken.getSubject());
            this.exp(accessToken.getExp());
            this.nbf(accessToken.getNbf());
            iat(accessToken.getIat());
            issuedFor(accessToken.getIssuedFor());
        }
        if (audience != null) {
            audience(audience);
        }
        this.permissions = permissions;
    }

    /** @param resources 权限条目列表 */
    public PermissionTicketToken(List<Permission> resources) {
        this(resources, null, null);
    }

    /** @return 权限条目列表 */
    public List<Permission> getPermissions() {
        return this.permissions;
    }

    /** @return 声明映射 */
    public Map<String, List<String>> getClaims() {
        return claims;
    }

    /** @param claims 声明映射 */
    public void setClaims(Map<String, List<String>> claims) {
        this.claims = claims;
    }
}
