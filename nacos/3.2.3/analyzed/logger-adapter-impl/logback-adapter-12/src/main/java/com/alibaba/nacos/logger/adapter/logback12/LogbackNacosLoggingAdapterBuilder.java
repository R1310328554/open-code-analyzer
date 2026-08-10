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

package com.alibaba.nacos.logger.adapter.logback12;

import com.alibaba.nacos.common.logging.NacosLoggingAdapter;
import com.alibaba.nacos.common.logging.NacosLoggingAdapterBuilder;

/**
 * Logback 1.2.x 日志适配器的 SPI 构建器。
 *
 * <p>通过 {@link com.alibaba.nacos.common.spi.NacosServiceLoader} 注册， 在 classpath 存在 Logback 1.2 且非 1.3+ 时由 {@link com.alibaba.nacos.common.logging.NacosLogging} 选用。</p>
 *
 * @author xiweng.yy
 */
public class LogbackNacosLoggingAdapterBuilder implements NacosLoggingAdapterBuilder {
    
    /** 创建 {@link LogbackNacosLoggingAdapter} 实例。 */
    @Override
    public NacosLoggingAdapter build() {
        return new LogbackNacosLoggingAdapter();
    }
}
