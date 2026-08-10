/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.keycloak.models.KeycloakSession;
import org.keycloak.utils.MediaType;

import static org.keycloak.protocol.oidc.endpoints.IframeUtil.returnIframeFromResources;

/**
 * 第三方 Cookie 检测 iframe 端点（两步 HTML）。
 * <p>用于浏览器第三方 Cookie 策略变更时的兼容性探测。</p>
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class ThirdPartyCookiesIframeEndpoint {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public ThirdPartyCookiesIframeEndpoint(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Path("step1.html")
    @Produces(MediaType.TEXT_HTML_UTF_8)
    /** 第三方 Cookie 检测第一步 HTML。 @param version 资源版本 @return HTML 响应 */
    public Response step1(@QueryParam("version") String version) {
        return returnIframeFromResources("3p-cookies-step1.html", version, session);
    }

    @GET
    @Path("step2.html")
    @Produces(MediaType.TEXT_HTML_UTF_8)
    /** 第三方 Cookie 检测第二步 HTML。 @param version 资源版本 @return HTML 响应 */
    public Response step2(@QueryParam("version") String version) {
        return returnIframeFromResources("3p-cookies-step2.html", version, session);
    }
}
