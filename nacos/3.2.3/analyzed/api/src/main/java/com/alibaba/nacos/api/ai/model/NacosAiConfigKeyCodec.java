/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model;

import java.nio.charset.StandardCharsets;

/**
 * Nacos Config {@code dataId} / {@code group} 片段的可逆编解码工具。
 *
 * <p>与 Config Server {@code ParamUtils.isValid} 对齐：仅允许字母、数字及 {@code _ - . :}。
 * 非法字符串编码为 {@code enc.} + UTF-8 小写十六进制（不含 {@code __}），
 * 保证 {@code skill_{name}__{version}} 分组布局解析无歧义。</p>
 */
public final class NacosAiConfigKeyCodec {
    
    /** 编码片段前缀；各字符均符合 Nacos 配置参数规则。 */
    public static final String ENCODED_PREFIX = "enc.";
    
    private static final String DOUBLE_UNDERSCORE = "__";
    
    private NacosAiConfigKeyCodec() {
    }
    
    /** 判断字符是否允许出现在 Nacos dataId / group 中（{@code ParamUtils} 规则）。 */
    public static boolean isValidNacosConfigChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '-' || ch == '.' || ch == ':';
    }
    
    /** 判断字符串非空且每个字符均符合 Nacos 配置参数规则。 */
    public static boolean isValidNacosConfigParam(String s) {
        if (s == null) {
            return false;
        }
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!isValidNacosConfigChar(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 编码逻辑片段供 Nacos {@code dataId} 或其他键使用。
     * 若已合法则原样返回（含 null/空串）。
     */
    public static String encodeSegment(String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        if (!isValidNacosConfigParam(raw)) {
            return ENCODED_PREFIX + toHex(raw.getBytes(StandardCharsets.UTF_8));
        }
        return raw;
    }
    
    /**
     * 编码 Skill / AgentSpec <em>manifest</em> 组名片段（类型前缀后的单段）。
     * 类似 {@link #encodeSegment(String)}，但当名称含 {@code __} 时亦做十六进制包装，
     * 以便与 {@code name__version} 分隔布局区分。
     */
    public static String encodeManifestGroupNameSegment(String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        if (!isValidNacosConfigParam(raw) || raw.contains(DOUBLE_UNDERSCORE)) {
            return ENCODED_PREFIX + toHex(raw.getBytes(StandardCharsets.UTF_8));
        }
        return raw;
    }
    
    /**
     * 编码<em>带版本</em> Nacos group 中的名称或版本片段。
     * 始终使用 {@link #ENCODED_PREFIX} 加十六进制，避免片段内出现字面 {@code __}，
     * 按最后一个 {@code __} 拆分解析无歧义。
     */
    public static String encodeVersionedGroupSegment(String raw) {
        if (raw == null) {
            return null;
        }
        return ENCODED_PREFIX + toHex(raw.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 解码由 {@link #encodeSegment(String)} 产生的片段。
     * 若无 {@link #ENCODED_PREFIX} 前缀则原样返回。
     */
    public static String decodeSegment(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return encoded;
        }
        if (!encoded.startsWith(ENCODED_PREFIX)) {
            return encoded;
        }
        String hex = encoded.substring(ENCODED_PREFIX.length());
        if (hex.isEmpty()) {
            throw new IllegalArgumentException("empty payload after " + ENCODED_PREFIX);
        }
        return new String(fromHex(hex), StandardCharsets.UTF_8);
    }
    
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
    
    private static byte[] fromHex(String hex) {
        int len = hex.length();
        if ((len & 1) != 0) {
            throw new IllegalArgumentException("illegal hex length: " + len);
        }
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("illegal hex at index " + i);
            }
            out[i / 2] = (byte) ((hi << 4) + lo);
        }
        return out;
    }
}
