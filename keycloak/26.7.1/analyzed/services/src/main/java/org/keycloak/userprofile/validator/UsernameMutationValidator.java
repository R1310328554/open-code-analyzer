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

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.userprofile.AttributeContext;
import org.keycloak.userprofile.Attributes;
import org.keycloak.userprofile.UserProfileAttributeValidationContext;
import org.keycloak.validate.SimpleValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidatorConfig;

/**
 * 校验用户名变更是否在领域策略允许范围内。
 * <p>领域禁止编辑用户名时拒绝修改；「注册邮箱即用户名」下邮箱同步导致的用户名变更除外。</p>
 *
 * @author Vlastimil Elias <velias@redhat.com>
 */
public class UsernameMutationValidator implements SimpleValidator {

    /** 校验器 SPI ID。 */
    public static final String ID = "up-username-mutation";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ValidationContext validate(Object input, String inputHint, ValidationContext context, ValidatorConfig config) {
        @SuppressWarnings("unchecked")
        List<String> values = (List<String>) input;

        if (values.isEmpty()) {
            return context;
        }

        String value = values.get(0);

        if (Validation.isBlank(value)) {
            return context;
        }

        AttributeContext attributeContext = UserProfileAttributeValidationContext.from(context).getAttributeContext();
        UserModel user = attributeContext.getUser();
        RealmModel realm = context.getSession().getContext().getRealm();

        String valueLowercased = value.toLowerCase();
        if (!realm.isEditUsernameAllowed() && user != null && !valueLowercased.equals(user.getFirstAttribute(UserModel.USERNAME))) {
            Attributes attributes = attributeContext.getAttributes();
            if (realm.isRegistrationEmailAsUsername() && valueLowercased.equals(attributes.getFirst(UserModel.EMAIL))) {
                // 注册邮箱即用户名场景下，用户名随邮箱规范化而变更，更新 Profile 时不应拦截
                return context;
            }
            context.addError(new ValidationError(ID, inputHint, Messages.READ_ONLY_USERNAME));
        }
        return context;
    }

}
