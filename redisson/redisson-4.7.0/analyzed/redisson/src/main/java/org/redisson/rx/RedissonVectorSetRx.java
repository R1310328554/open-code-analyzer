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
package org.redisson.rx;

import io.reactivex.rxjava3.core.Flowable;
import org.redisson.api.RObject;
import org.redisson.api.RVectorSetAsync;

import java.util.List;

/**
 * 向量集合 {@link org.redisson.api.RVectorSet} 的 RxJava3 适配。
 * <p>
 * 通过 {@code ZRANGEBYLEX} 分页拉取成员，以 {@link io.reactivex.rxjava3.core.Flowable}
 * 递归拼接实现全量迭代；每批默认 {@value #BATCH_SIZE} 条。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonVectorSetRx {

    /** 单次 range 请求的最大成员数。 */
    private static final int BATCH_SIZE = 10;

    /** 底层异步向量集合实例。 */
    private final RVectorSetAsync instance;
    /** Rx 命令执行器，用于将 {@link org.redisson.api.RFuture} 转为 {@link io.reactivex.rxjava3.core.Flowable}。 */
    private final CommandRxExecutor commandExecutor;

    /** @param commandExecutor Rx 执行器 @param instance 被包装的 RVectorSetAsync */
    public RedissonVectorSetRx(CommandRxExecutor commandExecutor, RVectorSetAsync instance) {
        this.commandExecutor = commandExecutor;
        this.instance = instance;
    }

    /** @return Redis 键名（委托底层 {@link RObject}） */
    public String getName() {
        return ((RObject) instance).getName();
    }

    /** 从字典序最小成员起迭代全部元素。 */
    public Flowable<String> iterator() {
        return scan(null);
    }

    /** 递归分页：本页不足 {@link #BATCH_SIZE} 则结束，否则以上一页末元素为下一起点继续 scan。 */
    private Flowable<String> scan(String lastElement) {
        return Flowable.defer(() -> fetchPage(lastElement).concatMap(page -> {
            Flowable<String> current = Flowable.fromIterable(page);
            if (page.size() < BATCH_SIZE) {
                return current;
            }
            return current.concatWith(scan(page.get(page.size() - 1)));
        }));
    }

    /** 拉取一页 lex 范围成员；{@code lastElement==null} 时从 {@code "-"} 起，否则用开区间 {@code "("+lastElement}。 */
    private Flowable<List<String>> fetchPage(String lastElement) {
        String start;
        if (lastElement == null) {
            start = "-";
        } else {
            start = "(" + lastElement;
        }
        return commandExecutor.flowable(() -> instance.rangeAsync(start, "+", BATCH_SIZE));
    }

}
