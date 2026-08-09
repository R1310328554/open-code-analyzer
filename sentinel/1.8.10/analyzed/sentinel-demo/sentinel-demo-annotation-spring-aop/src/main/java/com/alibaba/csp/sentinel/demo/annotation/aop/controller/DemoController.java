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
package com.alibaba.csp.sentinel.demo.annotation.aop.controller;

import com.alibaba.csp.sentinel.demo.annotation.aop.service.TestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring AOP 注解限流演示 REST 控制器：暴露 /foo、/bar、/baz 等测试接口。
 *
 * @author Eric Zhao
 */
@RestController
public class DemoController {

    @Autowired
    private TestService service;

    /** GET /foo：调用 test 与 hello(long)，未传 t 时使用当前时间戳。 */
    @GetMapping("/foo")
    public String apiFoo(@RequestParam(required = false) Long t) {
        if (t == null) {
            t = System.currentTimeMillis();
        }
        service.test();
        return service.hello(t);
    }

    /** GET /bar：调用 test 与 hello(String)。 */
    @GetMapping("/bar")
    public String apiBar(@RequestParam(required = false) String t) {
        service.test();
        return service.hello(t);
    }

    /** GET /baz/{name}：调用 helloAnother 演示 defaultFallback 与 exceptionsToIgnore。 */
    @GetMapping("/baz/{name}")
    public String apiBaz(@PathVariable("name") String name) {
        return service.helloAnother(name);
    }
}
