/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.utils;

import java.net.IDN;
import java.util.Map;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.KeycloakSession;

/**
 * SMTP 邮件配置工具类。
 *
 * @author rmartinc
 */
public class SMTPUtil {

    private SMTPUtil() {
        // 静态工具类，禁止实例化
    }

    /**
     * 通过 {@link EmailSenderProvider} 校验 SMTP 配置。
     *
     * @param session Keycloak 会话
     * @param config SMTP 配置映射
     * @throws EmailException 配置无效时抛出
     */
    public static void checkSMTPConfiguration(KeycloakSession session, Map<String, String> config) throws EmailException {
        if (config == null || config.isEmpty()) {
            return;
        }

        final EmailSenderProvider sender = session.getProvider(EmailSenderProvider.class);
        sender.validate(config);
    }

    /**
     * 将邮箱域名部分转为 Punycode ASCII（{@link IDN#toASCII}），本地部分不变。
     *
     * @param email 原始邮箱地址
     * @return 转换后的邮箱；{@link IDN#toASCII} 异常时返回 null
     */
    public static String convertIDNEmailAddress(String email) {
        final int idx = email == null ? -1 : email.lastIndexOf('@');
        if (idx < 0) {
            return email;
        }
        try {
            return email.substring(0, idx) + '@' + IDN.toASCII(email.substring(idx + 1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
