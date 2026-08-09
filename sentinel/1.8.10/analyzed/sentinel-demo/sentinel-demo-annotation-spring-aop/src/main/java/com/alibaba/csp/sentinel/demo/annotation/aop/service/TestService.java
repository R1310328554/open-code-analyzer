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

/**
 * 注解限流演示服务接口：覆盖 blockHandler、fallback 与 defaultFallback 场景。
 *
 * @author Eric Zhao
 */
public interface TestService {

    /** 无返回值资源，演示 blockHandlerClass 跨类处理。 */
    void test();

    /** 按 long 参数问候，演示 fallback。 */
    String hello(long s);

    /** 按 String 参数问候，演示同名 fallback 重载。 */
    String hello(String s);

    /** 演示 defaultFallback 与 exceptionsToIgnore。 */
    String helloAnother(String name);
}
