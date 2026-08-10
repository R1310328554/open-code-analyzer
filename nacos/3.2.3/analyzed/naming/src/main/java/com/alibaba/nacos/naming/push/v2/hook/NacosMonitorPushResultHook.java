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

package com.alibaba.nacos.naming.push.v2.hook;

import com.alibaba.nacos.naming.monitor.MetricsMonitor;
import com.alibaba.nacos.naming.monitor.NamingTpsMonitor;

/**
 * 命名推送结果监控钩子实现。
 *
 * <p>实现 {@link PushResultHook}，在推送成功/失败后更新 {@link MetricsMonitor} 计数与耗时，并通过 {@link NamingTpsMonitor} 记录 RPC 推送 TPS；空推送单独计数。</p>
 *
 * @author xiweng.yy
 */
public class NacosMonitorPushResultHook implements PushResultHook {
    
    /** 推送成功：累加推送次数、耗时、最大耗时，空推送与 TPS 成功指标。 */
    @Override
    public void pushSuccess(PushResult result) {
        MetricsMonitor.incrementPush();
        MetricsMonitor.incrementPushCost(result.getAllCost());
        MetricsMonitor.compareAndSetMaxPushCost(result.getAllCost());
        if (null == result.getData().getHosts() || !result.getData().validate()) {
            MetricsMonitor.incrementEmptyPush();
        }
        NamingTpsMonitor.rpcPushSuccess(result.getSubscribeClientId(),
            result.getSubscriber().getIp());
    }
    
    /** 推送失败：累加总推送与失败次数，并记录 TPS 失败指标。 */
    @Override
    public void pushFailed(PushResult result) {
        MetricsMonitor.incrementPush();
        MetricsMonitor.incrementFailPush();
        NamingTpsMonitor.rpcPushFail(result.getSubscribeClientId(), result.getSubscriber().getIp());
    }
}
