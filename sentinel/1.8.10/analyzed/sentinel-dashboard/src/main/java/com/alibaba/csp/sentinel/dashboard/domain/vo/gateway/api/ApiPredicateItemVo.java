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
package com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.api;

/**
 * 网关 API URL 匹配谓词项，定义匹配模式与策略。
 * <p>matchStrategy 常量见 {@code SentinelGatewayConstants}：
 * 0 精确匹配、1 前缀匹配、2 正则匹配。
 *
 * @author cdfive
 * @since 1.7.0
 */
public class ApiPredicateItemVo {

    /** URL 匹配模式（路径或正则表达式）。 */
    private String pattern;

    /**
     * URL 匹配策略，常量定义于 {@code SentinelGatewayConstants}：
     * <ul>
     *     <li>0（URL_MATCH_STRATEGY_EXACT）：精确匹配</li>
     *     <li>1（URL_MATCH_STRATEGY_PREFIX）：前缀匹配</li>
     *     <li>2（URL_MATCH_STRATEGY_REGEX）：正则匹配</li>
     * </ul>
     */
    private Integer matchStrategy;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Integer getMatchStrategy() {
        return matchStrategy;
    }

    public void setMatchStrategy(Integer matchStrategy) {
        this.matchStrategy = matchStrategy;
    }
}
