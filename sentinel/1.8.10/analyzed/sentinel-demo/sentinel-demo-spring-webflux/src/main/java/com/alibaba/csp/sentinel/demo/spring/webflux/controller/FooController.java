/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.spring.webflux.controller;

import com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer;
import com.alibaba.csp.sentinel.demo.spring.webflux.service.FooService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式 Mono/Flux 演示控制器，展示 {@link SentinelReactorTransformer} 对 Publisher 的限流。
 *
 * @author Eric Zhao
 */
@RestController
@RequestMapping(value = "/foo")
public class FooController {

    @Autowired
    private FooService fooService;

    /** GET /foo/single：单值 Mono，资源名 demo_foo_normal_single。 */
    @GetMapping("/single")
    public Mono<String> apiNormalSingle() {
        return fooService.emitSingle()
            // 在此对 Publisher 应用 Sentinel 变换
            .transform(new SentinelReactorTransformer<>("demo_foo_normal_single"));
    }

    /** GET /foo/flux：整数 Flux 流，资源名 demo_foo_normal_flux。 */
    @GetMapping("/flux")
    public Flux<Integer> apiNormalFlux() {
        return fooService.emitMultiple()
            .transform(new SentinelReactorTransformer<>("demo_foo_normal_flux"));
    }

    /** GET /foo/slow：慢调用接口（未套 SentinelReactorTransformer，依赖 WebFlux Filter）。 */
    @GetMapping("/slow")
    public Mono<String> apiDoSomethingSlow(ServerHttpResponse response) {
        return fooService.doSomethingSlow();
    }
}
