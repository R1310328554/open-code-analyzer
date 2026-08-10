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
package org.keycloak.services.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.util.PemUtils;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocolService;
import org.keycloak.representations.idm.PublishedRealmRepresentation;
import org.keycloak.services.Urls;
import org.keycloak.services.cors.Cors;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 领域公开信息 REST 资源。
 * <p>暴露无需认证的领域元数据（realm 名称、令牌服务 URL、RSA 公钥等）。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PublicRealmResource {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(PublicRealmResource.class);

    /** 当前 HTTP 请求 */
    protected final HttpRequest request;

    /** 当前 HTTP 响应 */
    protected final HttpResponse response;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 当前领域模型 */
    protected final RealmModel realm;

    /**
     * 从会话上下文构造公开领域资源。
     * @param session Keycloak 会话
     */
    public PublicRealmResource(KeycloakSession session) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.request = session.getContext().getHttpRequest();
        this.response = session.getContext().getHttpResponse();
    }

    /**
     * CORS 预检请求
     *
     * @return
     */
    @Path("/")
    @OPTIONS
    public Response accountPreflight() {
        return Cors.builder().auth().preflight().add(Response.ok());
    }

    /**
     * 返回领域的公开 JSON 信息。
     *
     * @return
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public PublishedRealmRepresentation getRealm() {
        Cors.builder().allowAllOrigins().auth().add();
        return realmRep(session, realm, session.getContext().getUri());
    }

    /**
     * 构建 {@link PublishedRealmRepresentation}。
     * @param session Keycloak 会话
     * @param realm 领域
     * @param uriInfo URI 信息
     * @return 公开领域表示
     */
    public static PublishedRealmRepresentation realmRep(KeycloakSession session, RealmModel realm, UriInfo uriInfo) {
        PublishedRealmRepresentation rep = new PublishedRealmRepresentation();
        rep.setRealm(realm.getName());
        rep.setTokenServiceUrl(OIDCLoginProtocolService.tokenServiceBaseUrl(uriInfo).build(realm.getName()).toString());
        rep.setAccountServiceUrl(Urls.accountBase(uriInfo.getBaseUri()).build(realm.getName()).toString());
        rep.setPublicKeyPem(PemUtils.encodeKey(session.keys().getActiveRsaKey(realm).getPublicKey()));
        rep.setNotBefore(realm.getNotBefore());
        return rep;
    }


}
