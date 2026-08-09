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
package com.alibaba.csp.sentinel.slots.system;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * 系统保护规则触发的阻断异常。
 *
 * @author jialiang.linjl
 */
public class SystemBlockException extends BlockException {

    private final String resourceName;

    /** 指定资源名、消息与原因构造异常。 */
    public SystemBlockException(String resourceName, String message, Throwable cause) {
        super(message, cause);
        this.resourceName = resourceName;
    }

    /** 指定资源名与限流类型构造异常。 */
    public SystemBlockException(String resourceName, String limitType) {
        super(limitType);
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    /** 不填充堆栈，降低阻断异常开销。 */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    /**
     * 返回触发的系统规则限流类型。
     *
     * @return 限流类型
     * @since 1.4.2
     */
    public String getLimitType() {
        return getRuleLimitApp();
    }
}
