/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oid4vc.issuance;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.ws.rs.core.UriInfo;

import org.keycloak.http.HttpResponse;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oid4vc.model.JWTVCIssuerMetadata;
import org.keycloak.protocol.oidc.utils.JWKSServerUtils;
import org.keycloak.services.Urls;
import org.keycloak.services.resources.ServerMetadataResource;
import org.keycloak.urls.UrlType;
import org.keycloak.wellknown.WellKnownProvider;

import org.apache.http.HttpHeaders;
import org.jboss.logging.Logger;

/**
 * JWT VC 签发者元数据的 {@link WellKnownProvider} 实现，端点为 {@code /.well-known/jwt-vc-issuer}。
 * <p>返回签发者标识符与 JWKS，供 SD-JWT VC 钱包解析。</p>
 * <p>{@see https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-03.html#name-jwt-vc-issuer-metadata}</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class JWTVCIssuerWellKnownProvider implements WellKnownProvider {
    private static final Logger LOGGER = Logger.getLogger(JWTVCIssuerWellKnownProvider.class);
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public JWTVCIssuerWellKnownProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public Object getConfig() {
        UriInfo frontendUriInfo = session.getContext().getUri(UrlType.FRONTEND);
        RealmModel realm = session.getContext().getRealm();

        addDeprecationHeadersIfOldRoute();
        // 显式设置 Date 响应头，满足 RFC7231 与一致性测试套件要求
        session.getContext().getHttpResponse().setHeader(HttpHeaders.DATE, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));

        JWTVCIssuerMetadata config = new JWTVCIssuerMetadata();
        config.setIssuer(Urls.realmIssuer(frontendUriInfo.getBaseUri(), realm.getName()));

        JSONWebKeySet jwks = JWKSServerUtils.getRealmJwks(session, realm);
        config.setJwks(jwks);

        return config;
    }

    /**
     * 若使用旧版 Realm 作用域路由，附加弃用响应头并记录 WARN 日志。
     * <p>旧：{@code /realms/{realm}/.well-known/jwt-vc-issuer}</p>
     * <p>新：{@code /.well-known/jwt-vc-issuer/realms/{realm}}</p>
     */
    private void addDeprecationHeadersIfOldRoute() {
        String requestPath = session.getContext().getUri().getRequestUri().getPath();
        if (requestPath == null) {
            return;
        }

        int idxRealms = requestPath.indexOf("/realms/");
        int idxWellKnown = requestPath.indexOf("/.well-known/");
        boolean isOldRoute = idxRealms >= 0 && idxWellKnown > idxRealms;
        if (!isOldRoute) {
            return;
        }

        var realm = session.getContext().getRealm();
        if (realm == null) {
            return;
        }

        var base = session.getContext().getUri().getBaseUriBuilder();
        var successor = ServerMetadataResource.wellKnownProviderUrl(base)
                .build(JWTVCIssuerWellKnownProviderFactory.PROVIDER_ID, realm.getName());

        HttpResponse httpResponse = session.getContext().getHttpResponse();
        httpResponse.setHeader("Warning", "299 - \"Deprecated endpoint; use " + successor + "\"");
        httpResponse.setHeader("Deprecation", "true");
        httpResponse.setHeader("Link", "<" + successor + ">; rel=\"successor-version\"");

        LOGGER.warnf("Deprecated realm-scoped well-known endpoint accessed for JWT VC issuer in realm '%s'. Use %s instead.", realm.getName(), successor);
    }
}
