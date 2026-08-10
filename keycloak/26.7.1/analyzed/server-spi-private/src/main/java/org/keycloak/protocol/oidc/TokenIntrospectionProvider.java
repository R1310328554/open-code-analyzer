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
package org.keycloak.protocol.oidc;

import jakarta.ws.rs.core.Response;

import org.keycloak.events.EventBuilder;
import org.keycloak.provider.Provider;

/**
 * OAuth2 令牌自省（Token Introspection）提供者接口：针对特定令牌类型执行自省。
 * <p>实现类在 OAuth2 Token Introspection 端点中解析并返回令牌元数据。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface TokenIntrospectionProvider extends Provider {

    /**
     * 对指定令牌执行自省。
     *
     * @param token the token to introspect.
     * @param event 事件构建器，用于记录自省相关事件
     * @return 包含令牌信息的 HTTP 响应
     */
    Response introspect(String token, EventBuilder event);
}
