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
package com.alibaba.csp.sentinel.datasource;

/**
 * 配置转换器：将原始类型 {@code S} 转换为目标类型 {@code T}（如 JSON 字符串 → 规则列表）。
 *
 * @author leyou
 * @author Eric Zhao
 */
public interface Converter<S, T> {

    /**
     * 将 {@code source} 转换为目标类型。
     *
     * @param source 原始配置对象
     * @return 解析后的目标对象
     */
    T convert(S source);
}
