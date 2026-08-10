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

package org.keycloak.email;

import java.util.Map;

import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * 邮件发送 SPI：按 realm SMTP 配置向用户或指定地址发送文本/HTML 邮件。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface EmailSenderProvider extends Provider {

    /** SMTP 配置项：是否允许 UTF-8 邮件头与正文。 */
    String CONFIG_ALLOW_UTF8 = "allowutf8";

    /** 向 {@link UserModel} 的邮箱地址发送邮件（委托 {@link #send(Map, String, String, String, String)}）。 */
    default void send(Map<String, String> config, UserModel user, String subject, String textBody, String htmlBody) throws EmailException {
        send(config, user.getEmail(), subject, textBody, htmlBody);
    }

    /**
     * 发送邮件。
     *
     * @param config realm SMTP 配置
     * @param address 收件人地址
     * @param subject 主题
     * @param textBody 纯文本正文（可为 {@code null}）
     * @param htmlBody HTML 正文（可为 {@code null}）
     */
    void send(Map<String, String> config, String address, String subject, String textBody, String htmlBody) throws EmailException;

    /**
     * 校验 SMTP 发送配置是否可用（如连通性、认证）。
     * @param config 待测试的配置
     * @throws EmailException 配置无效或连接失败时
     */
    void validate(Map<String, String> config) throws EmailException;
}
