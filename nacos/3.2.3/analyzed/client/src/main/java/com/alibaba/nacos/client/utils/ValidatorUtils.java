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

package com.alibaba.nacos.client.utils;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.env.NacosClientProperties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All parameter validation tools.
 * <p>客户端启动参数校验工具：检查 {@link NacosClientProperties} 中的 context path 等配置，防止非法 URL 片段导致请求拼接异常。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class ValidatorUtils {
    
    /** 匹配连续斜杠的 context path 非法模式 */
    private static final Pattern CONTEXT_PATH_MATCH = Pattern.compile("(\\/)\\1+");
    
    /** 校验客户端初始化属性（当前含 context path） */
    public static void checkInitParam(NacosClientProperties properties) throws NacosException {
        checkContextPath(properties.getProperty(PropertyKeyConst.CONTEXT_PATH));
    }
    
    /**
     * Check context path.
     * <p>null 合法；若存在连续 {@code //} 则抛 {@link IllegalArgumentException}。</p>
     *
     * @param contextPath context path
     */
    public static void checkContextPath(String contextPath) {
        if (contextPath == null) {
            return;
        }
        Matcher matcher = CONTEXT_PATH_MATCH.matcher(contextPath);
        if (matcher.find()) {
            throw new IllegalArgumentException("Illegal url path expression");
        }
    }
    
}
