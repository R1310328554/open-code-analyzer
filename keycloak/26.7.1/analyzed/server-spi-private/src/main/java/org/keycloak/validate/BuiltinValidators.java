/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.validate;

import org.keycloak.validate.validators.DoubleValidator;
import org.keycloak.validate.validators.EmailValidator;
import org.keycloak.validate.validators.IntegerValidator;
import org.keycloak.validate.validators.IsoDateValidator;
import org.keycloak.validate.validators.LengthValidator;
import org.keycloak.validate.validators.LocalDateValidator;
import org.keycloak.validate.validators.NotBlankValidator;
import org.keycloak.validate.validators.NotEmptyValidator;
import org.keycloak.validate.validators.OptionsValidator;
import org.keycloak.validate.validators.PatternValidator;
import org.keycloak.validate.validators.UriValidator;
import org.keycloak.validate.validators.ValidatorConfigValidator;

/**
 * 内置校验器工厂：提供常用 {@link org.keycloak.validate.Validator} 单例访问方法。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class BuiltinValidators {

    /** @return 非空白校验器单例 */
    public static NotBlankValidator notBlankValidator() {
        return NotBlankValidator.INSTANCE;
    }

    /** @return 非空校验器单例 */
    public static NotEmptyValidator notEmptyValidator() {
        return NotEmptyValidator.INSTANCE;
    }

    /** @return 长度校验器单例 */
    public static LengthValidator lengthValidator() {
        return LengthValidator.INSTANCE;
    }

    /** @return URI 校验器单例 */
    public static UriValidator uriValidator() {
        return UriValidator.INSTANCE;
    }

    /** @return 邮箱格式校验器单例 */
    public static EmailValidator emailValidator() {
        return EmailValidator.INSTANCE;
    }

    /** @return 正则模式校验器单例 */
    public static PatternValidator patternValidator() {
        return PatternValidator.INSTANCE;
    }

    /** @return 双精度浮点数校验器单例 */
    public static DoubleValidator doubleValidator() {
        return DoubleValidator.INSTANCE;
    }

    /** @return 整数校验器单例 */
    public static IntegerValidator integerValidator() {
        return IntegerValidator.INSTANCE;
    }

    /** @return 本地化日期格式校验器单例 */
    public static LocalDateValidator dateValidator() {
        return LocalDateValidator.INSTANCE;
    }

    /** @return ISO 8601 日期格式校验器单例 */
    public static IsoDateValidator isoDateValidator() {
        return IsoDateValidator.INSTANCE;
    }

    /** @return 选项枚举校验器单例 */
    public static OptionsValidator optionsValidator() {
        return OptionsValidator.INSTANCE;
    }

    /** @return 校验器配置校验器单例 */
    public static ValidatorConfigValidator validatorConfigValidator() {
        return ValidatorConfigValidator.INSTANCE;
    }
}
