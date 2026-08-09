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
package com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.rule;

/**
 * 网关参数流控匹配项视图，定义参数解析策略、字段名、模式与匹配方式。
 * <p>用于 {@link AddFlowRuleReqVo} 中按请求参数维度细化限流。
 *
 * @author cdfive
 * @since 1.7.0
 */
public class GatewayParamFlowItemVo {

    /** 参数解析策略（Header/URL 参数/客户端 IP 等）。 */
    private Integer parseStrategy;

    /** 待解析的字段名（如 Header 名或参数名）。 */
    private String fieldName;

    /** 参数值匹配模式。 */
    private String pattern;

    /** 参数值匹配策略（精确/包含/正则等）。 */
    private Integer matchStrategy;

    public Integer getParseStrategy() {
        return parseStrategy;
    }

    public void setParseStrategy(Integer parseStrategy) {
        this.parseStrategy = parseStrategy;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

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
