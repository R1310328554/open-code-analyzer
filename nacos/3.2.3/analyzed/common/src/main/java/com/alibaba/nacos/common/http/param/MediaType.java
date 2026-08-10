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

package com.alibaba.nacos.common.http.param;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Http Media type.
 * <p>HTTP Content-Type 常量与解析工具：预置 JSON、表单、multipart 等常用 MIME，并提供 {@link #valueOf(String)} 从 Content-Type 头解析 type 与 charset。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class MediaType {
    
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";
    
    /** 表单 URL 编码类型，与 {@link DefaultHttpClientRequest} 表单分支配合 */
    public static final String APPLICATION_FORM_URLENCODED =
        "application/x-www-form-urlencoded;charset=UTF-8";
    
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    
    public static final String APPLICATION_SVG_XML = "application/svg+xml";
    
    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";
    
    public static final String APPLICATION_XML = "application/xml;charset=UTF-8";
    
    /** 默认 JSON 类型，含 UTF-8 charset */
    public static final String APPLICATION_JSON = "application/json;charset=UTF-8";
    
    public static final String MULTIPART_FORM_DATA = "multipart/form-data;charset=UTF-8";
    
    public static final String TEXT_HTML = "text/html;charset=UTF-8";
    
    public static final String TEXT_PLAIN = "text/plain;charset=UTF-8";
    
    private MediaType(String type, String charset) {
        this.type = type;
        this.charset = charset;
    }
    
    /** MIME 主类型（不含 charset 参数） */
    private final String type;
    
    /** 从 Content-Type 解析或指定的字符集 */
    private final String charset;
    
    /**
     * Parse the given String contentType into a {@code MediaType} object.
     * <p>按分号拆分 Content-Type，提取 {@code charset=} 参数，缺省为平台默认编码。</p>
     *
     * @param contentType mediaType
     * @return MediaType
     */
    public static MediaType valueOf(String contentType) {
        if (StringUtils.isEmpty(contentType)) {
            throw new IllegalArgumentException("MediaType must not be empty");
        }
        String[] values = contentType.split(";");
        String charset = Constants.ENCODE;
        for (String value : values) {
            if (value.startsWith("charset=")) {
                charset = value.substring("charset=".length());
            }
        }
        return new MediaType(values[0], charset);
    }
    
    /**
     * Use the given contentType and charset to assemble into a {@code MediaType} object.
     *
     * @param contentType contentType
     * @param charset charset
     * @return MediaType
      * <p>MIME 类型常量与解析；详见类级说明。</p>
     */
    public static MediaType valueOf(String contentType, String charset) {
        if (StringUtils.isEmpty(contentType)) {
            throw new IllegalArgumentException("MediaType must not be empty");
        }
        String[] values = contentType.split(";");
        return new MediaType(values[0], StringUtils.isEmpty(charset) ? Constants.ENCODE : charset);
    }
    
    public String getType() {
        return type;
    }
    
    public String getCharset() {
        return charset;
    }
    
    @Override
    public String toString() {
        return type + ";charset=" + charset;
    }
}
