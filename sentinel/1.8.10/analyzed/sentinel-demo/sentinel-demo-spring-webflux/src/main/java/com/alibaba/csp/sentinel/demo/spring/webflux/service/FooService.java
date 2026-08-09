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
package com.alibaba.csp.sentinel.demo.spring.webflux.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * WebFlux 演示业务服务：提供 Mono/Flux 响应式方法，供 {@link com.alibaba.csp.sentinel.demo.spring.webflux.controller.FooController} 限流演示。
 *
 * @author Eric Zhao
 */
@Service
public class FooService {

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private final ExecutorService pool = Executors.newFixedThreadPool(8);
    private final Scheduler scheduler = Schedulers.fromExecutor(pool);

    /** 返回随机整数拼接 "d" 的单值 Mono。 */
    public Mono<String> emitSingle() {
        return Mono.just(ThreadLocalRandom.current().nextInt(0, 2000))
            .map(e -> e + "d");
    }

    /** 从随机起点连续发射 10 个整数的 Flux。 */
    public Flux<Integer> emitMultiple() {
        int start = ThreadLocalRandom.current().nextInt(0, 6000);
        return Flux.range(start, 10);
    }

    /** 在独立线程池上执行 2s 延迟任务，演示异步限流场景。 */
    public Mono<String> doSomethingSlow() {
        return Mono.fromCallable(() -> {
            Thread.sleep(2000);
            System.out.println("doSomethingSlow: " + Thread.currentThread().getName());
            return "ok";
        }).publishOn(scheduler);
    }
}
