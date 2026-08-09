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
package com.alibaba.csp.sentinel.demo.spring.webflux.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 基于 {@link ReactiveRedisTemplate} 的响应式 Redis 读写服务。
 * <p>运行前需在本地启动 Redis 实例。</p>
 *
 * @author Eric Zhao
 */
@Service
public class BazService {

    @Autowired
    private ReactiveRedisTemplate<String, String> template;

    /** 按 id 读取键 sentinel-reactor-test:{id}，不存在时返回 not_found。 */
    public Mono<String> getById(Long id) {
        if (id == null || id <= 0) {
            return Mono.error(new IllegalArgumentException("invalid id: " + id));
        }
        return template.opsForValue()
            .get(KEY_PREFIX + id)
            .switchIfEmpty(Mono.just("not_found"));
    }

    /** 向 sentinel-reactor-test:{id} 写入 value。 */
    public Mono<Boolean> setValue(Long id, String value) {
        if (id == null || id <= 0 || value == null) {
            return Mono.error(new IllegalArgumentException("invalid parameters"));
        }
        return template.opsForValue()
            .set(KEY_PREFIX + id, value);
    }

    private static final String KEY_PREFIX = "sentinel-reactor-test:";
}
