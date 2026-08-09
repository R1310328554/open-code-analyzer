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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * 热点参数流控触发时抛出的阻塞异常。
 *
 * @author jialiang.linjl
 * @since 0.2.0
 */
public class ParamFlowException extends BlockException {

    private final String resourceName;

    public ParamFlowException(String resourceName, String message, Throwable cause) {
        super(message, cause);
        this.resourceName = resourceName;
    }

    public ParamFlowException(String resourceName, String param) {
        super(param, param);
        this.resourceName = resourceName;
    }

    public ParamFlowException(String resourceName, String param, ParamFlowRule rule) {
        super(param, param);
        this.resourceName = resourceName;
        this.rule = rule;
    }

    public String getResourceName() {
        return resourceName;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    /**
     * 获取触发参数流控的参数值。
     *
     * @return 参数值字符串
     * @since 1.4.2
     */
    public String getLimitParam() {
        return getMessage();
    }

    /**
     * 获取触发的规则。
     * 注意：返回的规则引用自内部规则映射，请勿修改。
     *
     * @return 触发的 ParamFlowRule
     * @since 1.4.2
     */
    @Override
    public ParamFlowRule getRule() {
        return rule.as(ParamFlowRule.class);
    }
}
