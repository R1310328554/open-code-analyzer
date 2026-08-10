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
package org.keycloak.userprofile;

import java.util.Map;

import org.keycloak.models.UserModel;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.Validator;

/**
 * 用户资料属性校验上下文：扩展 {@link ValidationContext}，在校验 {@link UserProfile} 属性时提供
 * 对 {@link AttributeContext} 等用户资料相关信息的便捷访问。
 *
 * Extension of the {@link ValidationContext} used when validators are called for {@link UserProfile} attribute validation. Allows
 * easy access to UserProfile related bits, like {@link AttributeContext}
 * 
 * @author Vlastimil Elias <velias@redhat.com>
 *
 */
public class UserProfileAttributeValidationContext extends ValidationContext {

    /**
     * 在 {@link Validator} 实现中将 {@link ValidationContext} 安全转换为本类型。
     * Easy way to cast me from {@link ValidationContext} in {@link Validator} implementation
     */
    public static UserProfileAttributeValidationContext from(ValidationContext vc) {
        return (UserProfileAttributeValidationContext) vc;
    }
    
    /** 当前正在校验的属性上下文。 */
    private AttributeContext attributeContext;

    /** @param attributeContext 属性上下文 */
    public UserProfileAttributeValidationContext(AttributeContext attributeContext) {
        super(attributeContext.getSession());
        this.attributeContext = attributeContext;
    }

    /** @return 属性上下文 */
    public AttributeContext getAttributeContext() {
        return attributeContext;
    }

    @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = super.getAttributes();

        attributes.put(UserModel.class.getName(), getAttributeContext().getUser());

        return attributes;
    }
}