/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 基础数据类型转换工具：Long 与字节数组、整型位操作等。
 */
public class DataConverter {
    /** UTF-8 字符集常量。 */
    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    /** 将 Long 转为 8 字节大端数组。 */
    public static byte[] Long2Byte(Long v) {
        ByteBuffer tmp = ByteBuffer.allocate(8);
        tmp.putLong(v);
        return tmp.array();
    }

    /** 设置或清除 value 的第 index 位。 */
    public static int setBit(int value, int index, boolean flag) {
        if (flag) {
            return (int) (value | (1L << index));
        } else {
            return (int) (value & ~(1L << index));
        }
    }

    /** 读取 value 的第 index 位是否为 1。 */
    public static boolean getBit(int value, int index) {
        return (value & (1L << index)) != 0;
    }
}
