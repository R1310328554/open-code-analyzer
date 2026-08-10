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

package com.alibaba.nacos.core.distributed.raft.utils;

import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.core.distributed.raft.JRaftServer;
import com.alibaba.nacos.core.distributed.raft.RaftConfig;
import com.alibaba.nacos.core.distributed.raft.RaftSysConstants;
import com.alibaba.nacos.core.utils.ClassUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Raft 专用线程池门面：按 {@link com.alibaba.nacos.core.distributed.raft.RaftConfig} 初始化核心、CLI、公共调度与快照四类执行器。
 * raft executor.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class RaftExecutor {
    
    /** Raft 核心 apply/RPC 处理线程池。 */
    private static ExecutorService raftCoreExecutor;
    
    /** Raft CLI 运维命令线程池。 */
    private static ExecutorService raftCliServiceExecutor;
    
    /** Raft 公共调度线程池（成员刷新、延迟任务等）。 */
    private static ScheduledExecutorService raftCommonExecutor;
    
    /** Raft 快照异步执行线程池。 */
    private static ExecutorService raftSnapshotExecutor;
    
    /** 托管线程池归属标识（JRaftServer 全限定名）。 */
    private static final String OWNER = ClassUtils.getCanonicalName(JRaftServer.class);
    
    /** 工具类禁止实例化。 */
    private RaftExecutor() {
    }
    
    /**
     * 根据 {@link RaftConfig} 创建核心、CLI、公共调度与快照线程池。
     *
     * @param config {@link RaftConfig}
     */
    public static void init(RaftConfig config) {
        
        int raftCoreThreadNum =
            Integer.parseInt(config.getValOfDefault(RaftSysConstants.RAFT_CORE_THREAD_NUM, "8"));
        int raftCliServiceThreadNum = Integer
            .parseInt(config.getValOfDefault(RaftSysConstants.RAFT_CLI_SERVICE_THREAD_NUM, "4"));
        
        raftCoreExecutor = ExecutorFactory.Managed.newFixedExecutorService(OWNER, raftCoreThreadNum,
            new NameThreadFactory("com.alibaba.nacos.core.raft-core"));
        
        raftCliServiceExecutor =
            ExecutorFactory.Managed.newFixedExecutorService(OWNER, raftCliServiceThreadNum,
                new NameThreadFactory("com.alibaba.nacos.core.raft-cli-service"));
        
        raftCommonExecutor = ExecutorFactory.Managed.newScheduledExecutorService(OWNER, 8,
            new NameThreadFactory("com.alibaba.nacos.core.protocol.raft-common"));
        
        int snapshotNum = raftCoreThreadNum / 2;
        snapshotNum = snapshotNum == 0 ? raftCoreThreadNum : snapshotNum;
        
        raftSnapshotExecutor = ExecutorFactory.Managed.newFixedExecutorService(OWNER, snapshotNum,
            new NameThreadFactory("com.alibaba.nacos.core.raft-snapshot"));
        
    }
    
    /** 以固定速率调度 Raft 成员刷新任务。 */
    public static void scheduleRaftMemberRefreshJob(Runnable runnable, long initialDelay,
        long period, TimeUnit unit) {
        raftCommonExecutor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }
    
    /** 返回 Raft 核心线程池。 */
    public static ExecutorService getRaftCoreExecutor() {
        return raftCoreExecutor;
    }
    
    /** 返回 Raft CLI 线程池。 */
    public static ExecutorService getRaftCliServiceExecutor() {
        return raftCliServiceExecutor;
    }
    
    /** 在公共调度池上立即执行任务。 */
    public static void executeByCommon(Runnable r) {
        raftCommonExecutor.execute(r);
    }
    
    /** 在公共调度池上延迟执行任务。 */
    public static void scheduleByCommon(Runnable r, long delayMs) {
        raftCommonExecutor.schedule(r, delayMs, TimeUnit.MILLISECONDS);
    }
    
    /** 返回 Raft 公共调度线程池。 */
    public static ScheduledExecutorService getRaftCommonExecutor() {
        return raftCommonExecutor;
    }
    
    /** 在快照专用线程池上异步执行快照任务。 */
    public static void doSnapshot(Runnable runnable) {
        raftSnapshotExecutor.execute(runnable);
    }
    
}
