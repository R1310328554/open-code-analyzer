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
import java.util.Map;
import java.util.regex.Pattern;

import org.keycloak.common.util.ObjectUtil;
import org.keycloak.models.UserModel;
import org.keycloak.userprofile.AttributeContext;
import org.keycloak.userprofile.UserProfileAttributeValidationContext;
import org.keycloak.validate.SimpleValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidatorConfig;

import org.jboss.logging.Logger;

import static org.keycloak.common.util.ObjectUtil.isBlank;

/**
 * 校验匹配只读模式的属性值未被修改。
 * <p>通过 {@link #CFG_PATTERN} 指定属性名正则；输入为 {@code List<String>}。</p>
 *
 * @author Vlastimil Elias <velias@redhat.com>
 */
public class ReadOnlyAttributeUnchangedValidator implements SimpleValidator {

    private static final Logger logger = Logger.getLogger(ReadOnlyAttributeUnchangedValidator.class);

    /** 校验器 SPI ID。 */
    public static final String ID = "up-readonly-attribute-unchanged";

    /** 只读属性名 Pattern 配置键。 */
    public static final String CFG_PATTERN = "pattern";

    /** 拒绝修改只读属性时的错误消息键。 */
    public static String UPDATE_READ_ONLY_ATTRIBUTES_REJECTED_MSG = "updateReadOnlyAttributesRejectedMessage";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ValidationContext validate(Object input, String inputHint, ValidationContext context, ValidatorConfig config) {

        AttributeContext attributeContext = UserProfileAttributeValidationContext.from(context).getAttributeContext();
        Map.Entry<String, List<String>> attribute = attributeContext.getAttribute();
        String key = attribute.getKey();

        Pattern pattern = (Pattern) config.get(CFG_PATTERN);
        if (!pattern.matcher(key).find()) {
            return context;
        }

        @SuppressWarnings("unchecked")
        List<String> values = (List<String>) input;

        if (values == null) {
            return context;
        }

        UserModel user = attributeContext.getUser();
        String existingValue = user == null ? null : user.getFirstAttribute(key);

        String value = null;
        if (!values.isEmpty()) {
            value = values.get(0);
        }

        if (!isUnchanged(existingValue, value)) {
            logger.debugf("Attempt to edit denied for attribute '%s' with pattern '%s' of user '%s'", key, pattern, user == null ? "new user" : user.getFirstAttribute(UserModel.USERNAME));
            context.addError(new ValidationError(ID, key, UPDATE_READ_ONLY_ATTRIBUTES_REJECTED_MSG));
        }

        return context;
    }

    private boolean isUnchanged(String existingValue, String value) {
        if (existingValue == null && isBlank(value)) {
            // 用户尚无该属性且新值为空时通过校验
            return true;
        }

        return ObjectUtil.isEqualOrBothNull(existingValue, value);
    }

}
