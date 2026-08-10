/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.monitor.topn;

/**
 * 以 {@link String} 为 key 的 TopN 计数器，直接复用字符串作为统计键。
 * <p>继承 {@link BaseTopNCounter}，适用于 RPC 接口名、资源路径等天然字符串维度的热点统计。</p>
 * String key topN counter.
 *
 * @author xiweng.yy
 */
/**
 * String 键 TopN 计数器实现类。
 */
public class StringTopNCounter extends BaseTopNCounter<String> {
    
    /**
     * {@inheritDoc} — String key 无需转换，原样返回。
     *
     * @param s 统计键
     * @return 与入参相同的字符串
     */
    @Override
    protected String keyToString(String s) {
        return s;
    }
}
