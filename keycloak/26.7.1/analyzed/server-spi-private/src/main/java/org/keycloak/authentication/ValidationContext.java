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

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.utils.FormMessage;

/**
 * 表单校验上下文：继承 {@link FormContext}，通过 success() 或 validationError() 设置校验结果。
 *
 * Interface that encapsulates the current validation that is being performed.  Calling success() or validationError()
 * sets the status of this current validation.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ValidationContext extends FormContext {
    /**
     * 标记校验失败并携带表单回显数据与错误消息。
     *
     * Mark this validation as having a validation error
     *
     * @param formData form data you want to display when the form is refreshed
     * @param errors error messages to display on the form
     */
    void validationError(MultivaluedMap<String, String> formData, List<FormMessage> errors);

    /** 以单一错误消息标记校验失败。 */
    void error(String error);

    /**
     * 标记校验成功。
     *
     * Mark this validation as successful
     *
     */
    void success();

    /**
     * 仅展示本校验的错误，隐藏其他错误以防信息泄露（如 reCAPTCHA 防用户名枚举）。
     *
     * The error messages of this current validation will take precedence over any others. Other error messages will not
     * be shown. This is useful to prevent validation from leaking to an attacker. For example, the recaptcha validator
     * calls this method so that usernames cannot be phished
     */
    void excludeOtherErrors();
}
