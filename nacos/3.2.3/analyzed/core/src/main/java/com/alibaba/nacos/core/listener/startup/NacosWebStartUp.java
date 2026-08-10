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

package com.alibaba.nacos.core.listener.startup;

import org.slf4j.Logger;

/**
 * Nacos Web API 模块启动阶段，对应 {@link NacosStartUp#WEB_START_UP_PHASE}。
 * <p>负责 Open API / gRPC 等对外服务层的启动日志与耗时统计。</p>
 * Nacos Server Web API start up phase.
 *
 * @author xiweng.yy
 */
public class NacosWebStartUp extends AbstractNacosStartUp {
    
    /** 注册为 web 启动阶段实现。 */
    public NacosWebStartUp() {
        super(NacosStartUp.WEB_START_UP_PHASE);
    }
    
    /** 启动日志阶段名："Nacos Server API"。 */
    @Override
    protected String getPhaseNameInStartingInfo() {
        return "Nacos Server API";
    }
    
    /** 输出 Web API 启动成功及耗时（毫秒）。 */
    @Override
    public void logStarted(Logger logger) {
        long endTimestamp = System.currentTimeMillis();
        long startupCost = endTimestamp - getStartTimestamp();
        logger.info("Nacos Server API started successfully in {} ms", startupCost);
    }
}
