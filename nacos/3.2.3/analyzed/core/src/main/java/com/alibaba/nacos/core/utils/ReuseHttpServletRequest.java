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

package com.alibaba.nacos.core.utils;

import com.alibaba.nacos.common.http.HttpUtils;
import com.alibaba.nacos.common.http.param.MediaType;
import com.alibaba.nacos.common.utils.ByteUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * {@link HttpServletRequestWrapper} 实现：构造时缓存 body 与去重后的参数 Map，支持多次读取。
 * httprequest wrapper.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ReuseHttpServletRequest extends HttpServletRequestWrapper implements ReuseHttpRequest {
    
    /** 原始请求对象，用于读取 Content-Type 等元数据。 */
    private final HttpServletRequest target;
    
    /** 请求体字节缓存，供 getInputStream/getBody 重复消费。 */
    private byte[] body;
    
    private Map<String, String[]> stringMap;
    
    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
      * <p>请求体缓存包装器；详见类级说明。</p>
     */
    public ReuseHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.target = request;
        // 构造时一次性读尽 InputStream 并去重参数
        this.body = toBytes(request.getInputStream());
        this.stringMap = toDuplication(request);
    }
    
    @Override
    public Object getBody() throws Exception {
        // multipart 表单直接返回 parts，否则解析 body 或表单参数
        if (StringUtils.containsIgnoreCase(target.getContentType(),
            MediaType.MULTIPART_FORM_DATA)) {
            return target.getParts();
        } else {
            String s = ByteUtils.toString(body);
            if (StringUtils.isBlank(s)) {
                return HttpUtils
                    .encodingParams(HttpUtils.translateParameterMap(stringMap),
                        StandardCharsets.UTF_8.name());
            }
            return s;
        }
    }
    
    /** 将输入流完整读入字节数组。 */
    private byte[] toBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int n = 0;
        while ((n = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, n);
        }
        return bos.toByteArray();
    }
    
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
    
    @Override
    public Map<String, String[]> getParameterMap() {
        return stringMap;
    }
    
    @Override
    public String getParameter(String name) {
        String[] values = stringMap.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }
    
    @Override
    public String[] getParameterValues(String name) {
        return stringMap.get(name);
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        
        // 基于缓存 body 构造可重复读取的 ServletInputStream
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
        
        return new ServletInputStream() {
            
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }
            
            @Override
            public boolean isFinished() {
                return false;
            }
            
            @Override
            public boolean isReady() {
                return false;
            }
            
            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }
    
}
