/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.authzen;

import java.util.Map;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.Urls;
import org.keycloak.urls.UrlType;
import org.keycloak.wellknown.WellKnownProvider;

/**
 * AuthZen Well-Known 配置提供者：发布策略决策点（PDP）及访问评估端点 URL。
 */
public class AuthZenWellKnownProvider implements WellKnownProvider {

    private final KeycloakSession session;

    /**
     * @param session 当前 Keycloak 会话
     */
    public AuthZenWellKnownProvider(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 返回 AuthZen 发现文档 JSON：PDP 基址、单次/批量评估端点。
     */
    @Override
    public Object getConfig() {
        RealmModel realm = session.getContext().getRealm();
        String realmUri = Urls.realmIssuer(session.getContext().getUri(UrlType.FRONTEND).getBaseUri(), realm.getName());

        return Map.of(
              "policy_decision_point", realmUri,
              "access_evaluation_endpoint", accessEvaluationEndpoint(realmUri),
              "access_evaluations_endpoint", accessEvaluationsEndpoint(realmUri)
        );
    }

    /** 构造单次访问评估端点完整 URL。 */
    public static String accessEvaluationEndpoint(String realmUri) {
        return String.format("%s/%s/%s", realmUri, AuthZenRealmResourceProviderFactory.PROVIDER_ID, AuthZen.EVALUATION_PATH);
    }

    /** 构造批量访问评估端点完整 URL。 */
    public static String accessEvaluationsEndpoint(String realmUri) {
        return String.format("%s/%s/%s", realmUri, AuthZenRealmResourceProviderFactory.PROVIDER_ID, AuthZen.EVALUATIONS_PATH);
    }

    /** Well-Known 提供者无额外资源需释放。 */
    @Override
    public void close() {
    }
}
