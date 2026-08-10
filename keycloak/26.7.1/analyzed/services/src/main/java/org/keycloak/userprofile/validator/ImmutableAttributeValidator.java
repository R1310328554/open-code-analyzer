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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.userprofile.AttributeContext;
import org.keycloak.userprofile.UserProfileAttributeValidationContext;
import org.keycloak.validate.SimpleValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidatorConfig;

import static org.keycloak.common.util.CollectionUtil.collectionEquals;
import static org.keycloak.validate.BuiltinValidators.notBlankValidator;

/**
 * 只读属性值被修改时校验失败。
 * <p>支持注册邮箱即用户名、首次登录默认值等例外。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ImmutableAttributeValidator implements SimpleValidator {

    /** 校验器 SPI ID。 */
    public static final String ID = "up-immutable-attribute";

    /** 默认错误消息键。 */
    private static final String DEFAULT_ERROR_MESSAGE = "error-user-attribute-read-only";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ValidationContext validate(Object input, String inputHint, ValidationContext context, ValidatorConfig config) {
        UserProfileAttributeValidationContext ac = (UserProfileAttributeValidationContext) context;
        AttributeContext attributeContext = ac.getAttributeContext();
        UserModel user = attributeContext.getUser();

        if (user == null) {
            return context;
        }

        Stream<String> rawValues = user.getAttributeStream(inputHint).filter(Objects::nonNull);

        // 用户名/邮箱转小写，避免外部存储大小写格式不一致导致误判
        if ((!user.isFederated() && UserModel.USERNAME.equals(inputHint)) || UserModel.EMAIL.equals(inputHint)) {
            rawValues = rawValues.map(String::toLowerCase);
        }

        List<String> currentValue = rawValues.collect(Collectors.toList());
        List<String> values = (List<String>) input;

        if (!collectionEquals(currentValue, values) && isReadOnly(attributeContext)) {
            if (currentValue.isEmpty() && !notBlankValidator().validate(values).isValid()) {
                return context;
            }

            // 首次登录且属性为空时，允许写入配置的默认值
            // 且新值与默认值一致
            if (currentValue.isEmpty() && isDefaultValueApplied(attributeContext, values)) {
                return context;
            }

            RealmModel realm = ac.getSession().getContext().getRealm();

            if (realm.isRegistrationEmailAsUsername()) {
                String attributeName = attributeContext.getMetadata().getName();

                if (UserModel.EMAIL.equals(attributeName)) {
                    return context;
                }

                List<String> email = attributeContext.getAttributes().get(UserModel.EMAIL);

                if (UserModel.USERNAME.equals(attributeName) && collectionEquals(values, email)) {
                    return context;
                }
            }

            context.addError(new ValidationError(ID, inputHint, DEFAULT_ERROR_MESSAGE));
        }

        return context;
    }

    private boolean isReadOnly(AttributeContext attributeContext) {
        return attributeContext.getMetadata().isReadOnly(attributeContext);
    }
    
    /** 判断提交值是否与元数据配置的默认值一致。 */
    private boolean isDefaultValueApplied(AttributeContext attributeContext, List<String> values) {
        // 检查是否配置了默认值
        String defaultValue = attributeContext.getMetadata().getDefaultValue();
        if (defaultValue == null) {
            return false;
        }
        
        // 比较当前值是否与默认值完全匹配
        return collectionEquals(values, List.of(defaultValue));
    }
}
