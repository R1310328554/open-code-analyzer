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
package org.apache.rocketmq.common.statistics;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 基类：在 {@link ScheduledExecutorService} 上定时打印 {@link StatisticsItem} 累计值。
 */
public class StatisticsItemScheduledPrinter extends FutureHolder {
    /** 日志前缀名称。 */
    protected String name;

    /** 统计项打印机。 */
    protected StatisticsItemPrinter printer;
    /** 调度线程池。 */
    protected ScheduledExecutorService executor;
    /** 打印间隔（毫秒）。 */
    protected long interval;
    /** 首次打印延迟策略。 */
    protected InitialDelay initialDelay;
    /** 开关与零行打印策略。 */
    protected Valve valve;

    /** 构造定时打印机。 */
    public StatisticsItemScheduledPrinter(String name, StatisticsItemPrinter printer,
                                          ScheduledExecutorService executor, InitialDelay initialDelay,
                                          long interval, Valve valve) {
        this.name = name;
        this.printer = printer;
        this.executor = executor;
        this.initialDelay = initialDelay;
        this.interval = interval;
        this.valve = valve;
    }

    /** 注册统计项，按 interval 周期性打印当前累计值。 */
    public void schedule(final StatisticsItem statisticsItem) {
        ScheduledFuture future = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (enabled()) {
                    printer.print(name, statisticsItem);
                }
            }
        }, getInitialDelay(), interval, TimeUnit.MILLISECONDS);

        addFuture(statisticsItem, future);
    }

    /** 取消该统计项关联的全部定时任务。 */
    public void remove(final StatisticsItem statisticsItem) {
        removeAllFuture(statisticsItem);
    }

    /** 首次调度延迟（毫秒）。 */
    public interface InitialDelay {
        /** 返回初始延迟毫秒数。 */
        long get();
    }

    /** 控制是否启用打印及是否输出全零行。 */
    public interface Valve {
        /** 是否启用定时打印。 */
        boolean enabled();

        /** 增量全为 0 时是否仍打印一行。 */
        boolean printZeroLine();
    }

    /** 解析初始延迟，无 InitialDelay 时为 0。 */
    protected long getInitialDelay() {
        return initialDelay != null ? initialDelay.get() : 0;
    }

    /** 是否启用（依赖 Valve，默认 false）。 */
    protected boolean enabled() {
        return valve != null ? valve.enabled() : false;
    }

    /** 是否打印全零增量行。 */
    protected boolean printZeroLine() {
        return valve != null ? valve.printZeroLine() : false;
    }

}
