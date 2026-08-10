/*
 *  Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.policy.provider.regex;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.Config.Scope;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.RegexPolicyRepresentation;

/**
 * 正则（regex）策略类型的 {@link PolicyProviderFactory}，管理目标 claim、正则模式及上下文属性来源配置。
 *
 * @author <a href="mailto:yoshiyuki.tabata.jy@hitachi.com">Yoshiyuki Tabata</a>
 */
public class RegexPolicyProviderFactory implements PolicyProviderFactory<RegexPolicyRepresentation> {

    private RegexPolicyProvider provider = new RegexPolicyProvider(this::toRepresentation);

    @Override
    public PolicyProvider create(KeycloakSession session) {
        return provider;
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** 策略类型标识符。 */
    @Override
    public String getId() {
        return "regex";
    }

    /** 管理控制台显示名称。 */
    @Override
    public String getName() {
        return "Regex";
    }

    /** 策略分组：基于身份。 */
    @Override
    public String getGroup() {
        return "Identity Based";
    }

    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        return provider;
    }

    /** 从策略配置映射为 {@link RegexPolicyRepresentation}。 */
    @Override
    public RegexPolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        RegexPolicyRepresentation representation = new RegexPolicyRepresentation();
        Map<String, String> config = policy.getConfig();

        representation.setTargetClaim(config.get("targetClaim"));
        representation.setPattern(config.get("pattern"));
        representation.setTargetContextAttributes(Boolean.parseBoolean(config.get("targetContextAttributes")));

        return representation;
    }

    @Override
    public Class<RegexPolicyRepresentation> getRepresentationType() {
        return RegexPolicyRepresentation.class;
    }

    @Override
    public void onCreate(Policy policy, RegexPolicyRepresentation representation, AuthorizationProvider authorization) {
        updatePolicy(policy, representation);
    }

    @Override
    public void onUpdate(Policy policy, RegexPolicyRepresentation representation, AuthorizationProvider authorization) {
        updatePolicy(policy, representation);
    }

    /** 导入时直接复制原始配置映射。 */
    @Override
    public void onImport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        policy.setConfig(representation.getConfig());
    }

    /** 将表示对象字段写回策略配置。 */
    private void updatePolicy(Policy policy, RegexPolicyRepresentation representation) {
        Map<String, String> config = new HashMap<>(policy.getConfig());

        config.put("targetClaim", representation.getTargetClaim());
        config.put("pattern", representation.getPattern());
        config.put("targetContextAttributes", String.valueOf(representation.isTargetContextAttributes()));

        policy.setConfig(config);
    }
}
