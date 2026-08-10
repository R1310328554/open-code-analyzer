/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.env.convert;

/**
 * 环境变量字符串到目标类型的转换器抽象基类。
 * <p>由 {@link CompositeConverter} 按目标 Class 分派具体实现。</p>
 */
abstract class AbstractPropertyConverter<T> {
    
    /**
     * 将原始字符串属性转换为目标类型。
     * @param property 自环境读取的字符串值
     * @return 转换结果；空串通常映射为 {@code null}
     */
    abstract T convert(String property);
    
}
