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

package com.alibaba.nacos.core.distributed;

import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.core.utils.ClassUtils;

import java.util.concurrent.ExecutorService;

/**
 * 一致性协议专用线程池：为 CP/AP 集群成员变更提供单线程串行执行器，避免并发修改协议状态。
 * ProtocolExecutor.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class ProtocolExecutor {
    
    /** CP 协议成员变更单线程执行器。 */
    private static final ExecutorService CP_MEMBER_CHANGE_EXECUTOR = ExecutorFactory.Managed
        .newSingleExecutorService(ClassUtils.getCanonicalName(ProtocolManager.class));
    
    /** AP 协议成员变更单线程执行器。 */
    private static final ExecutorService AP_MEMBER_CHANGE_EXECUTOR = ExecutorFactory.Managed
        .newSingleExecutorService(ClassUtils.getCanonicalName(ProtocolManager.class));
    
    /**
     * 在 CP 成员变更线程池中异步执行 Runnable。
     *
     * @param runnable 成员变更任务
     */
    public static void cpMemberChange(Runnable runnable) {
        CP_MEMBER_CHANGE_EXECUTOR.execute(runnable);
    }
    
    /**
     * 在 AP 成员变更线程池中异步执行 Runnable。
     *
     * @param runnable 成员变更任务
     */
    public static void apMemberChange(Runnable runnable) {
        AP_MEMBER_CHANGE_EXECUTOR.execute(runnable);
    }
    
}
