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

package com.alibaba.nacos.common.logging;

/**
 * Builder of {@link NacosLoggingAdapter}.
 * <p>Nacos 日志适配器构建器 SPI：延迟实例化适配器，避免直接 SPI {@link NacosLoggingAdapter} 时初始化异常导致整个加载器退出。</p>
 * <p>
 *     Why not directly SPI {@link NacosLoggingAdapter}?
 * </p>
 * <p>
 *     To avoid some {@link NacosLoggingAdapter} initialization error casue the SPI loader exit.
 * </p>
 *
 * @author xiweng.yy
 */
public interface NacosLoggingAdapterBuilder {
    
    /**
     * Build {@link NacosLoggingAdapter} implementation.
     * <p>构建并返回具体的日志适配器实例。</p>
     *
     * @return {@link NacosLoggingAdapter}
     */
    NacosLoggingAdapter build();
}
