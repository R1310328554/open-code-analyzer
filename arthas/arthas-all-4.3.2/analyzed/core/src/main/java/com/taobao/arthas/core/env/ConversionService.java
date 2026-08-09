/*
 * Copyright 2002-2016 the original author or authors.
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

package com.taobao.arthas.core.env;

/**
 * 类型转换服务接口：Arthas 环境模块中属性值类型转换的统一入口。
 * <p>
 * 调用 {@link #convert(Object, Class)} 执行线程安全的类型转换；
 * 转换前先通过 {@link #canConvert(Class, Class)} 判断是否支持。
 *
 * @author Keith Donald
 * @author Phillip Webb
 * @since 3.0
 */
public interface ConversionService {

    /**
     * 判断 {@code sourceType} 能否转换为 {@code targetType}。
     * <p>
     * 返回 {@code true} 仅表示转换可能成功；对于集合/数组/Map 类型，
     * 元素不可转换时 {@link #convert(Object, Class)} 仍可能抛出 {@link ConversionException}。
     * 
     * @param sourceType 源类型（source 为 {@code null} 时可传 {@code null}）
     * @param targetType 目标类型（必填）
     * @return 支持转换返回 {@code true}，否则 {@code false}
     * @throws IllegalArgumentException 若 {@code targetType} 为 {@code null}
     */
    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    /**
     * 将 {@code source} 转换为 {@code targetType} 类型的对象。
     * 
     * @param source     源对象（可为 {@code null}）
     * @param targetType 目标类型（必填）
     * @return 转换后的对象实例
     * @throws ConversionException      转换失败时抛出
     * @throws IllegalArgumentException 若 targetType 为 {@code null}
     */
    <T> T convert(Object source, Class<T> targetType);

}
