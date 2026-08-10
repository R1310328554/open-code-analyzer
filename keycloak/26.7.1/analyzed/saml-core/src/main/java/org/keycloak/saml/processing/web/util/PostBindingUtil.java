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
package org.keycloak.saml.processing.web.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.GeneralConstants;

/**
 * SAML HTTP-POST 绑定编解码工具类。
 * <p>提供 Base64 编解码及 HTML 特殊字符转义，用于 POST 表单传输 SAML 消息。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since May 22, 2009
 */
public class PostBindingUtil {

    /** 日志记录器。 */
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * 对 SAML 消息字符串进行 Base64 编码。
     *
     * @param stringToEncode 待编码字符串
     *
     * @return Base64 编码结果
     */
    public static String base64Encode(String stringToEncode) throws IOException {
        return Base64.getEncoder().encodeToString(stringToEncode.getBytes(GeneralConstants.SAML_CHARSET));
    }

    /**
     * 对 Base64 编码字符串解码为字节数组。
     *
     * @param encodedString 已编码字符串
     *
     * @return 解码后的字节数组
     */
    public static byte[] base64Decode(String encodedString) {
        if (encodedString == null)
            throw logger.nullArgumentError("encodedString");

        try {
            return Base64.getMimeDecoder().decode(encodedString);
        } catch (Exception e) {
            logger.error(e);
            throw logger.invalidArgumentError("base64 decode failed: " + e.getMessage());
        }
    }

    /**
     * 解码 Base64 字符串并返回 {@link InputStream}。
     *
     * @param encodedString 已编码字符串
     *
     * @return 解码数据输入流
     */
    public static InputStream base64DecodeAsStream(String encodedString) {
        if (encodedString == null)
            throw logger.nullArgumentError("encodedString");

        return new ByteArrayInputStream(base64Decode(encodedString));
    }

    /**
     * 转义 HTML 中的双引号、尖括号等特殊字符，防止 XSS。
     *
     * @param toEscape 待转义字符串
     * @return 转义后的字符串
     */
    public static String escapeHTML(String toEscape) {
        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < toEscape.length(); i++) {
            char chr = toEscape.charAt(i);

            if (chr != '"' && chr != '<' && chr != '>') {
                escaped.append(chr);
            }
        }

        return escaped.toString();
    }
}
