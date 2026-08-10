/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.organization.validation;

import org.keycloak.validate.BuiltinValidators;

/**
 * 组织相关字段校验工具类。
 * <p>当前提供组织重定向 URL 的 URI 格式校验。</p>
 */
public class OrganizationsValidation {
    /**
     * 校验组织重定向 URL 是否为合法 URI。
     * @param redirectUrl 待校验的重定向 URL
     * @throws OrganizationValidationException URL 无效时抛出
     */
    public static void validateUrl(String redirectUrl) {
        if (!BuiltinValidators.uriValidator().validate(redirectUrl).isValid()) {
            throw new OrganizationValidationException("Organization redirect URL is not valid.");
        }
    }

    /** 组织字段校验失败时抛出的运行时异常。 */
    public static class OrganizationValidationException extends RuntimeException {
        /** @param message 校验失败描述 */
        public OrganizationValidationException(String message) {
            super(message);
        }
    }
}
