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
package com.alibaba.csp.sentinel.dashboard.repository.rule;

import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;

import org.springframework.stereotype.Component;

/**
 * 熔断降级规则内存仓库，基于 {@link InMemoryRuleRepositoryAdapter} 持久化 {@link DegradeRuleEntity}。
 *
 * @author leyou
 */
@Component
public class InMemDegradeRuleStore extends InMemoryRuleRepositoryAdapter<DegradeRuleEntity> {

    /** 自增 ID 生成器。 */
    private static AtomicLong ids = new AtomicLong(0);

    @Override
    /** @return 下一个未使用的规则 ID */
    protected long nextId() {
        return ids.incrementAndGet();
    }
}
