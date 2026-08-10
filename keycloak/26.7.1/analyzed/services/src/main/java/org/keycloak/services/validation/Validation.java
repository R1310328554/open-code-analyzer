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

package org.keycloak.services.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.keycloak.models.utils.FormMessage;
import org.keycloak.userprofile.ValidationException;
import org.keycloak.utils.EmailValidationUtil;

/**
 * 表单与注册字段校验工具类。
 */
public class Validation {

    /** 密码确认字段名 */
    public static final String FIELD_PASSWORD_CONFIRM = "password-confirm";
    /** 邮箱字段名 */
    public static final String FIELD_EMAIL = "email";
    /** 密码字段名 */
    public static final String FIELD_PASSWORD = "password";
    /** 用户名字段名 */
    public static final String FIELD_USERNAME = "username";
    /** TOTP 验证码字段名 */
    public static final String FIELD_OTP_CODE = "totp";
    /** TOTP 标签字段名 */
    public static final String FIELD_OTP_LABEL = "userLabel";

    /** 用户名允许字符：拉丁字母及通用字符 */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[\\p{IsLatin}|\\p{IsCommon}]+$");

    /** 向错误列表追加一条表单消息。 */
    private static void addError(List<FormMessage> errors, String field, String message, Object... parameters){
        errors.add(new FormMessage(field, message, parameters));
    }

    /**
     * 判断字符串是否为空（null 或长度为 0）。
     *
     * @param s 待检查字符串
     * @return 为空返回 true
     */
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
    
    /**
     * 判断字符串是否为空白（null、长度为 0 或仅含空白字符）。
     *
     * @param s 待检查字符串
     * @return 为空白返回 true
     */
    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    /** 校验邮箱格式是否合法。 */
    public static boolean isEmailValid(String email) {
        return EmailValidationUtil.isValidEmail(email);
    }

    /** 校验用户名是否仅含拉丁字母及通用字符。 */
    public static boolean isUsernameValid(String username) {

        return USERNAME_PATTERN.matcher(username).matches();
    }

    /** 将 {@link ValidationException} 错误列表转换为 {@link FormMessage} 列表。 */
    public static List<FormMessage> getFormErrorsFromValidation(List<ValidationException.Error> errors) {
        List<FormMessage> messages = new ArrayList<>();
        for (ValidationException.Error error : errors) {
            addError(messages, error.getAttribute(), error.getMessage(), error.getMessageParameters());
        }
        return messages;

    }
}
