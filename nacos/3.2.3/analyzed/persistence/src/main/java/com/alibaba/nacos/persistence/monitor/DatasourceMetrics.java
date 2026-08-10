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

package com.alibaba.nacos.persistence.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

/**
 * 数据源异常监控指标。
 *
 * <p>通过 Micrometer 暴露 {@code nacos_exception} 计数器， 统计 config 模块数据库相关异常次数。</p>
 *
 * @author xiweng.yy
 */
public class DatasourceMetrics {
    
    /** 获取数据库异常 Micrometer 计数器。 */
    public static Counter getDbException() {
        // TODO: NacosMeterRegistryCenter 迁移至更基础模块后可改用统一注册中心
        // TODO: 当前 core 模块可能依赖 persistence 存 namespace，故暂用 Metrics 全局注册
        return Metrics.counter("nacos_exception", "module", "config", "name", "db");
    }
}
