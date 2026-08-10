/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI module log util.
 * <p>AI 模块日志工具，提供 AI 资源版本操作审计专用 TRACE 日志器。</p>
 *
 * @author nacos
 */
public class AiLogUtil {
    
    /**
     * AI resource trace log for auditing AI resource version operations.
     * <p>AI 资源版本操作审计追踪日志（logger 名 com.alibaba.nacos.ai.resource.trace）。</p>
     */
    public static final Logger TRACE_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.ai.resource.trace");
}
