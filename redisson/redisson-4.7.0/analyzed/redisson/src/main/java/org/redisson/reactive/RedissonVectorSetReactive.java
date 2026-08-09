/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.reactive;

import org.redisson.RedissonVectorSet;
import org.redisson.api.RObject;
import org.redisson.api.RVectorSetAsync;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Redis Vector Set（RediSearch 向量索引集合）的 Reactor 响应式封装：
 * 通过分页 {@code VRANGE} 将成员名流式输出为 {@link Flux}。
 * <p>
 * 每页默认 {@link #BATCH_SIZE} 条；末页不足批量时停止扩展。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonVectorSetReactive {

    /** 每次 VRANGE 请求的最大成员数。 */
    private static final int BATCH_SIZE = 10;

    /** 底层异步 Vector Set。 */
    private final RVectorSetAsync instance;
    /** 响应式命令执行器。 */
    private final CommandReactiveExecutor commandExecutor;

    /** 按名称创建 Vector Set 响应式视图。 */
    public RedissonVectorSetReactive(CommandReactiveExecutor commandExecutor, String name) {
        this(commandExecutor, new RedissonVectorSet(commandExecutor, name));
    }

    /** 绑定已有异步实例。 */
    public RedissonVectorSetReactive(CommandReactiveExecutor commandExecutor, RVectorSetAsync instance) {
        this.commandExecutor = commandExecutor;
        this.instance = instance;
    }

    /** @return Vector Set 的 Redis 键名 */
    public String getName() {
        return ((RObject) instance).getName();
    }

    /** 分页 VRANGE 全量成员名，自动翻页直至末页。 */
    public Flux<String> iterator() {
        return fetchPage(null)
                // 末页满批量则继续从最后一个成员之后拉取
                .expand(page -> {
                    if (page.size() < BATCH_SIZE) {
                        return Mono.empty();
                    }
                    return fetchPage(page.get(page.size() - 1));
                })
                .concatMapIterable(page -> page);
    }

    /** 拉取一页成员；{@code lastElement} 为上一页末元素（开区间起点）。 */
    private Mono<List<String>> fetchPage(String lastElement) {
        String start;
        if (lastElement == null) {
            start = "-";
        } else {
            // 开区间：从 lastElement 之后继续扫描
            start = "(" + lastElement;
        }
        return commandExecutor.reactive(() -> instance.rangeAsync(start, "+", BATCH_SIZE));
    }

}
