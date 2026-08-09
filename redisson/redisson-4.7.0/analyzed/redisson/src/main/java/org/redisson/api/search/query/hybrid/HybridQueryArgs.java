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
package org.redisson.api.search.query.hybrid;

import org.redisson.api.search.Expression;
import org.redisson.api.search.GroupBy;

import java.time.Duration;

/**
 * {@code FT.HYBRID} 混合搜索命令参数，融合文本检索与向量相似度。
 * <p>
 * Requires Redis 8.4.0 or higher.
 *
 * @author Nikita Koksharov
 */
public interface HybridQueryArgs {

    /**
     * 以指定文本查询表达式创建混合搜索。
     *
     * @param query 文本检索表达式
     * @return 查询配置步骤
     */
    static QueryStep query(String query) {
        return new HybridQueryParams(query);
    }

    /**
     * 配置文本检索与向量相似度结果的融合方式。
     *
     * @param value 组合配置（使用 {@link Combine#reciprocalRankFusion()} 或 {@link Combine#linear()}）
     * @return 当前实例
     */
    HybridQueryArgs combine(Combine value);

    /**
     * 指定结果中需返回的字段。
     *
     * @param fields 字段名
     * @return 当前实例
     */
    HybridQueryArgs load(String... fields);

    /**
     * 限制最终返回结果数量。
     *
     * @param offset 起始偏移（从 0 起）
     * @param count 返回条数
     * @return 当前实例
     */
    HybridQueryArgs limit(int offset, int count);

    /**
     * 按指定字段分组并应用聚合函数。
     *
     * @param groups 分组定义
     * @return 当前实例
     */
    HybridQueryArgs groupBy(GroupBy... groups);

    /**
     * 应用表达式变换以生成新字段。
     *
     * @param expressions 表达式列表
     * @return 当前实例
     */
    HybridQueryArgs apply(Expression... expressions);

    /**
     * 设置查询执行超时时间。
     *
     * @param timeout 超时时间
     * @return 当前实例
     */
    HybridQueryArgs timeout(Duration timeout);

    /**
     * 在 COMBINE 步骤之后应用后置过滤表达式。
     *
     * @param value 过滤表达式
     * @return 当前实例
     */
    HybridQueryArgs filter(String value);

}