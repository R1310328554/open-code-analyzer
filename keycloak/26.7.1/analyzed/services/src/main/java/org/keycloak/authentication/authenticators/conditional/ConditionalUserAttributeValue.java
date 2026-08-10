/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authentication.authenticators.conditional;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * 条件认证器：根据用户属性值是否匹配期望值决定子流程是否执行。
 * 支持正则匹配、组属性继承及结果取反。
 */
public class ConditionalUserAttributeValue implements ConditionalAuthenticator {

    /** 单例实例。 */
    static final ConditionalUserAttributeValue SINGLETON = new ConditionalUserAttributeValue();

    @Override
    /** 读取配置并比对用户（及可选组）属性，支持正则与取反。 */
    public boolean matchCondition(AuthenticationFlowContext context) {
        // 读取认证器配置
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        String attributeName = config.get(ConditionalUserAttributeValueFactory.CONF_ATTRIBUTE_NAME);
        String attributeValue = config.get(ConditionalUserAttributeValueFactory.CONF_ATTRIBUTE_EXPECTED_VALUE);
        boolean includeGroupAttributes = Boolean.parseBoolean(config.get(ConditionalUserAttributeValueFactory.CONF_INCLUDE_GROUP_ATTRIBUTES));
        boolean negateOutput = Boolean.parseBoolean(config.get(ConditionalUserAttributeValueFactory.CONF_NOT));
        boolean regexOutput = Boolean.parseBoolean(config.get(ConditionalUserAttributeValueFactory.REGEX));

        UserModel user = context.getUser();
        if (user == null) {
            throw new AuthenticationFlowException("Cannot find user for obtaining particular user attributes. Authenticator: " + ConditionalUserAttributeValueFactory.PROVIDER_ID, AuthenticationFlowError.UNKNOWN_USER);
        }

        boolean result = user.getAttributeStream(attributeName).anyMatch(attr -> regexOutput ? Pattern.compile(attributeValue).matcher(attr).matches() : Objects.equals(attr, attributeValue));
        if (!result && includeGroupAttributes) {
            result = KeycloakModelUtils.resolveAttribute(user, attributeName, true).stream().anyMatch(attr -> regexOutput ? Pattern.compile(attributeValue).matcher(attr).matches() : Objects.equals(attr, attributeValue));
        }
        return negateOutput != result;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // 未使用
    }

    @Override
    /** @return 属性校验需要已识别用户 */
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Not used
    }

    @Override
    public void close() {
        // 无资源需释放
    }
}
