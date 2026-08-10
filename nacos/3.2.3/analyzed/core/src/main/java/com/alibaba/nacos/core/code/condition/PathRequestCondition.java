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

package com.alibaba.nacos.core.code.condition;

import static com.alibaba.nacos.sys.env.Constants.REQUEST_PATH_SEPARATOR;

/**
 * 请求路径匹配条件：将「HTTP 方法 + 路径」编码为单一字符串并解析为 {@link PathExpression}。
 * request path info. method:{@link org.springframework.web.bind.annotation.RequestMapping#method()} path: {@link
 * org.springframework.web.bind.annotation.RequestMapping#value()} or {@link org.springframework.web.bind.annotation.RequestMapping#value()}
 *
 * @author horizonzy
 * @since 1.3.2
 */
public class PathRequestCondition {
    
    /** 解析后的方法与路径表达式。 */
    private final PathExpression pathExpression;
    
    /**
     * 由 {@code METHOD/path} 格式字符串构造路径条件。
     *
     * @param pathExpression 方法与路径的组合键
     */
    public PathRequestCondition(String pathExpression) {
        this.pathExpression = parseExpressions(pathExpression);
    }
    
    /** 按 {@link Constants#REQUEST_PATH_SEPARATOR} 拆分方法与路径段。 */
    private PathExpression parseExpressions(String pathExpression) {
        String[] split = pathExpression.split(REQUEST_PATH_SEPARATOR);
        String method = split[0];
        String path = split[1];
        return new PathExpression(method, path);
    }
    
    @Override
    public String toString() {
        return "PathRequestCondition{" + "pathExpression=" + pathExpression + '}';
    }
    
    /** 不可变的路径表达式：HTTP 方法名与 URI 路径模板。 */
    static class PathExpression {
        
        /** HTTP 方法名（如 GET、POST）。 */
        private final String method;
        
        /** 映射 URI 路径（含类级与方法级前缀）。 */
        private final String path;
        
        /** 构造方法与路径组合。 */
        PathExpression(String method, String path) {
            this.method = method;
            this.path = path;
        }
        
        @Override
        public String toString() {
            return "PathExpression{" + "method='" + method + '\'' + ", path='" + path + '\'' + '}';
        }
    }
}
