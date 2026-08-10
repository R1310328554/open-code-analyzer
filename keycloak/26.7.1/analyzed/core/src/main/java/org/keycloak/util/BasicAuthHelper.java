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

package org.keycloak.util;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HTTP Basic 认证辅助工具，默认实现符合
 * <a href="https://datatracker.ietf.org/doc/html/rfc2617">RFC 2617</a>。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class BasicAuthHelper {

    /**
     * 根据用户名与密码生成 Basic 认证请求头值。
     *
     * @param username 用户名
     * @param password 密码
     * @return {@code Basic <Base64(username:password)>} 格式的头值
     */
    public static String createHeader(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ':' + password).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从 Authorization 头解析用户名与密码。
     *
     * @param header Authorization 头的完整值
     * @return 长度为 2 的数组 [username, password]，解析失败返回 null
     */
    public static String[] parseHeader(String header) {
        if (header.length() < 6) return null;

        String type = header.substring(0, 5);
        type = type.toLowerCase();
        if (!type.equalsIgnoreCase("Basic")) return null;

        String val;
        try {
            val = new String(Base64.getMimeDecoder().decode(header.substring(6)));
        } catch (IllegalArgumentException e) {
            return null;
        }

        int separatorIndex = val.indexOf(":");
        if (separatorIndex == -1) return null;

        String username = val.substring(0, separatorIndex);
        String password = val.substring(separatorIndex + 1);

        return new String[]{ username, password };
    }

    /**
     * 符合 <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-2.3.1">RFC 6749</a>
     * 的 Basic 认证实现，对用户名与密码做 URL 编码/解码。
     */
    public static abstract class RFC6749 {

        /** 生成经 URL 编码的 Basic 认证头。 */
        public static String createHeader(String username, String password) {
            try {
                return BasicAuthHelper.createHeader(
                    URLEncoder.encode(username, "UTF-8"),
                    URLEncoder.encode(password, "UTF-8")
                );
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }

        /** 解析 Basic 认证头并对用户名、密码做 URL 解码。 */
        public static String[] parseHeader(String header) {
            String[] val = BasicAuthHelper.parseHeader(header);
            if (null == val) {
                return null;
            }

            try {
                return new String[]{
                    URLDecoder.decode(val[0], "UTF-8"),
                    URLDecoder.decode(val[1], "UTF-8")
                };
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
    }
}
