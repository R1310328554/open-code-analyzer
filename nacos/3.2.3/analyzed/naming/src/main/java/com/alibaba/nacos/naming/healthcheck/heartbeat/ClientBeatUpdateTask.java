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

package com.alibaba.nacos.naming.healthcheck.heartbeat;

import com.alibaba.nacos.common.task.AbstractExecuteTask;
import com.alibaba.nacos.naming.core.v2.client.impl.IpPortBasedClient;
import com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;

/**
 * 客户端心跳批量更新时间任务。
 *
 * <p>将客户端下全部实例的最后心跳时间设为当前时刻，并刷新客户端更新时间。</p>
 *
 * @author xiweng.yy
 */
public class ClientBeatUpdateTask extends AbstractExecuteTask {
    
    /** 目标客户端。 */
    private final IpPortBasedClient client;
    
    /** 绑定待更新的客户端。 */
    public ClientBeatUpdateTask(IpPortBasedClient client) {
        this.client = client;
    }
    
    /** 批量刷新全部实例心跳时间与客户端更新时间。 */
    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        for (InstancePublishInfo each : client.getAllInstancePublishInfo()) {
            ((HealthCheckInstancePublishInfo) each).setLastHeartBeatTime(currentTime);
        }
        client.setLastUpdatedTime();
    }
}
