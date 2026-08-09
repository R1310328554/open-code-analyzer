/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.stats;

import java.util.concurrent.ScheduledExecutorService;
import org.apache.rocketmq.logging.org.slf4j.Logger;

/**
 * 响应时间（RT）统计项：继承 {@link StatsItem}，日志输出使用 AVGRT 而非 TPS/SUM。
 */
public class RTStatsItem extends StatsItem {

    /** 构造 RT 统计项。 */
    public RTStatsItem(String statsName, String statsKey, ScheduledExecutorService scheduledExecutorService,
        Logger logger) {
        super(statsName, statsKey, scheduledExecutorService, logger);
    }

    /** RT 统计打印 TIMES 与 AVGRT，不含 SUM/TPS。 */
    @Override
    protected String statPrintDetail(StatsSnapshot ss) {
        return String.format("TIMES: %d AVGRT: %.2f", ss.getTimes(), ss.getAvgpt());
    }
}
