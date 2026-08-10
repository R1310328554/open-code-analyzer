/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.userprofile;

import java.util.Map;
import java.util.Objects;

import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.Validator;
import org.keycloak.validate.ValidatorConfig;
import org.keycloak.validate.Validators;

/**
 * 用户配置属性校验器元数据：绑定校验器 ID 与配置，并执行校验。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 * @author Vlastimil Elias <velias@redhat.com>
 */
public final class AttributeValidatorMetadata {

    /** 校验器标识符。 */
    private final String validatorId;
    /** 校验器配置。 */
    private final ValidatorConfig validatorConfig;

    /** 仅指定校验器 ID（空配置）。
     * @param validatorId 校验器 ID */
    public AttributeValidatorMetadata(String validatorId) {
        this.validatorId = validatorId;
        this.validatorConfig = ValidatorConfig.configFromMap(null);
    }

    /** 指定校验器 ID 与配置。
     * @param validatorId 校验器 ID
     * @param validatorConfig 配置 */
    public AttributeValidatorMetadata(String validatorId, ValidatorConfig validatorConfig) {
        this.validatorId = validatorId;
        this.validatorConfig = validatorConfig;
    }

    /**
     * 供 GUI 收集校验配置以支持客户端动态校验。
     * 
     * @return the validatorId
     */
    public String getValidatorId() {
        return validatorId;
    }
    
    /**
     * 以 Map 形式返回校验器配置。
     * 
     * @return never null
     */
    public Map<String, Object> getValidatorConfig(){
        return validatorConfig.asMap();
    }
    
    /**
     * 对给定 {@link AttributeContext} 执行校验。
     * 
     * @param context to validate
     * @return context containing errors if any found
     */
    public ValidationContext validate(AttributeContext context) {

        Validator validator = Validators.validator(context.getSession(), validatorId);
        if (validator == null) {
            throw new RuntimeException("No validator with id " + validatorId + " found to validate UserProfile attribute " + context.getMetadata().getName() + " in realm " + context.getSession().getContext().getRealm().getName());
        }

        return validator.validate(context.getAttribute().getValue(), context.getMetadata().getName(), new UserProfileAttributeValidationContext(context), validatorConfig);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (! (o instanceof AttributeValidatorMetadata)) return false;
        AttributeValidatorMetadata other = (AttributeValidatorMetadata) o;
        return Objects.equals(getValidatorId(), other.getValidatorId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(validatorId);
    }
}
