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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

/**
 * 可覆盖/追加请求参数的 {@link HttpServletRequestWrapper}。
 *
 * <p>在保留原始请求的基础上，允许修改 {@link #getParameterMap()} 等读出的参数值。
 * 参考：https://blog.csdn.net/xieyuooo/article/details/8447301</p>
 *
 * @author nkorange
 * @since 0.8.0
 */
public class OverrideParameterRequestWrapper extends HttpServletRequestWrapper {
    
    /** 可变的参数表副本。 */
    private Map<String, String[]> params = new HashMap<>();
    
    /**
     * 包装原始请求并复制其参数表。
     *
     * @param request 原始 HTTP 请求
     * @throws IllegalArgumentException 若 request 为 null
     */
    public OverrideParameterRequestWrapper(HttpServletRequest request) {
        super(request);
        this.params.putAll(request.getParameterMap());
    }
    
    /** 基于原始请求构建包装器（不追加参数）。 */
    public static OverrideParameterRequestWrapper buildRequest(HttpServletRequest request) {
        return new OverrideParameterRequestWrapper(request);
    }
    
    /**
     * 构建包装器并追加单个参数。
     *
     * @param request 原始请求
     * @param name    参数名
     * @param value   参数值
     * @return 已追加参数的包装器
     */
    public static OverrideParameterRequestWrapper buildRequest(HttpServletRequest request,
        String name, String value) {
        OverrideParameterRequestWrapper requestWrapper =
            new OverrideParameterRequestWrapper(request);
        requestWrapper.addParameter(name, value);
        return requestWrapper;
    }
    
    /**
     * 构建包装器并批量合并追加参数。
     *
     * @param request          原始请求
     * @param appendParameters 待合并的参数 Map
     * @return 已合并参数的包装器
     */
    public static OverrideParameterRequestWrapper buildRequest(HttpServletRequest request,
        Map<String, String[]> appendParameters) {
        OverrideParameterRequestWrapper requestWrapper =
            new OverrideParameterRequestWrapper(request);
        requestWrapper.params.putAll(appendParameters);
        return requestWrapper;
    }
    
    /** {@inheritDoc} 返回参数第一个值，不存在则 {@code null}。 */
    @Override
    public String getParameter(String name) {
        String[] values = params.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }
    
    /** {@inheritDoc} 返回可变参数表。 */
    @Override
    public Map<String, String[]> getParameterMap() {
        return params;
    }
    
    /** {@inheritDoc} 返回参数全部值数组。 */
    @Override
    public String[] getParameterValues(String name) {
        return params.get(name);
    }
    
    /**
     * 追加或覆盖单个参数（值为 null 时不写入）。
     *
     * @param name  参数名
     * @param value 参数值
     */
    public void addParameter(String name, String value) {
        if (value != null) {
            params.put(name, new String[] {value});
        }
    }
    
}
