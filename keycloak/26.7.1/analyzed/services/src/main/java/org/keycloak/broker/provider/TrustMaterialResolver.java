/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.broker.provider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.keycloak.jose.jwk.JWK;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.IdentityBrokerService;
import org.keycloak.util.Strings;

/**
 * 信任材料解析器：按别名聚合多个 {@link TrustMaterialIdentityProvider} 的 JWK 公钥。
 * <p>用于客户端证明、OID4VCI 密钥证明等需复用 IdP 信任材料的场景。</p>
 */
public class TrustMaterialResolver {

    /**
     * 解析逗号分隔别名列表对应的 JWK 流。
     * @param session Keycloak 会话
     * @param aliases 逗号分隔的 IdP 别名
     * @param request 信任材料查询条件
     */
    public Stream<JWK> resolveKeys(KeycloakSession session, String aliases, TrustMaterialRequest request) {
        if (Strings.isEmpty(aliases)) {
            return Stream.empty();
        }
        return resolveKeys(session, splitAliases(aliases), request);
    }

    /** 解析别名集合对应的 JWK 流；跳过未启用或无法解析的 IdP。 */
    public Stream<JWK> resolveKeys(KeycloakSession session, Collection<String> aliases, TrustMaterialRequest request) {
        if (aliases == null || aliases.isEmpty()) {
            return Stream.empty();
        }

        return aliases.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(alias -> !alias.isEmpty())
                .map(alias -> resolveProvider(session, alias))
                .flatMap(Optional::stream)
                .flatMap(provider -> provider.resolveKeys(request));
    }

    /** 返回首个匹配的 JWK，无匹配时为空。 */
    public Optional<JWK> resolveKey(KeycloakSession session, String aliases, TrustMaterialRequest request) {
        return resolveKeys(session, aliases, request).findFirst();
    }

    /** 按别名查找已启用的 {@link TrustMaterialIdentityProvider}。 */
    private Optional<TrustMaterialIdentityProvider<?>> resolveProvider(KeycloakSession session, String alias) {
        IdentityProviderModel model = session.identityProviders().getByAlias(alias);
        if (model == null || !model.isEnabled()) {
            return Optional.empty();
        }

        TrustMaterialIdentityProvider<?> provider = IdentityBrokerService.getIdentityProvider(session, model, TrustMaterialIdentityProvider.class);
        return Optional.ofNullable(provider);
    }

    /** 将逗号分隔别名字符串拆分为去空白列表。 */
    private List<String> splitAliases(String aliases) {
        return Arrays.stream(aliases.split(","))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(alias -> !alias.isEmpty())
                .toList();
    }
}
