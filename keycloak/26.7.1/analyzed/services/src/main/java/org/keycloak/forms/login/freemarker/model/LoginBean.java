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
package org.keycloak.forms.login.freemarker.model;

import jakarta.ws.rs.core.MultivaluedMap;

/**
 * 登录表单 FreeMarker Bean：从 POST 表单数据暴露用户名、密码与记住我等字段。
 * <p>供 login.ftl 等模板回显用户输入或验证失败后的表单值。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LoginBean {

    /** 用户名字段值。 */
    private String username;

    /** 密码字段值。 */
    private String password;

    /** 密码令牌（如 WebAuthn/OTP 流程）。 */
    private String passwordToken;

    /** “记住我”复选框提交值。 */
    private String rememberMe;

    /** @param formData 登录 POST 表单参数映射 */
    public LoginBean(MultivaluedMap<String, String> formData){
        if (formData != null) {
            username = formData.getFirst("username");
            password = formData.getFirst("password");
            passwordToken = formData.getFirst("password-token");
            rememberMe = formData.getFirst("rememberMe");
        }
    }

    /** @return 用户名 */
    public String getUsername() {
        return username;
    }

    /** @return 密码 */
    public String getPassword() {
        return password;
    }

    /** @return 密码令牌 */
    public String getPasswordToken() {
        return passwordToken;
    }

    /** @return “记住我”提交值 */
    public String getRememberMe() {
        return rememberMe;
    }
}
