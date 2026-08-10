/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization;

import jakarta.ws.rs.Path;

import org.keycloak.authorization.protection.ProtectionService;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
/**
 * 客户端授权服务端点：挂载 {@code /protection} 下的资源保护 REST API。
 */
public class AuthorizationService {

    private final AuthorizationProvider authorization;

    /** @param authorization 授权 Provider 实例 */
    public AuthorizationService(AuthorizationProvider authorization) {
        this.authorization = authorization;
    }

    /** @return UMA 保护服务子资源 */
    @Path("/protection")
    public Object getProtectionService() {
        return new ProtectionService(authorization);
    }
}
