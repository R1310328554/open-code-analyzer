/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.common.api;

import java.util.Objects;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;

/**
 * 基于 URL 路径的 API 谓词项，支持精确/前缀/正则匹配策略。
 *
 * @author Eric Zhao
 * @since 1.6.0
 */
public class ApiPathPredicateItem implements ApiPredicateItem {

    /** URL 路径匹配模式。 */
    private String pattern;
    /** URL 匹配策略，默认精确匹配。 */
    private int matchStrategy = SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT;

    /** 设置路径匹配模式。 */
    public ApiPathPredicateItem setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /** 设置 URL 匹配策略。 */
    public ApiPathPredicateItem setMatchStrategy(int matchStrategy) {
        this.matchStrategy = matchStrategy;
        return this;
    }

    /** 获取路径匹配模式。 */
    public String getPattern() {
        return pattern;
    }

    /** 获取 URL 匹配策略。 */
    public int getMatchStrategy() {
        return matchStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ApiPathPredicateItem that = (ApiPathPredicateItem)o;

        if (matchStrategy != that.matchStrategy) { return false; }
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + matchStrategy;
        return result;
    }

    @Override
    public String toString() {
        return "ApiPathPredicateItem{" +
            "pattern='" + pattern + '\'' +
            ", matchStrategy=" + matchStrategy +
            '}';
    }
}
