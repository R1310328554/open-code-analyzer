/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.handler.codec.http;

import io.netty.util.AsciiString;

/**
 * HTTP 状态码类别枚举（1xx 信息、2xx 成功、3xx 重定向、4xx 客户端错误、5xx 服务端错误）。
 * <p>
 * {@link #valueOf(int)} 通过快速除 100 映射到对应类别；未知码归为 {@link #UNKNOWN}。
 */
public enum HttpStatusClass {
    /** 1xx 信息性响应 */

    INFORMATIONAL(100, 200, "Informational"),
    /** 2xx 成功响应 */

    SUCCESS(200, 300, "Success"),
    /**
     * The redirection class (3xx)
     */
    REDIRECTION(300, 400, "Redirection"),
    /** 4xx 客户端错误 */

    CLIENT_ERROR(400, 500, "Client Error"),
    /** 5xx 服务端错误 */

    SERVER_ERROR(500, 600, "Server Error"),
    /**
     * The unknown class
     */
    UNKNOWN(0, 0, "Unknown Status") {
        @Override
        public boolean contains(int code) {
            return code < 100 || code >= 600;
        }
    };

    private static final HttpStatusClass[] statusArray = new HttpStatusClass[6];
    static {
        statusArray[1] = INFORMATIONAL;
        statusArray[2] = SUCCESS;
        statusArray[3] = REDIRECTION;
        statusArray[4] = CLIENT_ERROR;
        statusArray[5] = SERVER_ERROR;
    }

    /**
     * Returns the class of the specified HTTP status code.
     */
    public static HttpStatusClass valueOf(int code) {
        if (UNKNOWN.contains(code)) {
            return UNKNOWN;
        }
        return statusArray[fast_div100(code)];
    }

    /**
     * 快速整数除 100（魔数乘法实现，{@code dividend} 须 ≥ 0）。
     * @param dividend Must >= 0
     * @return dividend/100
     */
    private static int fast_div100(int dividend) {
        return (int) ((dividend * 1374389535L) >> 37);
    }

    /**
     * Returns the class of the specified HTTP status code.
     * @param code Just the numeric portion of the http status code.
     */
    public static HttpStatusClass valueOf(CharSequence code) {
        if (code != null && code.length() == 3) {
            char c0 = code.charAt(0);
            return isDigit(c0) && isDigit(code.charAt(1)) && isDigit(code.charAt(2)) ? valueOf(digit(c0) * 100)
                                                                                     : UNKNOWN;
        }
        return UNKNOWN;
    }

    private static int digit(char c) {
        return c - '0';
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private final int min;
    private final int max;
    private final AsciiString defaultReasonPhrase;

    HttpStatusClass(int min, int max, String defaultReasonPhrase) {
        this.min = min;
        this.max = max;
        this.defaultReasonPhrase = AsciiString.cached(defaultReasonPhrase);
    }

    /**
     * 判断给定状态码是否属于本类别（{@code min <= code < max}）。
     */
    public boolean contains(int code) {
        return code >= min && code < max;
    }

    /**
     * Returns the default reason phrase of this HTTP status class.
     */
    AsciiString defaultReasonPhrase() {
        return defaultReasonPhrase;
    }
}
