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

/**
 * CDI 演示服务接口，定义带 Sentinel 注解绑定的测试方法。
 *
 * @author Eric Zhao
 */
public interface TestService {

    /** 无参测试方法，演示 blockHandler 限流回调。 */
    void test();

    /** 带 long 参数，演示 fallback 降级。 */
    String hello(long s);

    /** 带 String 参数，演示 defaultFallback 与 exceptionsToIgnore。 */
    String helloAnother(String name);
}
