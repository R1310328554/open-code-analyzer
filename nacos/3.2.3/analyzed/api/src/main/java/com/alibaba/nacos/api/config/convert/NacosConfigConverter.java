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

package com.alibaba.nacos.api.config.convert;

/**
 * Nacos 配置字符串到目标类型的转换器接口。
 *
 * @param <T> 目标 Java 类型
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.2.0
 */
public interface NacosConfigConverter<T> {
    
    /**
     * 判断是否可将配置转换为目标类型。
     *
     * @param targetType 目标类型
     * @return 支持转换返回 {@code true}，否则 {@code false}
     */
    boolean canConvert(Class<T> targetType);
    
    /**
     * 将 Nacos 配置字符串转换为目标类型实例。
     *
     * @param config 待转换的配置文本（非 {@code null}）
     * @return 转换结果，可能为 {@code null}
     */
    T convert(String config);
    
}
