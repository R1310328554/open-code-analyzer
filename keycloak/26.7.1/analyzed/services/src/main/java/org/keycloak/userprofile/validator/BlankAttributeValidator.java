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

import java.util.List;

import org.keycloak.services.validation.Validation;
import org.keycloak.userprofile.AttributeContext;
import org.keycloak.userprofile.UserProfileAttributeValidationContext;
import org.keycloak.validate.SimpleValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidatorConfig;
import org.keycloak.validate.ValidatorConfig.ValidatorConfigBuilder;

/**
 * 校验用户 Profile 属性值非空白（null 可接受，取决于配置）。
 * <p>输入为 {@code List<String>}；非必填属性跳过。</p>
 *
 * @author Vlastimil Elias <velias@redhat.com>
 */
public class BlankAttributeValidator implements SimpleValidator {

    /** 校验器 SPI ID。 */
    public static final String ID = "up-blank-attribute-value";

    /** 自定义错误消息键配置项。 */
    public static final String CFG_ERROR_MESSAGE = "error-message";

    /** 为 true 时 null 也视为校验失败。 */
    public static final String CFG_FAIL_ON_NULL = "fail-on-null";
    
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ValidationContext validate(Object input, String inputHint, ValidationContext context, ValidatorConfig config) {
        @SuppressWarnings("unchecked")
        List<String> values = (List<String>) input;

        boolean failOnNull = config.getBooleanOrDefault(CFG_FAIL_ON_NULL, false);
        
        if (values.isEmpty() && !failOnNull) {
            return context;
        }

        AttributeContext attributeContext = UserProfileAttributeValidationContext.from(context).getAttributeContext();

        if (!attributeContext.getMetadata().isRequired(attributeContext)) {
            return context;
        }

        String value = values.isEmpty() ? null: values.get(0);

        if ((failOnNull || value != null) && Validation.isBlank(value)) {
            context.addError(new ValidationError(ID, inputHint, config.getStringOrDefault(CFG_ERROR_MESSAGE, AttributeRequiredByMetadataValidator.ERROR_USER_ATTRIBUTE_REQUIRED)));
        }

        return context;
    }

    /**
     * 构建校验器配置。
     *
     * @param errorMessage 失败时的 i18n 消息键
     * @param failOnNull 是否在 null 时也失败（默认仅空字符串）
     * @return ValidatorConfig
     */
    public static ValidatorConfig createConfig(String errorMessage, boolean failOnNull) {
        ValidatorConfigBuilder builder = ValidatorConfig.builder();
        builder.config(CFG_FAIL_ON_NULL, failOnNull);
        if (errorMessage != null) {
            builder.config(CFG_ERROR_MESSAGE, errorMessage);
        }
        return builder.build();
    }

}
