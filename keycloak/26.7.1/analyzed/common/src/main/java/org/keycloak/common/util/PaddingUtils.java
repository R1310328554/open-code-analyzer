/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.common.util;

/**
 * 字符串尾部填充工具（用于固定长度缓冲区等场景）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PaddingUtils {

    private static final char PADDING_CHAR_NONE = '\u0000';

    /**
     * 将字符串填充至至少 {@code maxPaddingLength} 字符；不足时用 {@code \\0} 补齐，已足够长则原样返回。
     *
     * @param rawString 原始字符串
     * @param maxPaddingLength 目标最小长度
     * @return 填充后的字符串
     */
    public static String padding(String rawString, int maxPaddingLength) {
        if (rawString.length() < maxPaddingLength) {
            int nPad = maxPaddingLength - rawString.length();
            StringBuilder result = new StringBuilder(rawString);
            for (int i = 0 ; i < nPad; i++) result.append(PADDING_CHAR_NONE);
            return result.toString();
        } else
            return rawString;
    }
}
