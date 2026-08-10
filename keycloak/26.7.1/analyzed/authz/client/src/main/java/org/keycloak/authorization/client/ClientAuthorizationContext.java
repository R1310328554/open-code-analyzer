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
package org.keycloak.authorization.client;


import org.keycloak.AuthorizationContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig;

/**
 * 客户端侧授权上下文，在 {@link AuthorizationContext} 基础上持有 {@link AuthzClient} 引用。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ClientAuthorizationContext extends AuthorizationContext {

    private final AuthzClient client;

    /** 基于 RPT 令牌、当前路径配置与授权客户端构造上下文。 */
    public ClientAuthorizationContext(AccessToken authzToken, PolicyEnforcerConfig.PathConfig current, AuthzClient client) {
        super(authzToken, current);
        this.client = client;
    }

    /** 仅持有授权客户端、尚未绑定 RPT 的上下文。 */
    public ClientAuthorizationContext(AuthzClient client) {
        this.client = client;
    }

    /** 返回关联的 {@link AuthzClient} 实例。 */
    public AuthzClient getClient() {
        return client;
    }
}
