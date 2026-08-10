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

package org.keycloak.testsuite.domainextension.rest;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Path;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;

/**
 * 域扩展示例根 REST 资源，挂载公司与需认证的公司子路径。
 */
public class ExampleRestResource {

	/** 当前 Keycloak 会话。 */
	private final KeycloakSession session;
    /** Bearer 令牌认证结果，可为 null。 */
    private final AuthenticationManager.AuthResult auth;
	
	/**
	 * @param session Keycloak 会话
	 */
	public ExampleRestResource(KeycloakSession session) {
		this.session = session;
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
	}
	
    /** 返回无需额外认证的公司资源子路径。 */
    @Path("companies")
    public CompanyResource getCompanyResource() {
        return new CompanyResource(session);
    }

    // 与 "companies" 端点功能相同，但需 Bearer 令牌且用户须具备 realm 角色 "admin"，仅作演示
    /** 返回需 realm 管理员角色才能访问的公司资源子路径。 */
    @Path("companies-auth")
    public CompanyResource getCompanyResourceAuthenticated() {
        checkRealmAdmin();
        return new CompanyResource(session);
    }

    /** 校验当前 Bearer 令牌持有者是否具备 realm 管理员角色。 */
    private void checkRealmAdmin() {
        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        } else if (auth.token().getRealmAccess() == null || !auth.token().getRealmAccess().isUserInRole("admin")) {
            throw new ForbiddenException("Does not have realm admin role");
        }
    }

}
