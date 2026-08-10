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

import com.alibaba.nacos.common.utils.MapUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Http Query object.
 * <p>URL 查询参数构建器：{@link LinkedHashMap} 保持插入顺序，通过 {@link #toQueryUrl()} 生成 {@code K=V&K=V} 形式并已 URL 编码。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class Query {
    
    /** 是否尚未添加任何参数，与 {@link #clear()} 联动 */
    private boolean isEmpty = true;
    
    /** 空 Query 单例 */
    public static final Query EMPTY = Query.newInstance();
    
    private Map<String, Object> params;
    
    private static final String DEFAULT_ENC = "UTF-8";
    
    public Query() {
        params = new LinkedHashMap<>();
    }
    
    public static Query newInstance() {
        return new Query();
    }
    
    /**
     * Add query parameter.
     * <p>追加单个查询键值，value 可为任意 Object，序列化时 {@code String.valueOf}。</p>
     *
     * @param key   key
     * @param value value
     * @return this query
     */
    public Query addParam(String key, Object value) {
        isEmpty = false;
        params.put(key, value);
        return this;
    }
    
    public Object getValue(String key) {
        return params.get(key);
    }
    
    /**
     * Add all parameters as query parameter.
     *
     * @param params parameters
     * @return this query
      * <p>URL 查询参数；详见类级说明。</p>
     */
    public Query initParams(Map<String, String> params) {
        if (MapUtil.isNotEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                addParam(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }
    
    /**
     * Print query as a http url param string. Like K=V&K=V.
     * <p>按插入顺序拼接查询串，null 值跳过，键值均 UTF-8 URL 编码。</p>
     *
     * @return http url param string
     */
    public String toQueryUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        Set<Map.Entry<String, Object>> entrySet = params.entrySet();
        int i = entrySet.size();
        for (Map.Entry<String, Object> entry : entrySet) {
            try {
                if (null != entry.getValue()) {
                    urlBuilder.append(entry.getKey()).append('=')
                        .append(URLEncoder.encode(String.valueOf(entry.getValue()), DEFAULT_ENC));
                    if (i > 1) {
                        urlBuilder.append('&');
                    }
                }
                i--;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        
        return urlBuilder.toString();
    }
    
    public void clear() {
        params.clear();
        isEmpty = true;
    }
    
    public boolean isEmpty() {
        return isEmpty;
    }
    
}
