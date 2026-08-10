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

package org.keycloak.authorization.common;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;

/**
 * 默认策略评估上下文：聚合身份、请求 claims 与运行时环境属性。
 * <p>提供 kc.time、网络、User-Agent、realm 名等标准属性键。</p>
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class DefaultEvaluationContext implements EvaluationContext {

    protected final KeycloakSession keycloakSession;
    protected final Identity identity;
    private final Map<String, List<String>> claims;
    private Attributes attributes;

    /** @param identity 评估主体 @param keycloakSession 会话 */
    public DefaultEvaluationContext(Identity identity, KeycloakSession keycloakSession) {
        this(identity, null, keycloakSession);
    }

    /** @param claims 额外请求 claims（多值） */
    public DefaultEvaluationContext(Identity identity, Map<String, List<String>> claims, KeycloakSession keycloakSession) {
        this.identity = identity;
        this.claims = claims;
        this.keycloakSession = keycloakSession;
    }

    /** @return 评估主体身份 */
    @Override
    public Identity getIdentity() {
        return identity;
    }

    /** 构建标准评估属性（时间、IP、UA、realm、claims、client id）。 */
    protected Map<String, Collection<String>> getBaseAttributes() {
        Map<String, Collection<String>> attributes = new HashMap<>();

        attributes.put("kc.time.date_time", Arrays.asList(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        attributes.put("kc.client.network.ip_address", Arrays.asList(this.keycloakSession.getContext().getConnection().getRemoteAddr()));
        attributes.put("kc.client.network.host", Arrays.asList(this.keycloakSession.getContext().getConnection().getRemoteHost()));

        List<String> userAgents = this.keycloakSession.getContext().getRequestHeaders().getRequestHeader("User-Agent");

        if (userAgents != null) {
            attributes.put("kc.client.user_agent", userAgents);
        }

        attributes.put("kc.realm.name", Arrays.asList(this.keycloakSession.getContext().getRealm().getName()));

        if (claims != null) {
            for (Entry<String, List<String>> entry : claims.entrySet()) {
                attributes.put(entry.getKey(), entry.getValue());
            }
        }

        if (KeycloakIdentity.class.isInstance(identity)) {
            AccessToken accessToken = KeycloakIdentity.class.cast(this.identity).getAccessToken();

            if (accessToken != null) {
                attributes.put("kc.client.id", Arrays.asList(accessToken.getIssuedFor()));
            }
        }

        return attributes;
    }

    /** @return 懒加载缓存的评估属性 */
    @Override
    public Attributes getAttributes() {
        if (attributes == null) {
            attributes = Attributes.from(getBaseAttributes());
        }
        return attributes;
    }
}
