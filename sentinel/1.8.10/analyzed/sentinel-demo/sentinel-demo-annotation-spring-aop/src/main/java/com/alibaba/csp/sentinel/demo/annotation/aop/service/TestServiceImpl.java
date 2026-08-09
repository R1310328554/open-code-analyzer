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
package com.alibaba.csp.sentinel.demo.annotation.aop.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.stereotype.Service;

/**
 * {@link TestService} 实现：展示 {@link SentinelResource} 注解的多种降级策略。
 *
 * @author Eric Zhao
 */
@Service
public class TestServiceImpl implements TestService {

    /** 资源 test：限流时委托 {@link ExceptionUtil#handleException} 处理。 */
    @Override
    @SentinelResource(value = "test", blockHandler = "handleException", blockHandlerClass = {ExceptionUtil.class})
    public void test() {
        System.out.println("Test");
    }

    /** 资源 hello：异常或限流时调用 helloFallback(long, Throwable)。 */
    @Override
    @SentinelResource(value = "hello", fallback = "helloFallback")
    public String hello(long s) {
        if (s < 0) {
            throw new IllegalArgumentException("invalid arg");
        }
        return String.format("Hello at %d", s);
    }

    /** 资源 helloStr：与 hello(long) 共用 fallback 方法名但参数签名不同。 */
    @Override
    @SentinelResource(value = "helloStr", fallback = "helloFallback")
    public String hello(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("unknown");
        }
        return String.format("Hello, %s", s);
    }

    /** 资源 helloAnother：IllegalStateException 不触发降级，其余异常走 defaultFallback。 */
    @Override
    @SentinelResource(value = "helloAnother", defaultFallback = "defaultFallback",
        exceptionsToIgnore = {IllegalStateException.class})
    public String helloAnother(String name) {
        if (name == null || "bad".equals(name)) {
            throw new IllegalArgumentException("oops");
        }
        if ("foo".equals(name)) {
            throw new IllegalStateException("oops");
        }
        return "Hello, " + name;
    }

    /** hello(long) 的 fallback：打印异常并返回友好提示。 */
    public String helloFallback(long s, Throwable ex) {
        ex.printStackTrace();
        return "Oops, error occurred at " + s;
    }

    /** hello(String) 的 fallback（private 亦可被 Sentinel 反射调用）。 */
    private String helloFallback(String ignored, Throwable e) {
        e.printStackTrace();
        return "Hello, stranger";
    }

    /** helloAnother 的默认降级返回值。 */
    public String defaultFallback() {
        System.out.println("Go to default fallback");
        return "default_fallback";
    }
}
