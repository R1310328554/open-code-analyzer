/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 管控插件模块日志 Logger 集中定义。
 *
 * <p>按功能域划分管控主流程、TPS 限流、连接管控等独立 Logger，便于运行时分别调整日志级别。</p>
 *
 * @author shiyiyue
 */
public class Loggers {
    
    /** 管控插件主流程日志。 */
    public static final Logger CONTROL =
        LoggerFactory.getLogger("com.alibaba.nacos.plugin.control");
    
    /** TPS 限流相关日志。 */
    public static final Logger TPS =
        LoggerFactory.getLogger("com.alibaba.nacos.plugin.control.tps");
    
    /** 连接数管控相关日志。 */
    public static final Logger CONNECTION =
        LoggerFactory.getLogger("com.alibaba.nacos.plugin.control.connection");
    
}
