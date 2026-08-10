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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 可重复读取请求体的 {@link HttpServletRequest} 扩展接口，供过滤器链多次解析参数与 body。
 * ReuseHttpRequest.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface ReuseHttpRequest extends HttpServletRequest {
    
    /**
     * 获取已缓存的请求体：multipart 返回 parts，否则返回字符串或编码后的表单参数。
     * get request body.
     *
     * @return object
     * @throws Exception exception
     */
    Object getBody() throws Exception;
    
    /**
     * 将参数 Map 中各 key 的值数组去重后返回新 Map。
     * Remove duplicate values from the array.
     *
     * @param request {@link HttpServletRequest}
     * @return {@link Map}
     */
    default Map<String, String[]> toDuplication(HttpServletRequest request) {
        Map<String, String[]> tmp = request.getParameterMap();
        Map<String, String[]> result = new HashMap<>(tmp.size());
        Set<String> set = new HashSet<>();
        // 逐 key 去重参数值并写入结果 Map
        for (Map.Entry<String, String[]> entry : tmp.entrySet()) {
            set.addAll(Arrays.asList(entry.getValue()));
            result.put(entry.getKey(), set.toArray(new String[0]));
            set.clear();
        }
        return result;
    }
}
