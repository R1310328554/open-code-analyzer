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

package com.alibaba.nacos.core.monitor;

import com.alibaba.nacos.common.utils.StringUtils;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Nacos Core 模块指标中心，集中注册 Raft、gRPC 长连接与线程池等 Micrometer 指标。
 * <p>通过 {@link NacosMeterRegistryCenter#CORE_STABLE_REGISTRY} 统一上报。</p>
 * The Metrics center.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class MetricsMonitor {
    
    /** 使用的稳定指标注册表名称。 */
    private static final String METER_REGISTRY = NacosMeterRegistryCenter.CORE_STABLE_REGISTRY;
    
    /** Raft ReadIndex 失败次数分布摘要。 */
    private static final DistributionSummary RAFT_READ_INDEX_FAILED;
    
    /** 从 Leader 读取的请求计数分布。 */
    private static final DistributionSummary RAFT_FROM_LEADER;
    
    /** Raft 日志 apply 耗时计时器。 */
    private static final Timer RAFT_APPLY_LOG_TIMER;
    
    /** Raft 读请求 apply 耗时计时器。 */
    private static final Timer RAFT_APPLY_READ_TIMER;
    
    /** 全局 gRPC 长连接数 Gauge 值。 */
    private static AtomicInteger longConnection = new AtomicInteger();
    
    /** SDK gRPC 服务端线程池指标容器。 */
    private static GrpcServerExecutorMetric sdkServerExecutorMetric =
        new GrpcServerExecutorMetric("grpcSdkServer");
    
    /** 集群 gRPC 服务端线程池指标容器。 */
    private static GrpcServerExecutorMetric clusterServerExecutorMetric =
        new GrpcServerExecutorMetric("grpcClusterServer");
    
    /** 各模块长连接数：module → 连接计数。 */
    private static Map<String, AtomicInteger> moduleConnectionCnt = new ConcurrentHashMap<>();
    
    /** 各 Raft Group 本节点是否为 Leader（1/0）。 */
    private static Map<String, AtomicInteger> raftGroupLeaderStatus = new ConcurrentHashMap<>();
    
    /** 各 Raft Group 当前 Term 值。 */
    private static Map<String, AtomicLong> raftGroupTerm = new ConcurrentHashMap<>();
    
    static {
        ImmutableTag immutableTag = new ImmutableTag("module", "core");
        List<Tag> tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "raft_read_index_failed"));
        RAFT_READ_INDEX_FAILED =
            NacosMeterRegistryCenter.summary(METER_REGISTRY, "nacos_monitor_summary", tags);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "raft_read_from_leader"));
        RAFT_FROM_LEADER =
            NacosMeterRegistryCenter.summary(METER_REGISTRY, "nacos_monitor_summary", tags);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "raft_apply_log_timer"));
        RAFT_APPLY_LOG_TIMER =
            NacosMeterRegistryCenter.timer(METER_REGISTRY, "nacos_monitor_summary", tags);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "raft_apply_read_timer"));
        RAFT_APPLY_READ_TIMER =
            NacosMeterRegistryCenter.timer(METER_REGISTRY, "nacos_monitor_summary", tags);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "longConnection"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, longConnection);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("type", sdkServerExecutorMetric.getType()));
        initGrpcServerExecutorMetric(tags, sdkServerExecutorMetric);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("type", clusterServerExecutorMetric.getType()));
        initGrpcServerExecutorMetric(tags, clusterServerExecutorMetric);
    }
    
    /** 为指定 gRPC 服务端类型注册线程池各维度 Gauge 指标。 */
    private static void initGrpcServerExecutorMetric(List<Tag> tags,
        GrpcServerExecutorMetric metric) {
        List<Tag> snapshotTags = new ArrayList<>();
        snapshotTags.add(new ImmutableTag("name", "activeCount"));
        snapshotTags.addAll(tags);
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "grpc_server_executor", snapshotTags,
            metric.getActiveCount());
        
        snapshotTags = new ArrayList<>();
        snapshotTags.add(new ImmutableTag("name", "poolSize"));
        snapshotTags.addAll(tags);
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "grpc_server_executor", snapshotTags,
            metric.getPoolSize());
        
        snapshotTags = new ArrayList<>();
        snapshotTags.add(new ImmutableTag("name", "corePoolSize"));
        snapshotTags.addAll(tags);
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "grpc_server_executor", snapshotTags,
            metric.getCorePoolSize());
        
        snapshotTags = new ArrayList<>();
        snapshotTags.add(new ImmutableTag("name", "maximumPoolSize"));
        snapshotTags.addAll(tags);
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "grpc_server_executor", snapshotTags,
            metric.getMaximumPoolSize());
        
        snapshotTags = new ArrayList<>();
        snapshotTags.add(new ImmutableTag("name", "inQueueTaskCount"));
        snapshotTags.addAll(tags);
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "grpc_server_executor", snapshotTags,
            metric.getInQueueTaskCount());
        
        snapshotTags = new ArrayList<>();
        snapshotTags.add(new ImmutableTag("name", "taskCount"));
        snapshotTags.addAll(tags);
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "grpc_server_executor", snapshotTags,
            metric.getTaskCount());
        
        snapshotTags = new ArrayList<>();
        snapshotTags.add(new ImmutableTag("name", "completedTaskCount"));
        snapshotTags.addAll(tags);
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "grpc_server_executor", snapshotTags,
            metric.getCompletedTaskCount());
    }
    
    /** 获取全局长连接数 AtomicInteger（供外部更新）。 */
    public static AtomicInteger getLongConnectionMonitor() {
        return longConnection;
    }
    
    /** 记录一次 Raft ReadIndex 失败。 */
    public static void raftReadIndexFailed() {
        RAFT_READ_INDEX_FAILED.record(1);
    }
    
    /** 记录一次从 Leader 发起的 Raft 读。 */
    public static void raftReadFromLeader() {
        RAFT_FROM_LEADER.record(1);
    }
    
    /** 获取 Raft 日志 apply 计时器。 */
    public static Timer getRaftApplyLogTimer() {
        return RAFT_APPLY_LOG_TIMER;
    }
    
    /** 获取 Raft 读 apply 计时器。 */
    public static Timer getRaftApplyReadTimer() {
        return RAFT_APPLY_READ_TIMER;
    }
    
    /** 获取 ReadIndex 失败分布摘要指标。 */
    public static DistributionSummary getRaftReadIndexFailed() {
        return RAFT_READ_INDEX_FAILED;
    }
    
    /** 获取 Leader 读分布摘要指标。 */
    public static DistributionSummary getRaftFromLeader() {
        return RAFT_FROM_LEADER;
    }
    
    /**
     * 刷新指定 Raft Group 的 Leader 状态与 Term 指标（供 Actuator/Prometheus 采集）。
     *
     * @param groupId raft group id
     * @param leader current leader endpoint
     * @param term current raft term
     * @param selfMember local raft endpoint
     */
    public static void refreshRaftGroupMetrics(String groupId, String leader, Long term,
        String selfMember) {
        if (StringUtils.isBlank(groupId)) {
            return;
        }
        if (StringUtils.isNotBlank(leader)) {
            AtomicInteger leaderStatus = raftGroupLeaderStatus.computeIfAbsent(groupId,
                MetricsMonitor::registerRaftGroupLeaderStatus);
            leaderStatus.set(StringUtils.equals(leader, selfMember) ? 1 : 0);
        }
        if (term != null) {
            raftGroupTerm.computeIfAbsent(groupId, MetricsMonitor::registerRaftGroupTerm)
                .set(term);
        }
    }
    
    /** 为 Group 注册 Leader 状态 Gauge 并返回可更新的 AtomicInteger。 */
    private static AtomicInteger registerRaftGroupLeaderStatus(String groupId) {
        AtomicInteger result = new AtomicInteger();
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor",
            Arrays.asList(
                new ImmutableTag("module", "core"),
                new ImmutableTag("name", "raftLeaderStatus"),
                new ImmutableTag("group", groupId)),
            result);
        return result;
    }
    
    /** 为 Group 注册 Term Gauge 并返回可更新的 AtomicLong。 */
    private static AtomicLong registerRaftGroupTerm(String groupId) {
        AtomicLong result = new AtomicLong();
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor",
            Arrays.asList(
                new ImmutableTag("module", "core"),
                new ImmutableTag("name", "raftTerm"),
                new ImmutableTag("group", groupId)),
            result);
        return result;
    }
    
    /** 获取 SDK gRPC 线程池指标容器。 */
    public static GrpcServerExecutorMetric getSdkServerExecutorMetric() {
        return sdkServerExecutorMetric;
    }
    
    /** 获取集群 gRPC 线程池指标容器。 */
    public static GrpcServerExecutorMetric getClusterServerExecutorMetric() {
        return clusterServerExecutorMetric;
    }
    
    /** gRPC 服务端线程池各维度指标的内存容器，由 {@link GrpcServerThreadPoolMonitor} 周期性刷新。 */
    public static class GrpcServerExecutorMetric {
        
        /** 服务端类型标识（grpcSdkServer / grpcClusterServer）。 */
        private String type;
        
        /**
         * 正在执行任务的活跃线程数。
         */
        private AtomicInteger activeCount = new AtomicInteger();
        
        /**
         * 线程池核心线程数。
         */
        private AtomicInteger corePoolSize = new AtomicInteger();
        
        /**
         * 当前线程池中的线程总数。
         */
        private AtomicInteger poolSize = new AtomicInteger();
        
        /**
         * 线程池允许的最大线程数。
         */
        private AtomicInteger maximumPoolSize = new AtomicInteger();
        
        /**
         * 工作队列中等待执行的任务数。
         */
        private AtomicInteger inQueueTaskCount = new AtomicInteger();
        
        /**
         * 已完成的任务总数。
         */
        private AtomicLong completedTaskCount = new AtomicLong();
        
        /**
         * 已提交的任务总数（含已完成与排队中）。
         */
        private AtomicLong taskCount = new AtomicLong();
        
        /** 按类型标识构造指标容器。 */
        private GrpcServerExecutorMetric(String type) {
            this.type = type;
        }
        
        /** 获取活跃线程数引用。 */
        public AtomicInteger getActiveCount() {
            return activeCount;
        }
        
        /** 获取核心线程数引用。 */
        public AtomicInteger getCorePoolSize() {
            return corePoolSize;
        }
        
        /** 获取当前线程数引用。 */
        public AtomicInteger getPoolSize() {
            return poolSize;
        }
        
        /** 获取最大线程数引用。 */
        public AtomicInteger getMaximumPoolSize() {
            return maximumPoolSize;
        }
        
        /** 获取队列任务数引用。 */
        public AtomicInteger getInQueueTaskCount() {
            return inQueueTaskCount;
        }
        
        /** 获取已完成任务数引用。 */
        public AtomicLong getCompletedTaskCount() {
            return completedTaskCount;
        }
        
        /** 获取总任务数引用。 */
        public AtomicLong getTaskCount() {
            return taskCount;
        }
        
        /** 获取服务端类型标识。 */
        public String getType() {
            return type;
        }
    }
    
    /**
     * 刷新各模块 gRPC 长连接数；新模块自动注册 Gauge，下线模块计数归零。
     *
     * @param connectionCnt new connection count.
     */
    public static void refreshModuleConnectionCount(Map<String, Integer> connectionCnt) {
        // 更新已有模块连接数，并为新模块注册 Gauge
        connectionCnt.forEach((module, cnt) -> {
            AtomicInteger integer = moduleConnectionCnt.get(module);
            // 已注册模块直接更新计数
            if (integer != null) {
                integer.set(cnt);
            } else {
                // 新模块首次出现时注册指标
                AtomicInteger newModuleConnCnt = new AtomicInteger(cnt);
                moduleConnectionCnt.put(module, newModuleConnCnt);
                NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor",
                    Arrays.asList(
                        new ImmutableTag("module", module),
                        new ImmutableTag("name", "longConnection")),
                    moduleConnectionCnt.get(module));
            }
        });
        // 本次快照中未出现的模块连接数置 0
        moduleConnectionCnt.forEach((module, cnt) -> {
            if (connectionCnt.containsKey(module)) {
                return;
            }
            cnt.set(0);
        });
    }
    
    /**
     * 获取各模块长连接计数 Map（测试或诊断用）。
     *
     * @return moduleConnectionCnt.
     */
    public static Map<String, AtomicInteger> getModuleConnectionCnt() {
        return moduleConnectionCnt;
    }
    
    /**
     * 记录 gRPC 请求事件到 Timer 指标（含成功/失败、错误码、异常类与耗时）。
     *
     * @param requestClass      requestClass
     * @param success           success
     * @param errorCode         errorCode
     * @param throwableClass    throwableClass
     * @param module            module
     * @param costTime              cost
     */
    public static void recordGrpcRequestEvent(String requestClass,
        boolean success,
        int errorCode,
        String throwableClass,
        String module,
        long costTime) {
        NacosMeterRegistryCenter.timer(METER_REGISTRY, "grpc_server_requests",
            Arrays.asList(
                Tag.of("requestClass", requestClass),
                Tag.of("success", String.valueOf(success)),
                Tag.of("errorCode", String.valueOf(errorCode)),
                Tag.of("throwableClass",
                    StringUtils.isBlank(throwableClass) ? "None" : throwableClass),
                Tag.of("module", StringUtils.isBlank(module) ? "unknown" : module)))
            .record(costTime, TimeUnit.NANOSECONDS);
    }
}
