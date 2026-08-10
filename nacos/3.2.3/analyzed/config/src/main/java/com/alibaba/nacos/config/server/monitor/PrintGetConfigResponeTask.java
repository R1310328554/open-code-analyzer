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

package com.alibaba.nacos.config.server.monitor;

import static com.alibaba.nacos.config.server.utils.LogUtil.MEMORY_LOG;

/**
 * 定时打印拉配置响应耗时分布的任务：调用 {@link ResponseMonitor#getStringForPrint()} 写入 MEMORY_LOG。
 * 类名保留历史拼写 Respone。
 * PrintGetConfigResponeTask.
 *
 * @author zongtanghu
 */
public class PrintGetConfigResponeTask implements Runnable {
    
    /** 将响应耗时百分比分段统计写入内存监控日志 */
    @Override
    public void run() {
        MEMORY_LOG.info(ResponseMonitor.getStringForPrint());
    }
    
}
