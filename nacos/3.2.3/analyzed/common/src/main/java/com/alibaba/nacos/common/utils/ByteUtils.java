/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.utils;

import java.nio.charset.StandardCharsets;

/**
 * 字节数组工具类：UTF-8 字符串与 byte[] 互转、空数组常量及空值判断。
 * ByteUtils.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class ByteUtils {
    
    private ByteUtils() {
    }
    
    /** 长度为 0 的字节数组常量 */
    public static final byte[] EMPTY = new byte[0];
    
    /**
     * 将字符串按 UTF-8 编码为字节数组；null 返回 {@link #EMPTY}。
     *
     * @param input 输入字符串
     * @return UTF-8 字节数组
     */
    public static byte[] toBytes(String input) {
        if (input == null) {
            return EMPTY;
        }
        return input.getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * 将对象 {@link String#valueOf(Object)} 后转为 UTF-8 字节；null 返回空数组。
     *
     * @param obj 任意对象
     * @return UTF-8 字节数组
     */
    public static byte[] toBytes(Object obj) {
        if (obj == null) {
            return EMPTY;
        }
        return toBytes(String.valueOf(obj));
    }
    
    /**
     * 将 UTF-8 字节数组解码为字符串；null 返回 {@link StringUtils#EMPTY}。
     *
     * @param bytes 字节数组
     * @return 解码后的字符串
     */
    public static String toString(byte[] bytes) {
        if (bytes == null) {
            return StringUtils.EMPTY;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    /** 判断字节数组是否为 null 或长度为 0 */
    public static boolean isEmpty(byte[] data) {
        return data == null || data.length == 0;
    }
    
    /** {@link #isEmpty(byte[])} 的逻辑取反 */
    public static boolean isNotEmpty(byte[] data) {
        return !isEmpty(data);
    }
    
}
