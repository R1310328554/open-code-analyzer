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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.util.StringUtil;
import org.keycloak.saml.processing.api.util.DeflateUtil;

/**
 * SAML HTTP-Redirect 绑定编解码与 URL 构造工具类。
 * <p>支持 Deflate 压缩、Base64 编码、URL 编解码及目标查询字符串组装。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 14, 2009
 */
public class RedirectBindingUtil {

    /**
     * 使用 SAML 字符集对字符串进行 URL 编码。
     *
     * @param str 待编码字符串
     *
     * @return URL 编码结果
     *
     * @throws IOException 编码异常
     */
    public static String urlEncode(String str) throws IOException {
        return URLEncoder.encode(str, GeneralConstants.SAML_CHARSET_NAME);
    }

    /**
     * URL decode the string
     *
     * @param str
     *
     * @return
     *
     * @throws IOException
     */
    public static String urlDecode(String str) throws IOException {
        return URLDecoder.decode(str, GeneralConstants.SAML_CHARSET_NAME);
    }

    /**
     * On the byte array, apply base64 encoding
     *
     * @param stringToEncode
     *
     * @return
     *
     * @throws IOException
     */
    public static String base64Encode(byte[] stringToEncode) throws IOException {
        return Base64.getEncoder().encodeToString(stringToEncode);
    }

    /**
     * On the byte array, apply base64 encoding following by URL encoding
     *
     * @param stringToEncode
     *
     * @return
     *
     * @throws IOException
     */
    public static String base64URLEncode(byte[] stringToEncode) throws IOException {
        String base64Request = Base64.getEncoder().encodeToString(stringToEncode);
        return urlEncode(base64Request);
    }

    /**
     * On the byte array, apply URL decoding followed by base64 decoding
     *
     * @param encodedString
     *
     * @return
     *
     * @throws IOException
     */
    public static byte[] urlBase64Decode(String encodedString) throws IOException {
        String decodedString = urlDecode(encodedString);
        return Base64.getMimeDecoder().decode(decodedString);
    }

    /**
     * Apply deflate compression followed by base64 encoding and URL encoding
     *
     * @param stringToEncode
     *
     * @return
     *
     * @throws IOException
     */
    public static String deflateBase64URLEncode(String stringToEncode) throws IOException {
        return deflateBase64URLEncode(stringToEncode.getBytes(GeneralConstants.SAML_CHARSET));
    }

    /**
     * 对字节数组依次执行 Deflate 压缩、Base64 编码与 URL 编码（Redirect 绑定标准流程）。
     *
     * @param stringToEncode 待编码字节数组
     *
     * @return 编码后的 URL 安全字符串
     *
     * @throws IOException 压缩或编码异常
     */
    public static String deflateBase64URLEncode(byte[] stringToEncode) throws IOException {
        byte[] deflatedMsg = DeflateUtil.encode(stringToEncode);
        return base64URLEncode(deflatedMsg);
    }

    /**
     * Apply deflate compression followed by base64 encoding
     *
     * @param stringToEncode
     *
     * @return
     *
     * @throws IOException
     */
    public static String deflateBase64Encode(byte[] stringToEncode) throws IOException {
        byte[] deflatedMsg = DeflateUtil.encode(stringToEncode);
        return Base64.getEncoder().encodeToString(deflatedMsg);
    }

    /**
     * Apply URL decoding, followed by base64 decoding followed by deflate decompression
     *
     * @param encodedString
     *
     * @return
     *
     * @throws IOException
     */
    public static InputStream urlBase64DeflateDecode(String encodedString) throws IOException {
        return urlBase64DeflateDecode(encodedString, DeflateUtil.DEFAULT_MAX_INFLATING_SIZE);
    }

    /**
     * URL 解码 → Base64 解码 → Deflate 解压，返回输入流。
     *
     * @param encodedString 编码后的 SAML 参数字符串
     * @param maxInflatingSize 解压允许的最大字节数（防炸弹攻击）
     *
     * @return 解压后的 SAML 消息流
     *
     * @throws IOException 解码或解压异常
     */
    public static InputStream urlBase64DeflateDecode(String encodedString, long maxInflatingSize) throws IOException {
        byte[] deflatedString = urlBase64Decode(encodedString);
        return DeflateUtil.decode(deflatedString, maxInflatingSize);
    }

    /**
     * Base64 decode followed by Deflate decoding
     *
     * @param encodedString
     *
     * @return
     *
     * @throws IOException
     */
    public static InputStream base64DeflateDecode(String encodedString) throws IOException {
        return base64DeflateDecode(encodedString, DeflateUtil.DEFAULT_MAX_INFLATING_SIZE);
    }

    /**
     * Base64 decode followed by Deflate decoding
     *
     * @param encodedString
     * @param maxInflatingSize
     *
     * @return
     *
     * @throws IOException
     */
    public static InputStream base64DeflateDecode(String encodedString, long maxInflatingSize) throws IOException {
        byte[] base64decodedMsg = Base64.getMimeDecoder().decode(encodedString);
        return DeflateUtil.decode(base64decodedMsg, maxInflatingSize);
    }

    /**
     * 构造 Redirect 绑定的目标 URL 查询字符串。
     *
     * @param urlEncodedRequest 已 URL 编码的 SAML 请求/响应
     * @param urlEncodedRelayState 可选 RelayState
     * @param sendRequest true 时使用 SAMLRequest，false 时使用 SAMLResponse
     *
     * @return 查询参数字符串（不含前导 ?）
     */
    public static String getDestinationQueryString(String urlEncodedRequest, String urlEncodedRelayState, boolean sendRequest) {
        StringBuilder sb = new StringBuilder();
        if (sendRequest)
            sb.append("SAMLRequest=").append(urlEncodedRequest);
        else
            sb.append("SAMLResponse=").append(urlEncodedRequest);
        if (StringUtil.isNotNull(urlEncodedRelayState))
            sb.append("&RelayState=").append(urlEncodedRelayState);
        return sb.toString();
    }

    /**
     * Get the destination url
     *
     * @param holder
     *
     * @return
     *
     * @throws IOException
     */
    public static String getDestinationURL(RedirectBindingUtilDestHolder holder) throws IOException {
        String destination = holder.destination;
        StringBuilder destinationURL = new StringBuilder(destination);

        if (destination.contains("?"))
            destinationURL.append("&");
        else
            destinationURL.append("?");

        destinationURL.append(holder.destinationQueryString);

        return destinationURL.toString();
    }

    /**
     * Redirect 目标 URL 与查询字符串的持有者，支持链式配置。
     */
    public static class RedirectBindingUtilDestHolder {

        /** 目标主机 URL（不含查询串）。 */
        private String destination;
        /** 已编码的查询字符串。 */
        private String destinationQueryString;

        public RedirectBindingUtilDestHolder setDestinationQueryString(String dest) {
            destinationQueryString = dest;
            return this;
        }

        public RedirectBindingUtilDestHolder setDestination(String dest) {
            destination = dest;
            return this;
        }
    }
}
