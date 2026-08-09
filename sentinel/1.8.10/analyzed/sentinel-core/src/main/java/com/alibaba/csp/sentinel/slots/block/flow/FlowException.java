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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * 流控阻断异常，表示请求被流控规则拦截。
 *
 * @author youji.zj
 */
public class FlowException extends BlockException {

    /** 指定限流来源应用构造异常。 */
    public FlowException(String ruleLimitApp) {
        super(ruleLimitApp);
    }

    /** 指定限流来源与触发的流控规则构造异常。 */
    public FlowException(String ruleLimitApp, FlowRule rule) {
        super(ruleLimitApp, rule);
    }

    /** 指定消息与原因构造异常。 */
    public FlowException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 指定限流来源与消息构造异常。 */
    public FlowException(String ruleLimitApp, String message) {
        super(ruleLimitApp, message);
    }

    /** 不填充堆栈，降低阻断异常开销。 */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    /**
     * 获取触发的流控规则。
     * 注意：返回的是规则映射中的引用，不应修改。
     *
     * @return 触发的流控规则
     * @since 1.4.2
     */
    @Override
    public FlowRule getRule() {
        return rule.as(FlowRule.class);
    }
}
