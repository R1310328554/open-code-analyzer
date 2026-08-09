/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注控制器方法所需权限的注解，由 {@link AuthorizationInterceptor} 在请求前校验。
 *
 * @author lkxiaolou
 * @since 1.7.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD})
public @interface AuthAction {

    /**
     * @return 所需权限类型
     */
    AuthService.PrivilegeType value();

    /**
     * @return 请求参数名，用于提取鉴权目标（如应用名）
     */
    String targetName() default "app";

    /**
     * @return 权限不足时返回给客户端的提示信息
     */
    String message() default "Permission denied";
}
