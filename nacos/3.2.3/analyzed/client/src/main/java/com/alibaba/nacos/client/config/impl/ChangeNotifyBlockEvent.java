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

package com.alibaba.nacos.client.config.impl;

import com.alibaba.nacos.common.notify.SlowEvent;

/**
 * 配置变更通知阻塞事件。
 *
 * <p>当 {@link Listener#receiveConfigInfo} 执行超时仍未完成时，由 {@link CacheData} 的阻塞监控线程发布，携带监听器、配置定位信息与堆栈快照。</p>
 *
 * @author shiyiyue
 */
public class ChangeNotifyBlockEvent extends SlowEvent {
    
    public ChangeNotifyBlockEvent(String listener, String dataId, String group, String tenant,
        long startTime,
        long currentTime, String blockStack) {
        this.listener = listener;
        this.dataId = dataId;
        this.group = group;
        this.tenant = tenant;
        this.startTime = startTime;
        this.currentTime = currentTime;
        this.blockStack = blockStack;
    }
    
    /** 阻塞中的监听器类名。 */
    private String listener;
    
    /** 配置 Data ID。 */
    private String dataId;
    
    /** 配置分组。 */
    private String group;
    
    /** 命名空间（tenant）。 */
    private String tenant;
    
    /** 通知开始时间戳（毫秒）。 */
    private long startTime;
    
    /** 检测到阻塞时的当前时间戳（毫秒）。 */
    private long currentTime;
    
    /** 监听器线程的堆栈快照，用于排查阻塞原因。 */
    private String blockStack;
    
    public String getDataId() {
        return dataId;
    }
    
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getTenant() {
        return tenant;
    }
    
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getCurrentTime() {
        return currentTime;
    }
    
    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
    
    public String getBlockStack() {
        return blockStack;
    }
    
    public void setBlockStack(String blockStack) {
        this.blockStack = blockStack;
    }
    
    @Override
    public String toString() {
        return "ChangeNotifyBlockEvent{" + "listener='" + listener + '\'' + ", dataId='" + dataId
            + '\'' + ", group='"
            + group + '\'' + ", tenant='" + tenant + '\'' + ", startTime=" + startTime
            + ", currentTime="
            + currentTime + ", blockStack='" + blockStack + '\'' + '}';
    }
}
