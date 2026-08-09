/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.quarkus;

import com.alibaba.csp.sentinel.annotation.cdi.interceptor.SentinelResourceBinding;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.enterprise.context.ApplicationScoped;

/**
 * 问候业务服务：通过 {@link com.alibaba.csp.sentinel.annotation.cdi.interceptor.SentinelResourceBinding}
 * 演示流控、熔断与 fallback。
 *
 * @author sea
 */
@ApplicationScoped
@RegisterForReflection
public class GreetingService {

    @SentinelResourceBinding(value = "greeting1", fallback = "globalDefaultFallback", fallbackClass = GreetingFallback.class, blockHandler = "globalBlockHandler", blockHandlerClass = GreetingFallback.class)
    /** 资源 greeting1：name=degrade 时抛异常触发熔断/fallback。 */
    public String greeting(String name) {
        if ("degrade".equals(name)) {
            throw new RuntimeException("test sentinel fallback");
        }
        return "hello " + name;
    }

    @SentinelResourceBinding(value = "greeting2", fallback = "greetingFallback")
    /** 资源 greeting2：使用同类 fallback 方法。 */
    public String greetingWithFallbackName(String name) {
        if ("degrade".equals(name)) {
            throw new RuntimeException("test sentinel fallback");
        }
        return "hello " + name;
    }

    /** greeting2 的业务 fallback。 */
    public String greetingFallback(String name, Throwable t) {
        return "greetingFallback: " + t.getClass().getSimpleName();
    }
}
