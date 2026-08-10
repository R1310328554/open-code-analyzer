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

package org.keycloak.authentication;

import java.util.List;

import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.policy.MaxAuthAgePasswordPolicyProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.userprofile.ValidationException;
import org.keycloak.validate.ValidationError;

/**
 * {@link RequiredActionProvider} 工厂接口，提供管理控制台展示与 max_auth_age 等配置。
 *
 * Factory interface for {@link RequiredActionProvider RequiredActionProvider's}.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RequiredActionFactory extends ProviderFactory<RequiredActionProvider> {

    /** max_auth_age 配置项元数据。 */
    List<ProviderConfigProperty> MAX_AUTH_AGE_CONFIG_PROPERTIES = getMaxAuthAgePropertyConfig();

    /** 构建 max_auth_age 配置属性定义。 */
    static List<ProviderConfigProperty> getMaxAuthAgePropertyConfig() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(Constants.MAX_AUTH_AGE_KEY)
                .label("Maximum Age of Authentication")
                .helpText("Configures the duration in seconds this action can be used after the last authentication before the user is required to re-authenticate. " +
                        "This parameter is used just in the context of AIA when the kc_action parameter is available in the request, which is for instance when user " +
                        "himself updates his password in the account console.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(MaxAuthAgePasswordPolicyProviderFactory.DEFAULT_MAX_AUTH_AGE)
                .add()
                .build();
    }

    /**
     * 管理控制台中展示的名称。
     *
     * Display text used in admin console to reference this required action
     *
     * @return
     */
    String getDisplayText();

    /**
     * 相同情境（如相同 action token）下是否仅允许执行一次。
     *
     * Flag indicating whether the execution of the required action by the same circumstances
     * (e.g. by one and the same action token) should only be permitted once.
     * @return
     */
    default boolean isOneTimeAction() {
        return true;
    }

    /**
     * 是否可在管理 UI 中配置。
     *
     * Indicates whether this required action can be configured via the admin ui.
     * @return
     */
    default boolean isConfigurable() {
        List<ProviderConfigProperty> configMetadata = getConfigMetadata();
        return configMetadata != null && !configMetadata.isEmpty();
    }

    @Override
    default List<ProviderConfigProperty> getConfigMetadata() {
        return List.copyOf(MAX_AUTH_AGE_CONFIG_PROPERTIES);
    }

    /**
     * 校验必需操作配置；默认校验 max_auth_age 数值。
     * 无效时可抛出 {@link org.keycloak.models.ModelValidationException}。
     *
     * Allows users to validate the provided configuration for this required action. Users can throw a {@link org.keycloak.models.ModelValidationException} to indicate that the configuration is invalid.
     * Defaults validating max_auth_age value.
     *
     * @param session
     * @param realm
     * @param model
     */
    default void validateConfig(KeycloakSession session, RealmModel realm, RequiredActionConfigModel model) {
        if (model.getConfigValue(Constants.MAX_AUTH_AGE_KEY) == null) {
            return;
        }

        int parsedMaxAuthAge;
        try {
            parsedMaxAuthAge = parseMaxAuthAge(model);
        } catch (NumberFormatException ex) {
            throw new ValidationException(new ValidationError(getId(), Constants.MAX_AUTH_AGE_KEY, "error-invalid-value"));
        }

        if (parsedMaxAuthAge < 0) {
            throw new ValidationException(new ValidationError(getId(), Constants.MAX_AUTH_AGE_KEY, "error-number-out-of-range-too-small", 0));
        }
    }

    /** 从配置模型解析 max_auth_age 秒数。 */
    static int parseMaxAuthAge(RequiredActionConfigModel model) throws NumberFormatException {
        return Integer.parseInt(model.getConfigValue(Constants.MAX_AUTH_AGE_KEY));
    }
}
