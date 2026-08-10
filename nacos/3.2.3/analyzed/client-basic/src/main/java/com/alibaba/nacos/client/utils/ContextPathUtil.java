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

import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Context path Util.
 * <p>Web 上下文路径规范化工具：将空值、根路径 {@code "/"} 转为空串，非空路径保证以 {@code "/"} 开头，便于客户端拼接 Nacos 服务端 URL。</p>
 *
 * @author Wei.Wang
 */
public class ContextPathUtil {
    
    /** Web 根上下文路径常量 */
    private static final String ROOT_WEB_CONTEXT_PATH = "/";
    
    /**
     * normalize context path.
     * <p>规范化上下文路径：空白或 {@code "/"} 返回空串；否则确保以 {@code "/"} 前缀。</p>
     *
     * @param contextPath origin context path
     * @return normalized context path
     */
    public static String normalizeContextPath(String contextPath) {
        if (StringUtils.isBlank(contextPath) || ROOT_WEB_CONTEXT_PATH.equals(contextPath)) {
            return StringUtils.EMPTY;
        }
        return contextPath.startsWith(ROOT_WEB_CONTEXT_PATH) ? contextPath
            : ROOT_WEB_CONTEXT_PATH + contextPath;
    }
}
