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

package com.alibaba.nacos.naming.core.v2.pojo;

import com.alibaba.nacos.naming.healthcheck.HealthCheckStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 带健康检查状态的实例发布信息（V1 心跳检测场景）。
 *
 * <p>维护最近心跳时间与 {@link HealthCheckStatus}，支持并发探测互斥。</p>
 *
 * @author xiweng.yy
 */
public class HealthCheckInstancePublishInfo extends InstancePublishInfo {
    
    private static final long serialVersionUID = 5424801693490263492L;
    
    /** 最近一次心跳时间戳（毫秒）。 */
    private long lastHeartBeatTime = System.currentTimeMillis();
    
    /** 健康检查计数与探测状态。 */
    private HealthCheckStatus healthCheckStatus;
    
    public HealthCheckInstancePublishInfo() {
    }
    
    public HealthCheckInstancePublishInfo(String ip, int port) {
        super(ip, port);
    }
    
    public long getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }
    
    public void setLastHeartBeatTime(long lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }
    
    /** 初始化健康检查状态对象。 */
    public void initHealthCheck() {
        healthCheckStatus = new HealthCheckStatus();
    }
    
    /** CAS 尝试开始一次健康检查，避免重复并发探测。 */
    public boolean tryStartCheck() {
        return healthCheckStatus.isBeingChecked.compareAndSet(false, true);
    }
    
    /** 结束当前健康检查，释放探测锁。 */
    public void finishCheck() {
        healthCheckStatus.isBeingChecked.set(false);
    }
    
    /** 重置连续成功计数。 */
    public void resetOkCount() {
        healthCheckStatus.checkOkCount.set(0);
    }
    
    /** 重置连续失败计数。 */
    public void resetFailCount() {
        healthCheckStatus.checkFailCount.set(0);
    }
    
    /** 记录最近一次健康检查耗时（毫秒）。 */
    public void setCheckRt(long checkRt) {
        healthCheckStatus.checkRt = checkRt;
    }
    
    @JsonIgnore
    public AtomicInteger getOkCount() {
        return healthCheckStatus.checkOkCount;
    }
    
    @JsonIgnore
    public AtomicInteger getFailCount() {
        return healthCheckStatus.checkFailCount;
    }
}
