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

import org.springframework.util.ObjectUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 请求参数匹配条件：等价于 Spring {@link org.springframework.web.bind.annotation.RequestMapping#params()} 语义，支持存在性、等值与取反表达式。
 * request param info. {@link org.springframework.web.bind.annotation.RequestMapping#params()}
 *
 * @author horizonzy
 * @since 1.3.2
 */
public class ParamRequestCondition {
    
    /** 解析后的参数表达式集合。 */
    private final Set<ParamExpression> expressions;
    
    /**
     * 由 params 字符串数组构造条件。
     *
     * @param expressions 参数表达式，如 {@code foo=bar}、{@code !baz}
     */
    public ParamRequestCondition(String... expressions) {
        this.expressions = parseExpressions(expressions);
    }
    
    /** 将原始 params 字符串解析为 {@link ParamExpression} 集合。 */
    private Set<ParamExpression> parseExpressions(String... params) {
        if (ObjectUtils.isEmpty(params)) {
            return Collections.emptySet();
        }
        Set<ParamExpression> expressions = new LinkedHashSet<>(params.length);
        for (String param : params) {
            expressions.add(new ParamExpression(param));
        }
        return expressions;
    }
    
    /** 返回全部参数表达式。 */
    public Set<ParamExpression> getExpressions() {
        return expressions;
    }
    
    /**
     * 若当前请求满足全部表达式则返回自身，否则返回 null。
     *
     * @param request HTTP 请求
     * @return 匹配的条件或 null
     */
    public ParamRequestCondition getMatchingCondition(HttpServletRequest request) {
        for (ParamExpression expression : this.expressions) {
            if (!expression.match(request)) {
                return null;
            }
        }
        return this;
    }
    
    @Override
    public String toString() {
        return "ParamRequestCondition{" + "expressions=" + expressions + '}';
    }
    
    /** 单个请求参数表达式：支持名称存在、等值匹配及前缀 {@code !} 取反。 */
    static class ParamExpression {
        
        /** 参数名。 */
        private final String name;
        
        /** 期望参数值；null 表示仅检查存在性。 */
        private final String value;
        
        /** 是否为取反匹配（期望不满足条件）。 */
        private final boolean isNegated;
        
        /** 解析形如 {@code name=value} 或 {@code !name} 的表达式字符串。 */
        ParamExpression(String expression) {
            int separator = expression.indexOf('=');
            if (separator == -1) {
                this.isNegated = expression.startsWith("!");
                this.name = isNegated ? expression.substring(1) : expression;
                this.value = null;
            } else {
                this.isNegated = (separator > 0) && (expression.charAt(separator - 1) == '!');
                this.name = isNegated ? expression.substring(0, separator - 1)
                    : expression.substring(0, separator);
                this.value = expression.substring(separator + 1);
            }
        }
        
        /** 判断请求是否满足（或取反后不满足）该表达式。 */
        public final boolean match(HttpServletRequest request) {
            boolean isMatch;
            if (this.value != null) {
                isMatch = matchValue(request);
            } else {
                isMatch = matchName(request);
            }
            return this.isNegated != isMatch;
        }
        
        /** 仅按参数名是否存在匹配。 */
        private boolean matchName(HttpServletRequest request) {
            return request.getParameterMap().containsKey(this.name);
        }
        
        /** 按参数名与期望值精确匹配。 */
        private boolean matchValue(HttpServletRequest request) {
            return ObjectUtils.nullSafeEquals(this.value, request.getParameter(this.name));
        }
        
        @Override
        public String toString() {
            return "ParamExpression{" + "name='" + name + '\'' + ", value='" + value + '\''
                + ", isNegated=" + isNegated
                + '}';
        }
    }
}
