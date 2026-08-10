/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.healthcheck.v2;

import com.alibaba.nacos.common.task.AbstractExecuteTask;
import com.alibaba.nacos.naming.core.v2.client.impl.IpPortBasedClient;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.healthcheck.HealthCheckReactor;
import com.alibaba.nacos.naming.healthcheck.NacosHealthCheckTask;
import com.alibaba.nacos.naming.healthcheck.v2.processor.HealthCheckProcessorV2Delegate;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.sys.utils.ApplicationUtils;
import com.alibaba.nacos.common.utils.RandomUtils;

import java.util.Optional;

/**
 * Nacos 命名 V2 健康检查定时任务，绑定 {@link IpPortBasedClient} 周期性探测其发布实例。
 *
 * <p>Current health check logic is same as v1.x. TODO refactor health check for v2.x.</p>
 * <p>维护检查 RT 统计（最优/最差/归一化），并在 finally 中重新调度下一次检查。</p>
 *
 * @author nacos
 */
public class HealthCheckTaskV2 extends AbstractExecuteTask implements NacosHealthCheckTask {
    
    /** 首次检查 RT 归一化下限（毫秒）。 */
    private static final int LOWER_CHECK_RT = 2000;
    
    /** SwitchDomain 未就绪时随机 RT 上限（毫秒）。 */
    private static final int UPPER_RANDOM_CHECK_RT = 5000;
    
    /** 全局开关域（懒加载静态缓存）。 */
    private static SwitchDomain switchDomain;
    
    /** 命名元数据管理器（懒加载静态缓存）。 */
    private static NamingMetadataManager metadataManager;
    
    /** 被检查的 IP:Port 客户端。 */
    private final IpPortBasedClient client;
    
    /** 任务 ID，等于 client.getResponsibleId()，用于 Distro 分片。 */
    private final String taskId;
    
    /** 归一化后的检查间隔 RT（毫秒）。 */
    private long checkRtNormalized = -1;
    
    private long checkRtBest = -1;
    
    private long checkRtWorst = -1;
    
    private long checkRtLast = -1;
    
    private long checkRtLastLast = -1;
    
    private long startTime;
    
    /** 取消标志，为 true 时不再重新调度。 */
    private volatile boolean cancelled = false;
    
    public HealthCheckTaskV2(IpPortBasedClient client) {
        this.client = client;
        this.taskId = client.getResponsibleId();
    }
    
    /** 懒初始化 SwitchDomain、MetadataManager 与检查 RT 参数。 */
    private void initIfNecessary() {
        if (switchDomain == null) {
            switchDomain = ApplicationUtils.getBean(SwitchDomain.class);
        }
        if (metadataManager == null) {
            metadataManager = ApplicationUtils.getBean(NamingMetadataManager.class);
        }
        initCheckRt();
    }
    
    private void initCheckRt() {
        if (-1 != checkRtNormalized) {
            return;
        }
        // 首次检查随机延迟，避免集群内同时探测
        if (null != switchDomain) {
            checkRtNormalized = LOWER_CHECK_RT + RandomUtils.nextInt(0,
                RandomUtils.nextInt(0, switchDomain.getTcpHealthParams().getMax()));
        } else {
            checkRtNormalized = LOWER_CHECK_RT + RandomUtils.nextInt(0, UPPER_RANDOM_CHECK_RT);
        }
        checkRtBest = Long.MAX_VALUE;
        checkRtWorst = 0L;
    }
    
    public IpPortBasedClient getClient() {
        return client;
    }
    
    @Override
    public String getTaskId() {
        return taskId;
    }
    
    /** 遍历客户端已发布服务，委托 {@link HealthCheckProcessorV2Delegate} 执行探测。 */
    @Override
    public void doHealthCheck() {
        try {
            initIfNecessary();
            for (Service each : client.getAllPublishedService()) {
                if (switchDomain.isHealthCheckEnabled(each.getGroupedServiceName())) {
                    InstancePublishInfo instancePublishInfo = client.getInstancePublishInfo(each);
                    ClusterMetadata metadata = getClusterMetadata(each, instancePublishInfo);
                    ApplicationUtils.getBean(HealthCheckProcessorV2Delegate.class).process(this,
                        each, metadata);
                    if (Loggers.EVT_LOG.isDebugEnabled()) {
                        Loggers.EVT_LOG.debug("[HEALTH-CHECK] schedule health check task: {}",
                            client.getClientId());
                    }
                }
            }
        } catch (Throwable e) {
            Loggers.SRV_LOG.error("[HEALTH-CHECK] error while process health check for {}",
                client.getClientId(), e);
        } finally {
            if (!cancelled) {
                initCheckRt();
                HealthCheckReactor.scheduleCheck(this);
                // worst 为 0 表示尚未完成过检查，跳过 RT 差分日志
                if (this.getCheckRtWorst() > 0) {
                    // TLog 不支持浮点，RT 变化率乘以 10000 转为 long
                    long checkRtLastLast = getCheckRtLastLast();
                    this.setCheckRtLastLast(this.getCheckRtLast());
                    if (checkRtLastLast > 0) {
                        long diff = ((this.getCheckRtLast() - this.getCheckRtLastLast()) * 10000)
                            / checkRtLastLast;
                        if (Loggers.CHECK_RT.isDebugEnabled()) {
                            Loggers.CHECK_RT.debug(
                                "{}->normalized: {}, worst: {}, best: {}, last: {}, diff: {}",
                                client.getClientId(), this.getCheckRtNormalized(),
                                this.getCheckRtWorst(),
                                this.getCheckRtBest(), this.getCheckRtLast(), diff);
                        }
                    }
                }
            }
        }
    }
    
    /** 拦截链放行后直接执行健康检查。 */
    @Override
    public void passIntercept() {
        doHealthCheck();
    }
    
    /** 被拦截后仍重新调度，保持任务生命周期。 */
    @Override
    public void afterIntercept() {
        if (!cancelled) {
            try {
                initIfNecessary();
            } finally {
                initCheckRt();
                HealthCheckReactor.scheduleCheck(this);
            }
        }
    }
    
    @Override
    public void run() {
        doHealthCheck();
    }
    
    /** 从服务元数据解析实例所在集群的 {@link ClusterMetadata}。 */
    private ClusterMetadata getClusterMetadata(Service service,
        InstancePublishInfo instancePublishInfo) {
        Optional<ServiceMetadata> serviceMetadata = metadataManager.getServiceMetadata(service);
        if (!serviceMetadata.isPresent()) {
            return new ClusterMetadata();
        }
        String cluster = instancePublishInfo.getCluster();
        ClusterMetadata result = serviceMetadata.get().getClusters().get(cluster);
        return null == result ? new ClusterMetadata() : result;
    }
    
    public long getCheckRtNormalized() {
        return checkRtNormalized;
    }
    
    public long getCheckRtBest() {
        return checkRtBest;
    }
    
    public long getCheckRtWorst() {
        return checkRtWorst;
    }
    
    public void setCheckRtWorst(long checkRtWorst) {
        this.checkRtWorst = checkRtWorst;
    }
    
    public void setCheckRtBest(long checkRtBest) {
        this.checkRtBest = checkRtBest;
    }
    
    public void setCheckRtNormalized(long checkRtNormalized) {
        this.checkRtNormalized = checkRtNormalized;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getCheckRtLast() {
        return checkRtLast;
    }
    
    public void setCheckRtLast(long checkRtLast) {
        this.checkRtLast = checkRtLast;
    }
    
    public long getCheckRtLastLast() {
        return checkRtLastLast;
    }
    
    public void setCheckRtLastLast(long checkRtLastLast) {
        this.checkRtLastLast = checkRtLastLast;
    }
}
