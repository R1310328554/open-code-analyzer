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

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.representations.idm.authorization.RegexPolicyRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;

import static org.keycloak.utils.JsonUtils.getJsonValue;
import static org.keycloak.utils.JsonUtils.hasPath;
import static org.keycloak.utils.JsonUtils.splitClaimPath;

/**
 * 正则表达式策略提供者：从身份或上下文属性中读取目标 claim 值，与策略配置的正则模式匹配则授予访问。
 *
 * @author <a href="mailto:yoshiyuki.tabata.jy@hitachi.com">Yoshiyuki Tabata</a>
 */
public class RegexPolicyProvider implements PolicyProvider {

    private static final Logger logger = Logger.getLogger(RegexPolicyProvider.class);
    /** 将 {@link Policy} 转为 {@link RegexPolicyRepresentation} 的函数 */
    private final BiFunction<Policy, AuthorizationProvider, RegexPolicyRepresentation> representationFunction;

    /**
     * @param representationFunction 策略表示转换函数
     */
    public RegexPolicyProvider(BiFunction<Policy, AuthorizationProvider, RegexPolicyRepresentation> representationFunction) {
        this.representationFunction = representationFunction;
    }

    /** 正则策略无额外资源需释放。 */
    @Override
    public void close() {
    }

    /**
     * 解析目标 claim 值并用策略正则全匹配，成功则 {@code grant()}。
     *
     * @param evaluation 当前授权评估上下文
     */
    @Override
    public void evaluate(Evaluation evaluation) {
        AuthorizationProvider authorizationProvider = evaluation.getAuthorizationProvider();
        RegexPolicyRepresentation policy = representationFunction.apply(evaluation.getPolicy(), authorizationProvider);
        String value = getClaimValue(evaluation, policy);

        if (value == null) {
            return;
        }

        Pattern pattern = Pattern.compile(policy.getPattern());
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            evaluation.grant();
            logger.debugf("policy %s evaluated with status %s on identity %s and claim value %s", policy.getName(), evaluation.getEffect(), evaluation.getContext().getIdentity().getId(), getClaimValue(evaluation, policy));
        }
    }

    /**
     * 根据 {@code targetContextAttributes} 从上下文或身份属性中解析目标 claim 值。
     */
    private String getClaimValue(Evaluation evaluation, RegexPolicyRepresentation policy) {
        Attributes attributes = policy.isTargetContextAttributes()
                ? evaluation.getContext().getAttributes()
                : evaluation.getContext().getIdentity().getAttributes();
        String targetClaim = policy.getTargetClaim();

        try {
            if (hasPath(targetClaim)) {
                return resolveJsonValue(attributes, targetClaim);
            }

            return resolveSimpleValue(attributes, targetClaim);
        } catch (IOException cause) {
            throw new RuntimeException("Failed to resolve value from claim: " + targetClaim, cause);
        }
    }

    /** 从属性中读取简单（非 JSON 路径）claim 的首个字符串值。 */
    private String resolveSimpleValue(Attributes attributes, String targetClaim) {
        Attributes.Entry value = attributes.getValue(targetClaim);

        if (value == null || value.isEmpty()) {
            return null;
        }

        return value.asString(0);
    }

    /** 解析 JSON 嵌套路径 claim（如 {@code user.address.city}）。 */
    private String resolveJsonValue(Attributes attributes, String targetClaim) throws IOException {
        List<String> paths = splitClaimPath(targetClaim);

        if (paths.isEmpty()) {
            return null;
        }

        Attributes.Entry attribute = attributes.getValue(paths.get(0));

        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        JsonNode node = JsonSerialization.readValue(attribute.asString(0), JsonNode.class);
        String path = String.join(".", paths.subList(1, paths.size()));

        return Optional.ofNullable(getJsonValue(node, path)).map(Object::toString).orElse(null);
    }
}
