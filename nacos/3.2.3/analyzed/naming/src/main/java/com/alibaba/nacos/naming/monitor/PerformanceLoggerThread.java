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

package com.alibaba.nacos.naming.monitor;

import com.alibaba.nacos.core.distributed.distro.monitor.DistroRecord;
import com.alibaba.nacos.core.distributed.distro.monitor.DistroRecordsHolder;
import com.alibaba.nacos.naming.consistency.ephemeral.distro.v2.DistroClientDataProcessor;
import com.alibaba.nacos.naming.misc.GlobalExecutor;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.NamingExecuteTaskDispatcher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Naming 性能日志定时输出组件。
 *
 * <p>周期性将服务数、实例数、推送耗时、Distro 同步统计等写入 PERFORMANCE 日志，并定时采集/重置 {@link MetricsMonitor} 指标，便于运维排查性能瓶颈。</p>
 *
 * @author nacos
 */

@Component
public class PerformanceLoggerThread {
    
    /** 性能日志输出周期（秒）。 */
    private static final long PERIOD = 60;
    
    /** Spring 容器就绪后启动性能日志定时任务。 */
    @PostConstruct
    public void init() {
        start();
    }
    
    private void start() {
        PerformanceLogTask task = new PerformanceLogTask();
        GlobalExecutor.schedulePerformanceLogger(task, 30, PERIOD, TimeUnit.SECONDS);
    }
    
    /**
     * 每日零点重置全部 MetricsMonitor 指标。
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshMetrics() {
        MetricsMonitor.resetAll();
    }
    
    /**
     * 每 15 秒采集服务总数与平均推送耗时。
     */
    @Scheduled(cron = "0/15 * * * * ?")
    public void collectMetrics() {
        MetricsMonitor.getDomCountMonitor()
            .set(com.alibaba.nacos.naming.core.v2.ServiceManager.getInstance().size());
        MetricsMonitor.getAvgPushCostMonitor().set(getAvgPushCost());
    }
    
    /** 性能日志输出 Runnable，由 GlobalExecutor 定时调度。 */
    class PerformanceLogTask implements Runnable {
        
        /** 日志轮次计数，每 10 次输出表头。 */
        private int logCount = 0;
        
        @Override
        public void run() {
            try {
                logCount %= 10;
                if (logCount == 0) {
                    Loggers.PERFORMANCE_LOG
                        .info(
                            "PERFORMANCE:|serviceCount|ipCount|subscribeCount|maxPushCost|avgPushCost|totalPushCount|failPushCount");
                    Loggers.PERFORMANCE_LOG
                        .info("DISTRO:|V1SyncDone|V1SyncFail|V2SyncDone|V2SyncFail|V2VerifyFail|");
                }
                int serviceCount =
                    com.alibaba.nacos.naming.core.v2.ServiceManager.getInstance().size();
                int ipCount = MetricsMonitor.getIpCountMonitor().get();
                int subscribeCount = MetricsMonitor.getSubscriberCount().get();
                long maxPushCost = MetricsMonitor.getMaxPushCostMonitor().get();
                long avgPushCost = getAvgPushCost();
                long totalPushCount = MetricsMonitor.getTotalPushMonitor().longValue();
                long failPushCount = MetricsMonitor.getFailedPushMonitor().longValue();
                Loggers.PERFORMANCE_LOG
                    .info("PERFORMANCE:|{}|{}|{}|{}|{}|{}|{}", serviceCount, ipCount,
                        subscribeCount, maxPushCost,
                        avgPushCost, totalPushCount, failPushCount);
                Loggers.PERFORMANCE_LOG
                    .info("Task worker status: \n"
                        + NamingExecuteTaskDispatcher.getInstance().workersStatus());
                printDistroMonitor();
                logCount++;
                MetricsMonitor.getTotalPushCountForAvg().set(0);
                MetricsMonitor.getTotalPushCostForAvg().set(0);
                MetricsMonitor.getMaxPushCostMonitor().set(-1);
            } catch (Exception e) {
                Loggers.SRV_LOG.warn("[PERFORMANCE] Exception while print performance log.", e);
            }
            
        }
        
        /** 输出 v2 Distro 同步/校验统计到 PERFORMANCE 日志。 */
        private void printDistroMonitor() {
            Optional<DistroRecord> v2Record = DistroRecordsHolder.getInstance()
                .getRecordIfExist(DistroClientDataProcessor.TYPE);
            long v2SyncDone = 0;
            long v2SyncFail = 0;
            int v2VerifyFail = 0;
            if (v2Record.isPresent()) {
                v2SyncDone = v2Record.get().getSuccessfulSyncCount();
                v2SyncFail = v2Record.get().getFailedSyncCount();
                v2VerifyFail = v2Record.get().getFailedVerifyCount();
            }
            Loggers.PERFORMANCE_LOG.info("DISTRO:|{}|{}|{}|", v2SyncDone, v2SyncFail, v2VerifyFail);
        }
    }
    
    /** 计算当前周期内平均推送耗时（毫秒），无样本时返回 -1。 */
    private long getAvgPushCost() {
        int size = MetricsMonitor.getTotalPushCountForAvg().get();
        long totalCost = MetricsMonitor.getTotalPushCostForAvg().get();
        return (size > 0 && totalCost > 0) ? totalCost / size : -1;
    }
}
