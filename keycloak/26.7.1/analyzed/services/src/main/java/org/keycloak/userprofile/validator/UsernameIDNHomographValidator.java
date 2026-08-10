/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.userprofile.validator;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.validation.Validation;
import org.keycloak.validate.SimpleValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidatorConfig;

/**
 * 校验用户名不含 IDN 同形字符，仅允许拉丁字母及常见 Unicode 字符。
 * <p>适用于易受同形攻击的字段（如用户名）；输入为 {@code List<String>}。</p>
 *
 * @author Vlastimil Elias <velias@redhat.com>
 */
public class UsernameIDNHomographValidator implements SimpleValidator, ConfiguredProvider {

    /** 校验器 SPI ID。 */
    public static final String ID = "up-username-not-idn-homograph";

    /** 自定义错误消息键配置项。 */
    public static final String CFG_ERROR_MESSAGE = "error-message";

    /** 默认错误消息键。 */
    public static final String MESSAGE_NO_MATCH = "error-username-invalid-character";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(CFG_ERROR_MESSAGE);
        property.setLabel("Error message key");
        property.setHelpText("Key of the error message in i18n bundle. Default message key is " + MESSAGE_NO_MATCH);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ValidationContext validate(Object input, String inputHint, ValidationContext context, ValidatorConfig config) {
        @SuppressWarnings("unchecked")
        List<String> values = (List<String>) input;

        String value = null;

        if (values != null && !values.isEmpty()) {
            value = values.get(0);
        }

        if (!Validation.isBlank(value) && !Validation.isUsernameValid(value)) {
            context.addError(new ValidationError(ID, inputHint, getErrorMessageKey(inputHint, config)));
        }

        return context;
    }

    /** 解析配置中的错误消息键，未配置时使用 {@link #MESSAGE_NO_MATCH}。 */
    protected String getErrorMessageKey(String inputHint, ValidatorConfig config) {
        String cfg = config.getString(CFG_ERROR_MESSAGE);
        return (cfg != null && !cfg.isBlank()) ? cfg : MESSAGE_NO_MATCH;
    }

    /** @return 校验器帮助文本（英文，供管理控制台展示） */
    @Override
    public String getHelpText() {
        return "The field can contain only latin characters and common unicode characters. Useful for the fields, which can be subject of IDN homograph attacks (typically username).";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
