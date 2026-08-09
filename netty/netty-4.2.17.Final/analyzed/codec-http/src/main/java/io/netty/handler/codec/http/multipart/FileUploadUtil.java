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
package io.netty.handler.codec.http.multipart;

import io.netty.handler.codec.http.HttpConstants;
import io.netty.util.internal.ObjectUtil;

/**
 * {@link FileUpload} 的 hashCode/equals/compareTo 及出站文件名校验工具类。
 */
final class FileUploadUtil {

    /** 工具类禁止实例化。 */
    private FileUploadUtil() { }

    /** 基于字段名（忽略大小写语义由 equals 保证）计算哈希。 */
    static int hashCode(FileUpload upload) {
        return upload.getName().hashCode();
    }

    /** 两上传项字段名忽略大小写相等则视为相同。 */
    static boolean equals(FileUpload upload1, FileUpload upload2) {
        return upload1.getName().equalsIgnoreCase(upload2.getName());
    }

    /** 按字段名忽略大小写排序比较。 */
    static int compareTo(FileUpload upload1, FileUpload upload2) {
        return upload1.getName().compareToIgnoreCase(upload2.getName());
    }

    /**
     * 出站（编码）文件名校验：拒绝控制字符、DEL、双引号与反斜杠。
     * @param filename The filename to check.
     * @return The validated filename, unchanged.
     */
    static String validateFileNameForMultiPart(String filename) {
        int length = ObjectUtil.checkNotNull(filename, "filename").length();
        for (int i = 0; i < length; i++) {
            char c = filename.charAt(i);
            if (c < HttpConstants.SP /* 控制字符 */ || c == HttpConstants.DEL ||
                    c == HttpConstants.DOUBLE_QUOTE || c == HttpConstants.BACKSLASH) {
                throw new IllegalArgumentException(
                        String.format("Illegal filename character 0x%02x at index %d", (int) c, i));
            }
        }
        return filename;
    }
}
