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

package org.keycloak.common.util;


import java.util.Base64;

/**
 * URL 安全的 Base64 编解码工具（无填充 {@code =}）。
 *
 * <p>基于 {@link java.util.Base64} 的 URL 编码器，并提供标准 Base64 与 Base64Url 字符串互转。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Base64Url {
  
    // 仅初始化一次，避免工厂方法重复创建编码器
    public static final Base64.Encoder BASE64_URL_ENCODER_WITHOUT_PADDING = Base64.getUrlEncoder().withoutPadding();
    
    /** 将字节数组编码为 Base64Url 字符串（无填充）。 */
    public static String encode(byte[] bytes) {
        return BASE64_URL_ENCODER_WITHOUT_PADDING.encodeToString(bytes);
    }

    /** 解码 Base64Url 字符串；部分调用方传入标准 Base64，内部会先规范化。 */
    public static byte[] decode(String s) {
        // some places invoke this without a Base64 url encoding! ugh!
        return Base64.getUrlDecoder().decode(encodeBase64ToBase64Url(s));
    }


    /**
     * 将标准 Base64 字符串转换为 Base64Url 格式。
     *
     * @param base64 标准 Base64 编码字符串
     * @return Base64Url 编码字符串
     */
    public static String encodeBase64ToBase64Url(String base64) {
        String s = base64.split("=")[0]; // Remove any trailing '='s
        s = s.replace('+', '-'); // 62nd char of encoding
        s = s.replace('/', '_'); // 63rd char of encoding
        return s;
    }


    /**
     * 将 Base64Url 字符串转换为标准 Base64 格式（已废弃）。
     *
     * @param base64Url Base64Url 编码字符串
     * @return 标准 Base64 编码字符串
     */
    @Deprecated
    public static String encodeBase64UrlToBase64(String base64Url) {
        String s = base64Url.replace('-', '+'); // 62nd char of encoding
        s = s.replace('_', '/'); // 63rd char of encoding
        switch (s.length() % 4) // Pad with trailing '='s
        {
            case 0:
                break; // No pad chars in this case
            case 2:
                s += "==";
                break; // Two pad chars
            case 3:
                s += "=";
                break; // One pad char
            default:
                throw new RuntimeException(
                        "Illegal base64url string!");
        }

        return s;
    }


}
