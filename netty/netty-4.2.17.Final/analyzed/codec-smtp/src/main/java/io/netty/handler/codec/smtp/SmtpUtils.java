/*
 * Copyright 2016 The Netty Project
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
package io.netty.handler.codec.smtp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * SMTP 请求构造与参数校验的内部工具类。
 */
final class SmtpUtils {

    /** 将可变参数转为不可变列表；null 或空数组返回 {@link Collections#emptyList()}。 */
    static List<CharSequence> toUnmodifiableList(CharSequence... sequences) {
        if (sequences == null || sequences.length == 0) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(sequences));
    }

    /**
     * Validates SMTP parameters to prevent SMTP command injection.
     * Throws IllegalArgumentException if any parameter contains CRLF sequences.
     * <p>拒绝参数中的 {@code \r} / {@code \n}，防止构造额外 SMTP 命令行（命令注入）。</p>
     */
    static void validateSMTPParameters(CharSequence... parameters) {
        if (parameters != null) {
            for (CharSequence parameter : parameters) {
                if (parameter != null) {
                    validateSMTPParameter(parameter);
                }
            }
        }
    }

    /**
     * Validates SMTP parameters to prevent SMTP command injection.
     * Throws IllegalArgumentException if any parameter contains CRLF sequences.
     */
    static void validateSMTPParameters(List<CharSequence> parameters) {
        if (parameters != null) {
            for (CharSequence parameter : parameters) {
                if (parameter != null) {
                    validateSMTPParameter(parameter);
                }
            }
        }
    }

    /**
     * 单参数 CRLF 扫描；{@link String} 走 {@code indexOf} 快速路径。
     */
    private static void validateSMTPParameter(CharSequence parameter) {
        if (parameter instanceof String) {
            String paramStr = (String) parameter;
            if (paramStr.indexOf('\r') != -1 || paramStr.indexOf('\n') != -1) {
                throw new IllegalArgumentException("SMTP parameter contains CRLF characters: " + parameter);
            }
        } else {
            for (int i = 0; i < parameter.length(); i++) {
                char c = parameter.charAt(i);
                if (c == '\r' || c == '\n') {
                    throw new IllegalArgumentException("SMTP parameter contains CRLF characters: " + parameter);
                }
            }
        }
    }

    private SmtpUtils() { }
}
