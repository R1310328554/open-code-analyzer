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

package com.alibaba.nacos.cmdb.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loggers holder.
 * <p>CMDB 模块 SLF4J 日志入口聚合，统一使用 logger 名 {@code com.alibaba.nacos.cmdb.main}。</p>
 *
 * @author nacos
 * @since 0.7.0
 */
public class Loggers {
    
    /** CMDB 主流程日志（dump/标签/事件任务等） */
    public static final Logger MAIN = LoggerFactory.getLogger("com.alibaba.nacos.cmdb.main");
}
