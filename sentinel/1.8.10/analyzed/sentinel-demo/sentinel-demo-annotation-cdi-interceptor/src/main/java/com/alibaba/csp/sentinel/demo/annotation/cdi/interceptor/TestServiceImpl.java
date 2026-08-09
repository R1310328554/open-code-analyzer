/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.annotation.cdi.interceptor;

import com.alibaba.csp.sentinel.annotation.cdi.interceptor.SentinelResourceBinding;
import javax.enterprise.context.ApplicationScoped;


/**
 * {@link TestService} 实现：通过 {@link SentinelResourceBinding} 绑定资源名与降级策略。
 *
 * @author Eric Zhao
 */
@ApplicationScoped
public class TestServiceImpl implements TestService {

    @Override
    /** 资源 test：限流时调用 ExceptionUtil.handleException。 */
    @SentinelResourceBinding(value = "test", blockHandler = "handleException", blockHandlerClass = {ExceptionUtil.class})
    public void test() {
        System.out.println("Test");
    }

    @Override
    /** 资源 hello：异常时走 helloFallback 方法降级。 */
    @SentinelResourceBinding(value = "hello", fallback = "helloFallback")
    public String hello(long s) {
        if (s < 0) {
            throw new IllegalArgumentException("invalid arg");
        }
        return String.format("Hello at %d", s);
    }

    @Override
    /** 资源 helloAnother：除 IllegalStateException 外均走 defaultFallback。 */
    @SentinelResourceBinding(value = "helloAnother", defaultFallback = "defaultFallback",
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

    /** fallback 方法：参数为原方法参数 + 捕获的 Throwable。 */
    public String helloFallback(long s, Throwable ex) {
        // 可在此记录降级日志
        return "Oops, error occurred at " + s + ", msg:" + ex.getMessage();
    }

    /** 默认 fallback，无参数，返回固定降级文案。 */
    public String defaultFallback() {
        System.out.println("Go to default fallback");
        return "default_fallback";
    }
}
