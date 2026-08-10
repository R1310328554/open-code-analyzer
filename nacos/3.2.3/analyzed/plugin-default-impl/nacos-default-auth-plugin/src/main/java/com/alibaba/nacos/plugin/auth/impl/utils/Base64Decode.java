/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.utils;

import java.util.Arrays;

/**
 * Base64 解码工具类（无第三方依赖）。
 *
 * <p>支持标准 Base64 字母表、填充符 {@code =} 及 76 字符换行分隔； 非法字符将抛出 {@link IllegalArgumentException}。</p>
 *
 * @author xYohn
 * @date 2023/8/7
 */
public class Base64Decode {
    
    private static final char[] BASE64_ALPHABET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    
    private static final int[] BASE64_IALPHABET = new int[256];
    
    private static final int IALPHABET_MAX_INDEX = BASE64_IALPHABET.length - 1;
    
    private static final int[] IALPHABET = BASE64_IALPHABET;
    
    static {
        Arrays.fill(BASE64_IALPHABET, -1);
        for (int i = 0, iS = BASE64_ALPHABET.length; i < iS; i++) {
            BASE64_IALPHABET[BASE64_ALPHABET[i]] = i;
        }
        BASE64_IALPHABET['='] = 0;
    }
    
    /**
     * 将 Base64 编码字符串解码为新分配的字节数组。
     *
     * @param input the string to decode
     * @return a byte array containing binary data
     */
    public static byte[] decode(String input) {
        
        // 空输入直接返回空数组
        if (input == null || input.equals("")) {
            return new byte[0];
        }
        char[] sArr = input.toCharArray();
        int sLen = sArr.length;
        if (sLen == 0) {
            return new byte[0];
        }
        
        int sIx = 0;
        // 裁剪非法字符后的起止下标
        int eIx = sLen - 1;
        
        // 跳过首部非法 Base64 字符
        while (sIx < eIx && IALPHABET[sArr[sIx]] < 0) {
            sIx++;
        }
        
        // 跳过尾部非法 Base64 字符
        while (eIx > 0 && IALPHABET[sArr[eIx]] < 0) {
            eIx--;
        }
        
        // 统计末尾填充符 {@code =} 个数（0/1/2）
        // 根据末尾 {@code =} 判断填充长度
        int pad = sArr[eIx] == '=' ? (sArr[eIx - 1] == '=' ? 2 : 1) : 0;
        // 有效字符数（含可能的换行分隔符）
        int cCnt = eIx - sIx + 1;
        int sepCnt = sLen > 76 ? (sArr[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;
        // 计算解码后的字节长度
        int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
        // 预分配精确长度的结果数组
        byte[] dArr = new byte[len];
        
        // 批量解码除最后 0～2 字节外的全部内容
        int d = 0;
        int three = 3;
        int eight = 8;
        for (int cc = 0, eLen = (len / three) * three; d < eLen;) {
            
            // 四个合法字符拼成一个 24 位整数
            int i = ctoi(sArr[sIx++]) << 18 | ctoi(sArr[sIx++]) << 12 | ctoi(sArr[sIx++]) << 6
                | ctoi(sArr[sIx++]);
            
            // 拆出三个字节写入结果数组
            dArr[d++] = (byte) (i >> 16);
            dArr[d++] = (byte) (i >> 8);
            dArr[d++] = (byte) i;
            
            // 遇到 76 字符换行则跳过 \r\n
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }
        
        if (d < len) {
            // 处理末尾带填充的最后 1～3 字节
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) {
                i |= ctoi(sArr[sIx++]) << (18 - j * 6);
            }
            
            for (int r = 16; d < len; r -= eight) {
                dArr[d++] = (byte) (i >> r);
            }
        }
        
        return dArr;
    }
    
    /** 将 Base64 字符映射为 6 位索引值，非法字符抛异常。 */
    private static int ctoi(char c) {
        int i = c > IALPHABET_MAX_INDEX ? -1 : IALPHABET[c];
        if (i < 0) {
            String msg = "Illegal base64 character: '" + c + "'";
            throw new IllegalArgumentException(msg);
        }
        return i;
    }
    
}
