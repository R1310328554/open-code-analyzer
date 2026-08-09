/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.gateway.common.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;

/**
 * 网关参数流控项，描述从请求中提取并匹配参数的策略。
 *
 * @author Eric Zhao
 * @since 1.6.0
 */
public class GatewayParamFlowItem {

    /**
     * 应用于参数流控规则时需设置的参数索引。
     */
    private Integer index;

    /**
     * 解析策略（如客户端 IP、任意 Header 或 URL 参数）。
     */
    private int parseStrategy;
    /**
     * 待提取字段名（仅在 Header 或 URL 参数模式下必填）。
     */
    private String fieldName;
    /**
     * 匹配模式。若未设置，所有值将保留在 LRU map 中。
     */
    private String pattern;
    /**
     * 参数值的匹配策略。
     */
    private int matchStrategy = SentinelGatewayConstants.PARAM_MATCH_STRATEGY_EXACT;

    public Integer getIndex() {
        return index;
    }

    GatewayParamFlowItem setIndex(Integer index) {
        this.index = index;
        return this;
    }

    public int getParseStrategy() {
        return parseStrategy;
    }

    public GatewayParamFlowItem setParseStrategy(int parseStrategy) {
        this.parseStrategy = parseStrategy;
        return this;
    }

    public String getFieldName() {
        return fieldName;
    }

    public GatewayParamFlowItem setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public GatewayParamFlowItem setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public int getMatchStrategy() {
        return matchStrategy;
    }

    public GatewayParamFlowItem setMatchStrategy(int matchStrategy) {
        this.matchStrategy = matchStrategy;
        return this;
    }

    @Override
    public String toString() {
        return "GatewayParamFlowItem{" +
            "index=" + index +
            ", parseStrategy=" + parseStrategy +
            ", fieldName='" + fieldName + '\'' +
            ", pattern='" + pattern + '\'' +
            ", matchStrategy=" + matchStrategy +
            '}';
    }
}
